import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.PlatformID;
import jade.core.AID;

import jade.content.*;
import jade.content.lang.*;
import jade.content.lang.sl.*;
import jade.content.onto.basic.*;
import jade.domain.*;
import jade.domain.mobility.*;
import jade.domain.JADEAgentManagement.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;

import jade.realtime.scheduler.EDF;
import jade.realtime.scheduler.PEDF;
import jade.realtime.scheduler.FIFO;
import jade.realtime.scheduler.LIFO;
import jade.realtime.scheduler.DM;
import jade.realtime.scheduler.PRIO;
import jade.realtime.scheduler.PPRIO;
import jade.realtime.scheduler.PDM;
import jade.realtime.scheduler.SJF;
import jade.realtime.scheduler.SRTF;
import jade.realtime.scheduler.RoundRobin;

import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import queueUtils.maIdent;


public class MobileAgent extends Agent{

	private static final long serialVersionUID = 1L;

	private static final int FIFO  				= 1;
	private static final int EDF   				= 2;
	private static final int LIFO 				= 3;
	private static final int PreemptiveEDF  	= 4;
	private static final int DeadlineMonotonic  = 5;
	private static final int PRIO  				= 6;
	private static final int PreemptivePRIO  	= 7;
	private static final int PreemptiveDM  		= 8;
	private static final int SJF  				= 9;
	private static final int SRTF  				= 10;
	private static final int RoundRobin  		= 11;

	private ArrayList<String> nodos    	= new ArrayList<String>();
	private ArrayList<String> oldHosts 	= new ArrayList<String>();
	private ArrayList<String> resource 	= new ArrayList<String>();
	private ArrayList<String> qstatus 	= new ArrayList<String>();

	private LinkedBlockingQueue<String>[] queueSize;

	private boolean _done;

	private int linkedQueueSize=0;
	private int step;
	private int scheduler=-1;
	private int myps;//elemento de prioridade (deadline, credencial...)
	private long w        = 0;
	private long startJob = 0;

	private long startTime;
	private long myTotalTime;

	private String request="";
	private String hostToGo="";
	private String localHost="";
	private String result="";
	private String firstInQueue="";

	private AID user;

	private InetAddress address;

	private String HOME="";

	protected void setup(){

		/*########### Recuperando Parametros ###########*/
		System.out.println(getAID().getName()+" started!");

		request= (String)(getArguments()[0]);
		scheduler= (Integer) (getArguments()[1]) ;
		user = (AID)(getArguments()[2]);
		nodos = (ArrayList)(getArguments()[3]);
		HOME = (String)(getArguments()[4]); //hostname do nodo HOME
		myps = (Integer)(getArguments()[5]);
		resource = (ArrayList)(getArguments()[6]);

	//	System.out.println(getAID().getName()+ " resources: "+resource.size());
		/*#############################################*/

		this.queueSize = new LinkedBlockingQueue[nodos.size()];
		for(int i = 0;i<nodos.size();i++) {
			this.queueSize[i] = new LinkedBlockingQueue<String>();
		}
		startTime = System.currentTimeMillis(); //t0 do agente movel
		addBehaviour(new actions());
		addBehaviour(new Processing(this));
	}//setup

	private class Processing extends CyclicBehaviour{
		private MobileAgent ma;
		public Processing(MobileAgent ma){
			this.ma = ma;
		}

		private void sendMessage(int performative, String to, boolean toType, String content){
			ACLMessage r = new ACLMessage(performative);
     		r.addReceiver(new AID(to, toType));//dono do objeto
			r.setContent(content);
			send(r);
		}
		private void checkQueues(){
			//formato: nodo#tempo
			StringTokenizer st = new StringTokenizer(qstatus.get(0),"#");
			String lessTHost = st.nextToken();//quem enviou
			Long lessTime = Long.parseLong(st.nextToken()); //pega o tempo
			String aux="";
			Long t;

			for(int i=0;i<qstatus.size();i++){//caso haja mais de uma resposta, não perde e verifica todas
				st = new StringTokenizer(qstatus.get(i),"#");
				aux = st.nextToken();//quem enviou
				t = Long.parseLong(st.nextToken()); //pega o tempo
				if(t.compareTo(lessTime) < 0){ //se for menor
					lessTime = t;
					lessTHost = aux;
				}
			}//for
			//---------- comparacao com deadline
			if(scheduler == EDF || scheduler == DeadlineMonotonic || scheduler == PreemptiveEDF || scheduler == PreemptiveDM){
				long remainT = (myps - (System.currentTimeMillis()-startTime));
				if(lessTime <= (remainT*0.65)){//se o tempo for maior ou igual a 65% de remainT (ex. remainT = 100ms. less = 60ms = OK)
					//migra pro nodo correspondente
					oldHosts.add(localHost);
					hostToGo=lessTHost;
					//avisa que está partindo
					sendMessage(ACLMessage.INFORM_REF,localHost,AID.ISLOCALNAME, "CHANGING");
					step=7;
					//move(hostToGo);
				}else{
					if(w > remainT)
						step = 6; //senão, volta pra home com status de missão incompleta
					}
				}else{ //se nao tiver deadline
					if(lessTime < (w *0.9)){//se tempo do outro for menor que 90% do tempo daqui (ex. w = 100ms. less = 85ms = OK)
						//migra pro nodo correspondente
						oldHosts.add(localHost);
						hostToGo=lessTHost;
						//avisa que está partindo
						sendMessage(ACLMessage.INFORM_REF,localHost,AID.ISLOCALNAME, "CHANGING");
						step=7;
						//move(hostToGo);
					}
				}
			//-----------
		}//checkqueues

		public void action(){
			try{
			//	//System.out.println(getAID().getName()+"> MEU DEADLINE: "+myps);
                ACLMessage msg = receive();//respostas do servidor
                if(msg!=null){
                	switch(msg.getPerformative()){

                	case ACLMessage.QUERY_IF: //trata pedidos de elemento de prioridade de outros agentes
                		//informa elemento de prioridade
                		try{
                			ACLMessage reply = msg.createReply();
				            reply.setPerformative(ACLMessage.INFORM_IF);
                			//System.out.println(getAID().getName()+"> Informando remainingTime!");
                			if(msg.getContent().equals("EDF") || msg.getContent().equals("PEDF")){//se for EDF ou PEDF
                				long remainingTime = myps - (System.currentTimeMillis()-startTime);
                				reply.setContent(String.valueOf(remainingTime));//informa tempo restante
                			}else{
                				reply.setContent(String.valueOf(myps));//informa elemento de prioridade
                			}
							send(reply);
                		}catch(Exception e){
                			e.printStackTrace();
                		}

                		break;
                	case ACLMessage.PROXY: //possível preempção
                		if(scheduler == PreemptiveEDF){
                			Object[] args = (Object[])msg.getContentObject();
                			ArrayList<maIdent> current_view = new ArrayList<maIdent>();
                			current_view = (ArrayList)args[0];
                			maIdent Aj = (maIdent)args[1];
                			runScheduler(scheduler,current_view,Aj);
                		}
                		break;

					case ACLMessage.PROPOSE: //eu sou o lider
						//System.out.println(getAID().getName()+"> SOU O LIDER");
						Object[] args = (Object[])msg.getContentObject();
						ArrayList<maIdent> current_view = new ArrayList<maIdent>();
						current_view = (ArrayList)args[0];
						//se for uma nova visão, executa o scheduler
						if(((Boolean) args[1]).booleanValue()){
							runScheduler(scheduler,current_view,new maIdent(new AID("NULL", AID.ISGUID)));
						}
						else{
							//envia o primeiro da fila de prioridades para sessão critica
							//System.out.println(getAID().getName()+"> current_view = preview_view. Sending to host: 'send higher Aj to critical section'");
        					ACLMessage reply = msg.createReply();
			   				reply.setPerformative(ACLMessage.INFORM_IF);
							reply.setContent("No new view");
							send(reply);
						}
						break;
                	case ACLMessage.REQUEST: //deve informar a missao
						if(msg.getContent().equals("RESOURCE")){
							//devolver o resource
							ACLMessage reply1 = msg.createReply();
			            	reply1.setPerformative(ACLMessage.QUERY_IF);
							reply1.setContentObject(resource);
							send(reply1);
						}
						if(msg.getContent().equals("MISSION")){
							if(scheduler == SRTF){
								startJob = System.currentTimeMillis();
							}
							ACLMessage reply1 = msg.createReply();
			            	reply1.setPerformative(ACLMessage.QUERY_REF);
							reply1.setContent(request);
							send(reply1);
							//System.out.println(getAID().getName()+"> Missao enviada.");
						}
                		break;

                	case ACLMessage.CONFIRM://resultado da missão
                		//System.out.println(getAID().getName()+"> Recebi resultado da missao de "+msg.getSender().getName());
						Object[] reply = (Object[])msg.getContentObject();
						result = (String)reply[0];
						StringTokenizer st = new StringTokenizer((String)reply[1],":");
						while(st.hasMoreTokens()){
							resource.remove(resource.indexOf(st.nextToken())); //remove recursos ja atendidos
						}
						oldHosts = nodos;
						nodos = (ArrayList)reply[2];

						if(resource.isEmpty()){ //se não houver mais nenhum recurso
							step = 3; //volta pra HOME
						}else{ //senão
							sendMessage(ACLMessage.INFORM_REF,localHost,AID.ISLOCALNAME, "LEAVING");
							/*ACLMessage r = new ACLMessage(ACLMessage.INFORM_REF);
     						r.addReceiver(new AID(localHost, AID.ISLOCALNAME));//dono do objeto
							r.setContent("LEAVING"); //avisa partida
							send(r); */
							step = 0; //vai pro step 0 (escolha de nodo)
						}
                		break;
                	case ACLMessage.REFUSE: //recebe nodos a seguir
                		oldHosts = nodos;
                		nodos = (ArrayList)msg.getContentObject();
                		//escolher um para migrar
                		step = 0;
                		break;
                	case ACLMessage.INFORM://recebe tempo de espera ou situação da fila (tipo_de_inf:Informação)
						StringTokenizer ts = new StringTokenizer(msg.getContent(),":");
						if(ts.nextToken().equals("TWAIT")){ //tempo de espera
							w = Integer.parseInt(ts.nextToken().trim());//tempo de espera
							myTotalTime = Long.parseLong(ts.nextToken().trim());//tempo total que gastará no recurso
							long rt = System.currentTimeMillis()-startTime;
							if(scheduler == EDF || scheduler == DeadlineMonotonic || scheduler == PreemptiveEDF || scheduler == PreemptiveDM){
								//System.out.println(getAID().getName()+"############## MA DEBUG: Tempo estimativo de espera: "+w+" VS "+ (myps - rt));
							//	System.out.println("############## MA DEBUG: RT= "+rt+" VS "+w+" RTT: "+(System.currentTimeMillis()-startTime));
								if(w > -(myps - rt))//se o tempo de espera for maior que o tempo que resta
									{
										System.out.println(getAID().getName()+"> Tempo de espera no recurso ultrapassa meu remaining time!");
										if(nodos.size()==1){//significa que o único nodo que tinha era o que está atualmente
											step = 6; //volta pro home com status de missão incompleta
										}else{
									//perguntar aos outros nodos a situação (w)
									for(int i=0;i<nodos.size();i++){
										if(!(nodos.get(i).equals(localHost))){//se for diferente do nodo que está
											sendMessage(ACLMessage.REQUEST,nodos.get(i),AID.ISLOCALNAME, "QSTATUS");
											/*	ACLMessage r = new ACLMessage(ACLMessage.REQUEST); //requisita situação
     										r.addReceiver(new AID(nodos.get(i), AID.ISLOCALNAME));//dono do objeto
											r.setContent("QSTATUS"); //pede status da fila para outros nodos
											send(r);*/
											}
										}//for
									}//else
								}//if w
							}//if sch
						}else{ //é QSTATUS: situação da fila
							qstatus.add(msg.getSender()+"#"+Long.parseLong(ts.nextToken()));
							checkQueues();
						}

                		break;
                	} //switch
                 }//if
                 else{
                 	block();
                 }
			 }catch(Exception e){
                     e.printStackTrace();
              }
		}

		public void runScheduler(int sch, ArrayList<maIdent> current_view, maIdent Aj) throws Exception{
			//System.out.println(getAID().getName()+"> SCHEDULING....");
			long remainingTime;
			switch(sch){
				case FIFO://1
					Behaviour runFIFO = new FIFO(this.ma,current_view,localHost);//trata requests
					addBehaviour(runFIFO);
					step=5;
					break;
				case EDF://2
					remainingTime = myps - (System.currentTimeMillis()-startTime);//tempo restante pro agente retornar
					//EDF (<my agent instance>,<current view>,<stationary host>)
					Behaviour runEDF = new EDF(this.ma,current_view,localHost,remainingTime);//trata requests
					addBehaviour(runEDF);
					step=5;
					break;
				case LIFO://3
					Behaviour runLIFO = new LIFO(this.ma,current_view,localHost);//trata requests
					addBehaviour(runLIFO);
					step=5;
					break;
				case PreemptiveEDF://4
					remainingTime = myps - (System.currentTimeMillis()-startTime);//tempo restante pro agente retornar
					//PEDF (<my agent instance>,<agent in critical section>,<current view>,<stationary host>)
					Behaviour runPEDF = new PEDF(this.ma,Aj,current_view,localHost,remainingTime);//trata requests
					addBehaviour(runPEDF);
					step=5;
					break;
				case DeadlineMonotonic://5
					Behaviour runDM = new DM(this.ma,current_view,localHost,myps);//trata requests
					addBehaviour(runDM);
					step=5;
					break;
				case PRIO://6
					Behaviour runPRIO = new PRIO(this.ma,current_view,localHost,myps);//trata requests
					addBehaviour(runPRIO);
					step=5;
					break;
				case PreemptivePRIO://7
					Behaviour runPPRIO = new PPRIO(this.ma,Aj,current_view,localHost,myps);//trata requests
					addBehaviour(runPPRIO);
					step=5;
					break;
				case PreemptiveDM://8
					Behaviour runPDM = new PDM(this.ma,Aj,current_view,localHost,myps);//trata requests
					addBehaviour(runPDM);
					step=5;
					break;
				case SJF://9
					Behaviour runSJF = new SJF(this.ma,current_view,localHost);//trata requests
					addBehaviour(runSJF);
					step=5;
					break;
				case SRTF://10
					Behaviour runSRTF = new SRTF(this.ma,Aj,current_view,localHost,(startJob + myTotalTime));//example: Start Job in t=500. My Job Size is 100. Ends in t=600.
					addBehaviour(runSRTF);
					step=5;
					break;
				case RoundRobin:
					Behaviour runRR = new RoundRobin(this.ma,current_view,localHost);//trata requests
					addBehaviour(runRR);
					step=5;
					break;
				default:
					break;
			}
		}//runScheduler
	}//cyclicBehaviour

	 private class actions extends SimpleBehaviour{
   		private static final long serialVersionUID = 1L;
		private int retry;
		private boolean OK=false;

		public boolean done(){
			return _done;
		}

		private String nextHost(){
			return nodos.get((int)(Math.random()*nodos.size())); //retorna um host randomico
		}

		private void sendMessage(int performative, String to, boolean toType, String content){
			ACLMessage r = new ACLMessage(performative);
     		r.addReceiver(new AID(to, toType));//dono do objeto
			r.setContent(content);
			send(r);
		}
		public void action(){

			switch(step){
			/*********** MIGRA PARA UM HOST RANDOMICO DA LISTA **********************/
     			case 0:
     				try{
     					hostToGo=nextHost(); //retorna um host randomico
     					if(hostToGo.equals(HOME)){ //se deve voltar ao nodo HOME
     						step = 3;
              			}else{
             				//System.out.println(getAID().getName()+"> HOST escolhido: "+hostToGo);
							step=2;
							move(hostToGo);
              			}
     				}catch(Exception e){
     					e.printStackTrace();
     				}
                      break;

       		/*********** JA SABE QUAL HOST TEM MENOR FILA, ENTAO, MIGRAR!**********************/
               	case 1:
               			try{
               				//System.out.println(getAID().getName()+">Avisando chegada");
               				ACLMessage r2 = new ACLMessage(ACLMessage.SUBSCRIBE);
                     		r2.addReceiver(new AID(hostToGo, AID.ISLOCALNAME));//dono do objeto
							r2.setContentObject(resource);
							send(r2);
               		 		step = 5;
               			}catch(Exception e){
               				e.printStackTrace();
               			}
               	      break;

      		/********************** ESTA NO HOST AO QUAL MIGROU *******************************/
              	case 2: //compara hostname com nodo ao qual deveria que estar
              			try{
              			//	//System.out.println("#####Entrou no step 2");
              				address = InetAddress.getLocalHost();

              				/*---------------- TRATAMENTO DE ERRO DE MIGRAÇÃO ------------*/
              				if(!(address.getHostName().equals(hostToGo))){

              						//System.out.println("##### Reconheceu diferente");
              					if(this.retry==3){//se ja tentou migrar por tres vezes
              						System.err.println(getLocalName()+"> MOBILE AGENT FAIL: CAN'T MOVE TO "+hostToGo);
              						_done=true;//desiste

              					}else{
              						System.err.println(getAID().getName()+"> FAILED TO MOVE. RETRYING...");
              						this.retry++;
              						step = 0;
              					}
              				}
              				/* -------------------FIM DO TRATAMENTO --------------------*/
              				else{
              					//oldHosts.add(hostToGo);//adiciona a lista de nos visitados
              					localHost = hostToGo; //nome do host
              					this.retry=0;
              					if(localHost.equals(HOME)){ //se voltou ao nodo HOME
              						step = 4;
              					}else{
              					//	System.out.println("\n\n"+getAID().getName()+"> I'm now in "+localHost);
									step = 1;
              					}
              				}
              			}catch(UnknownHostException e){
              				System.err.println("I'm sorry. I don't know my own name.");
              			}
              		  break;
      		/********************** TERMINOU SUA MISSAO *******************************/
      			case 3:
      				System.out.println(getAID().getName()+"> Backing to "+HOME);
      				sendMessage(ACLMessage.INFORM_REF,localHost,AID.ISLOCALNAME, "LEAVING");
      				move(HOME);
      				OK = true;
      				step=4;
      				break;

       		/********************** INFORMA AO HOME RESULTADO DA MISSAO *******************************/
           		case 4:
           		//	//System.out.println(getAID().getName()+"> Informando resultado a "+HOME);
           			long rtt = System.currentTimeMillis()-startTime;
           			ACLMessage r1 = new ACLMessage(ACLMessage.CONFIRM);
     				r1.addReceiver(new AID("N0", AID.ISLOCALNAME));//dono do objeto

           			if(rtt <= myps){
           				if(OK){
           					r1.setContent(startTime+"ms\t\t "+myps+"ms\t\t "+rtt+"ms\t YES\t\t TOTAL*"+user.getName());
							//r1.setContent(getAID().getName()+"> "+result+"\n StartTime:"+startTime+"ms\nDeadline: "+myps+"ms \n RTT: "+rtt+"*"+user.getName());
           				}else{
           					r1.setContent(startTime+"ms\t\t "+myps+"ms\t\t "+rtt+"ms\t YES\t\t HALF*"+user.getName());
           				}

           			}else{
           				  r1.setContent(startTime+"ms\t\t "+myps+"ms\t\t "+rtt+"ms\t NO\t\t NO*"+user.getName());
           				//r1.setContent(getAID().getName()+"> Don't fulfill its deadline\n StartTime:"+startTime+"ms \n Deadline: "+myps+"ms \n RTT: "+rtt+" \n Reply: "+result+"*"+user.getName());
           			}
					send(r1);
					_done=true;
              		System.out.println(getAID().getName()+"> I has terminated my mission");
              		doDelete();
           			break;

           	/******************************** CASE SO PARA TRAVAR **************************************/
          		case 5:
          			//waiting next command....
          			break;
           	/********************** TERMINOU PARCIALMENTE SUA MISSAO *******************************/
      			case 6:
      				System.out.println(getAID().getName()+"> Backing to "+HOME+" with INCOMPLETE status.");
      				sendMessage(ACLMessage.INFORM_REF,localHost,AID.ISLOCALNAME, "LEAVING");
      				move(HOME);
      				OK = false;
      				step=4;
      				break;
			/******************************** CASE SO PARA MOVER **************************************/
			 	case 7:
			 		step=2;
			 		move(hostToGo);
			 		break;
  			}//end switch

		}//action
		private void move(String h){ //verificar isso quando for colocar mais hosts
			try{
				if(!h.equals(HOME))
				{
					StringTokenizer st = new StringTokenizer(h,"@");
					h=st.nextToken();//agente tem mesmo nome de host
					hostToGo = h;

				}
					System.out.println(getAID().getName()+"> Moving to "+h);//TATIANA@TATIANA:1099/JADE
					AID a = new AID("ams@"+h+":1099/JADE",AID.ISGUID);
					a.addAddresses("http://"+h+":7778/acc");
					PlatformID dest1=new PlatformID(a);
					myAgent.doMove(dest1);

			}catch(Exception err){
				err.printStackTrace();
			}


		}//move
     }//class actions
}//MA

