/*TESTE*/
 import java.util.concurrent.TimeUnit;
 /*END TESTE*/

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.PlatformID;
import jade.core.AID;

import DATABASE.utils.historicalUtils;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Scanner;
import java.util.StringTokenizer;

import java.io.FileReader;
import java.io.IOException;

import queueUtils.maIdent;


import jade.wrapper.*;


public class ServerBehaviours extends CyclicBehaviour{
	private Controller me;
	private String reply="";
	private boolean R1 = true;
	private long troughputStart;
	private int internalDeadline = 5;
	private maIdent Ai;
	private historicalUtils u = new historicalUtils();
	private boolean RoundRobin = false;
	private maIdent nextMA=null;
	/*--------------- TESTE ---------------------------
	private ArrayList<struct_tempo> t = new ArrayList<struct_tempo>();
	/*--------------- END TESTE ---------------------------*/
	private ArrayList<trhougputMetrics> tp = new ArrayList<trhougputMetrics>();

	ServerBehaviours(Agent a,Controller me){
		super(a);
		this.me = me;
	}
/*-----------------------------------------------------------------------------------------------------------*/
/*                         FUNÇÃO QUE CALCULA O TEMPO MÉDIO DE ESPERA DO AGENTE NA FILA                      */
/*-----------------------------------------------------------------------------------------------------------*/

 	private void informWaitTime(maIdent Ai){
		int k = me.getQueue().indexOf(Ai); //pega posicao do agente que acabou de chegar
		long w=0;
		for(int i=0;i<k;i++){ //totalTime = tempo que gastará naquele host, usando os recursos.
			w = w + me.getElement(i).totalTime(); //soma todos os Ti de cada agente móvel na fila
		}
		Ai.setW(w);
		try{
			//informa ao agente seu tempo médio de espera na fila
			ACLMessage r = new ACLMessage(ACLMessage.INFORM);
			r.addReceiver(new AID(Ai.getName(), AID.ISGUID));
			r.setContent("TWAIT: "+Ai.getW()+":"+Ai.totalTime());
			myAgent.send(r);
		}catch(Exception e){
			e.printStackTrace();
		}
 	}
/*-----------------------------------------------------------------------------------------------------------*/
/*                         FUNÇÃO PARA ENVIAR RESPOSTA DA BUSCA NA BASE DE DADOS                             */
/*-----------------------------------------------------------------------------------------------------------*/
	private void sendReply(AID mobileAgent, String rs){
		try{
			Object args[] = new Object[3];
			args[0] = reply;
			args[1] = rs;//por aqui os recursos que foram usados
			args[2] = me.getNodos(); //proximos nodos
			ACLMessage r = new ACLMessage(ACLMessage.CONFIRM);
			r.addReceiver(new AID(mobileAgent.getName(), AID.ISGUID));
			r.setContentObject(args);
			myAgent.send(r);
			//System.out.println(me.getName()+"> Reply enviada para "+mobileAgent.getName());
			reply="";
		}catch(Exception err){
			err.printStackTrace();
		}
	}

/*-----------------------------------------------------------------------------------------------------------*/
/*                         FUNÇÃO QUE VERIFICA SE HÁ O RECURSO DESEJADO                                       */
/*-----------------------------------------------------------------------------------------------------------*/
	private boolean haveResources(AID sender, ArrayList<String> resources){
		try{
				String aux="";
				long Ci;
				//verifica se há o(s) recurso(s)
				Ai = new maIdent(sender);
			//	System.out.println("SERVER > Checando recursos para "+sender.getName());
			//System.out.println("Numero de recursos a ser utilizado por "+sender.getName()+": "+resources.size());
					for(int i=0;i<resources.size();i++){
						aux = resources.get(i);
						System.out.println("RECURSO "+i+": "+aux);
						if(me.getResources().contains(aux)){//se contiver o recurso
							Ci = u.getCimed(aux); //calcula tempo médio de computação com base no historico
							Ai.addRS(aux,Ci); //adiciona recurso + tempo de computação
						}
					}
				if(Ai.isEmptyRS()){ //se não houver nenhum recurso, dispensar o agente
				//	System.out.println("SERVER > Nao ha recursos. Dispensando...");
					return false;
				}else{
					informWaitTime(Ai);
				//	System.out.println("SERVER > Recursos: OK!");
					return true;
				}
		}catch(Exception err){
			err.printStackTrace();
		}
		return false;
	}
/*-----------------------------------------------------------------------------------------------------------*/
/*                         FUNÇÃO QUE INFORMA POSSIVEL PREEMPÇÃO     	                                       */
/*-----------------------------------------------------------------------------------------------------------*/
	private void setPossiblePreemptive(maIdent Ai,maIdent Aj){
		try{
			ACLMessage r = new ACLMessage(ACLMessage.PROXY);
			Object[] args = new Object[2];
			args[0] = me.getQueue();
			args[1] = Aj;
			r.setContentObject(args);
        	r.addReceiver(new AID(Ai.getName(), AID.ISGUID));
			myAgent.send(r);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
/*-----------------------------------------------------------------------------------------------------------*/
/*                         FUNÇÃO QUE INFORMA LIDER						                                       */
/*-----------------------------------------------------------------------------------------------------------*/
	private void informLeader(){
		try{
			if(me.getSuspended() != null){
				me.resume(me.getSuspended());	//ativa o agente que estiver suspenso
				me.setSuspended(null);
			}
			if(RoundRobin){
				me.setCurrentView(me.getQueue());       //current_view = Queue
				System.out.println("Nova visão agora tem tamanho: "+me.m_viewSize());
				System.exit(0);
			}
		//	me.setCurrentView(me.getQueue());       //current_view = Queue


			Object[] args = new Object[2];
			args[0] = me.getQueue();
			args[1] = me.getNewView();
			System.out.println(me.getName()+"> Lider "+me.getLeader().getName()+" escolhido. Comunicando-o...");
			ACLMessage r = new ACLMessage(ACLMessage.PROPOSE);
			r.setContentObject(args);
        	r.addReceiver(new AID(me.getLeader().getName(), AID.ISGUID));
			myAgent.send(r);
		}catch(Exception e){
			e.printStackTrace();
		}

	}
/*-----------------------------------------------------------------------------------------------------------*/
/*                         FUNÇÃO QUE BUSCA REQUEST NA BASE DE DADOS                                         */
/*-----------------------------------------------------------------------------------------------------------*/
	private void searchQuery(String request){
		String aux="";
			try {
				 String rs="";
	 			 FileReader file = new FileReader( "url.database" );
        		 Scanner in = new Scanner( file );

         		 while (in.hasNext()) {//03
      		     	rs = in.nextLine();
      		     	StringTokenizer st = new StringTokenizer(request,":");//separa o request, se houver mais de um query
					while(st.hasMoreTokens()){//02
						aux = st.nextToken();	//joga na var aux
						if (rs.contains(aux)) {//se no database contiver a query
							StringTokenizer t = new StringTokenizer(rs,aux+"=");
							while(t.hasMoreTokens()){//01
							//	rs = t.nextToken();//retira-a
							//	//System.out.println(rs);
								reply = reply+" \n "+t.nextToken();
							}//01
						}
					}//02
         	     }//03
          ////System.out.println(reply);
            in.close();
    	} catch (IOException e) {}
	}
/*-----------------------------------------------------------------------------------------------------------*/
/*   						 TRATAMENTO DE CHEGADA DE MENSAGENS 											 */
/*-----------------------------------------------------------------------------------------------------------*/
	public void action(){
		ACLMessage msg = myAgent.receive();//recebe mensagens do agente movel
		String higher="";
		int pos;
		AID p_higher;
		maIdent current=null;
		boolean locked=false;
        if (msg!=null) {

        	switch(msg.getPerformative()){ //ve que tipo de mensagem é

			 /*############################################################*/
           	/*      				CHEGOU AGENTE MÓVEL				       */
           /*############################################################*/
			case ACLMessage.SUBSCRIBE:
				try{
					this.troughputStart = System.currentTimeMillis();
					if(internalDeadline < 30){
						tp.add(new trhougputMetrics(msg.getSender().getLocalName(),internalDeadline,System.currentTimeMillis()));
						internalDeadline+=5;
					}else{
						internalDeadline=5;
						tp.add(new trhougputMetrics(msg.getSender().getLocalName(),internalDeadline,System.currentTimeMillis()));
						internalDeadline+=5;
					}

					/*----------------- TESTE -------------------------
					t.add(new struct_tempo(0,0,System.currentTimeMillis(),0,0,msg.getSender().getName()));
					/*----------------- END TESTE -------------------------*/

					if(haveResources(msg.getSender(),(ArrayList)msg.getContentObject())){ //true
					//	System.out.println("SERVER > Ha recursos. Setando nova visao...");
						me.setNewView(true); //newView = true
						//adicionar struct junto, na fila
						me.addToQueue(Ai); //enqueue(Queue,Ai)

						if(me.getCurrentMA()!= null){ //if (has a Aj in critical section)
							setPossiblePreemptive(Ai,me.getCurrentMA()); //Ai.setPossiblePreemptive(Aj)
						}

					////System.out.println("############# SERVER DEBUG: Preview="+me.viewIsEmpty("preview_view")+" LEADER="+me.getLeader().getName()+" LOCKED="+locked+"QUEUE(0)="+me.getElement(0));

						if(R1 && me.getLeader().getName().startsWith("NULL") && locked == false){ // se for a primeira execução
							locked = true;
							me.chooseLeader("QUEUE"); //escolha um lider
							informLeader();
							locked=false;
							R1 = false;
						}
						Ai=null;
				}//haveResources
				else{
					//dispensar o agente movel
					System.out.println("SERVER > REQUESTED RESOURCES NOT FOUND!");
					ACLMessage reply1 = msg.createReply();
			        reply1.setPerformative(ACLMessage.REFUSE);
					reply1.setContentObject(me.getNodos()); //informa os nodos interligados para o agente poder prosseguir
					myAgent.send(reply1);
				}

				}catch(Exception e){
					e.printStackTrace();
				}

				break;
           /*############################################################*/
           	/*       LIDER ENVIA AGENTE MOVEL COM MENOR DEADLINE         */
           /*############################################################*/

        	case ACLMessage.INFORM: //houve scheduling
        		try{
        				me.setCurrentView((ArrayList<maIdent>)msg.getContentObject());  //pega a visao atual ordenada
        				current = me.getViewElement(0);
        				p_higher = current.getAgent();//agente c maior prioridade
        				if(p_higher != null){ //se nao for nulo
        					higher = p_higher.getName();

        					if(me.getCurrentMA() == null && higher != null){
        						nextMA = me.removeViewElement(0);//agente c maior prioridade
        						for(int i=0;i<me.m_queueSize();i++){
        							if(p_higher.getName().equals(me.getElement(i).getName())){//encontra-o na fila
        								me.removeQueueElement(i);
        								break;
        							}
        						}
        						/*------------------- TESTE -----------------------
        							for(int i=0;i<t.size();i++){
        								if(higher.equals(t.get(i).ma_name)){
        								//System.out.println("ON CS> ACHOU NO STRUCT!");
        									t.get(i).setStart(System.currentTimeMillis());
        									break;
        								}
        							}
        						/*------------------- END TESTE -----------------------*/

							//	System.out.println("QUANDO HOUVE SCHEDULING: "+current.getName());
        						me.setCurrentMA(current);
        						ACLMessage reply = new ACLMessage(ACLMessage.REQUEST);
        						reply.addReceiver(new AID(higher, AID.ISGUID));//dono do objeto
								reply.setContent("MISSION");
								myAgent.send(reply);	//requisita missão ao agente movel
								//System.out.println(me.getName()+"> Pedido de missao enviado a "+higher);
        					}

        				}

        		}catch(Exception e){
        			e.printStackTrace();
        		}

        	break;
        	/*############################################################*/
           	/*       LIDER ENVIA AGENTE MOVEL COM MENOR DEADLINE         */
           /*############################################################*/

        	case ACLMessage.INFORM_IF: //nao houve scheduling
        		try{
        			//System.out.println(me.getName()+"> Nao houve novo scheduling. Solicitando missao...");
        			current = me.getViewElement(0);
        			p_higher = current.getAgent();//agente c maior prioridade
        			if(p_higher != null){ //se nao for nulo
        				higher = p_higher.getName();

        				 if(me.getCurrentMA()==null && higher != null){
        				 	nextMA = me.removeViewElement(0);//agente c maior prioridade é REMOVIDO da visão!

        				 	for(int i=0;i<me.m_queueSize();i++){
        						if(p_higher.getName().equals(me.getElement(i).getName())){//encontra-o na fila
        							me.removeQueueElement(i);//agente c maior prioridade é REMOVIDO da fila!
        						break;
        						}
        					}

        				 	/*------------------- TESTE -----------------------
        							for(int i=0;i<t.size();i++){
        								if(higher.equals(t.get(i).ma_name)){
        									//System.out.println("ON CS> ACHOU NO STRUCT!");
        									t.get(i).setStart(System.currentTimeMillis());
        									break;
        								}
        							}
        					/*------------------- END TESTE -----------------------*/
						//	System.out.println("QUANDO NAO HOUVE SCHEDULING: "+current.getName());
        					me.setCurrentMA(current);
        					ACLMessage reply = new ACLMessage(ACLMessage.REQUEST);
        					reply.addReceiver(new AID(higher, AID.ISGUID));//dono do objeto
							reply.setContent("MISSION");
							myAgent.send(reply);	//requisita missão ao agente movel
							//System.out.println(me.getName()+"> Pedido de missao enviado a "+higher);
        				}
        			}

        		}catch(Exception e){
        			e.printStackTrace();
        		}

        	break;

        	/*############################################################*/
           	/*       			On Round Robin Scheduling			     */
           /*############################################################*/
            case ACLMessage.CFP:
            	try{
            		RoundRobin = true;
            		if(me.viewIsEmpty("current_view")){
            			me.setCurrentView(me.getQueue());       //current_view = Queue
						System.out.println("Nova visão agora tem tamanho: "+me.m_viewSize());
            		}
            		ContainerController cc = myAgent.getContainerController();
            		AgentController ac;
            		//suspende o agente movel corrente
            		String action = msg.getContent();//pega o tipo de acao

					/*=========== ROUND ROBIN TERMINOU SUA EXECUÇÃO ==================*/

					if(action.equals("RRDONE")){

						me.setCurrentMA(null);//não há mais agente móvel no recurso
        				me.setLeader(new maIdent(new AID("NULL", AID.ISLOCALNAME)));//não há mais líder
        				me.updateStatus(false);

        				me.setPreviewView(me.getCurrentView()); //preview_view = current_view
        				me.setCurrentView(me.getQueue());       //current_view = Queue

						RoundRobin = false;

        				if(!me.viewIsEmpty("current_view")){//se houver agentes na visão
        					me.chooseLeader("current_view"); 		//chooseLeader(current_view)
        					informLeader();
        					me.setNewView(false);					//newView = false
        				} else{
        					R1 = true;
        				}
					}
            		/*========= ROUND ROBIN SOLICITA SUSPENSÃO DE UM AGENTE APÓS O QUANTUM  ======*/
            		if(action.equals("SUSPEND")){ //se for para suspender um agente
            			if(me.getCurrentMA() != null){ //se tiver um agente no recurso
               				AID currentMA = me.getCurrentMA().getAgent(); //pega o agente que será suspenso (este agente é o que está no recurso)
                		//if(currentMA != null){
                   			System.out.println("##### SOLICITANDO CONTROLLER DE : "+currentMA.getLocalName());
				   			ac = cc.getAgent(currentMA.getLocalName());
				   			//me.setSuspended(ac);//supende-o
				   			ac.suspend();//suspende-o
				   			me.addToView(me.getCurrentMA()); //devolve-o para a última posição da visão
            			    me.addToQueue(me.getCurrentMA()); //devolve-o para a última posição da fila
				  		 	me.setCurrentMA(null);
                 		}else{//senão, deve avisar que o agente já não faz parte da visão
                 			ACLMessage reply = msg.createReply();
				            reply.setPerformative(ACLMessage.INFORM_IF);
				            reply.setContent("maDONE");
				            myAgent.send(reply);
                			//me.removeViewElement(1);//remove da visao o agente que estava no recurso
                 		}
            		}//if SUSPEND

            		/*=========== ROUND ROBIN SOLICITA ENVIO DO AGENTE ==================*/
            		if(action.equals("SEND")){
            				current = me.getViewElement(0);//pega o primeiro agente
        					p_higher = current.getAgent();//agente c maior prioridade
        					if(p_higher != null){ //se nao for nulo
        						higher = p_higher.getName();

        				 		if(me.getCurrentMA()==null && higher != null){
        				 			me.removeViewElement(0);//remove da visão agente c maior prioridade

        				 			for(int i=0;i<me.m_queueSize();i++){
        								if(p_higher.getName().equals(me.getElement(i).getName())){//encontra-o na fila
        									me.removeQueueElement(i); //remove da fila
        								break;
        								}
        							}//for

        				 	/*------------------- TESTE -----------------------
        							for(int i=0;i<t.size();i++){
        								if(higher.equals(t.get(i).ma_name)){
        									//System.out.println("ON CS> ACHOU NO STRUCT!");
        									t.get(i).setStart(System.currentTimeMillis());
        									break;
        								}
        							}
        					/*------------------- END TESTE -----------------------*/

        						me.setCurrentMA(current);
        						ac = cc.getAgent(p_higher.getLocalName());
        						ac.activate();//ativa o agente

        						ACLMessage reply = new ACLMessage(ACLMessage.REQUEST);
        						reply.addReceiver(new AID(higher, AID.ISGUID));//dono do objeto
								reply.setContent("MISSION");
								myAgent.send(reply);	//requisita missão ao agente movel
							//System.out.println(me.getName()+"> Pedido de missao enviado a "+higher);
        					}
        				}
					}

            	}catch(Exception e){
            		e.printStackTrace();
            	}

            	break;

        	/*############################################################*/
           	/*       			On leave of an Ai     				     */
           /*############################################################*/
        	case ACLMessage.INFORM_REF:
        		long endThroughputTime = System.currentTimeMillis();
        		for(int i=0;i<tp.size();i++){
        			if(tp.get(i).name.equals(msg.getSender().getLocalName())){
        				if((endThroughputTime - troughputStart) < tp.get(i).dl){
        						System.out.println("===============> CUMPRIU! SYSTEM RUNNING TIME: "+(endThroughputTime - troughputStart)+" ms");
        				}
        			}
        		}






        		/*------------------- TESTE -----------------------

					try{
						for(int i=0;i<t.size();i++){
        					if(msg.getSender().getName().equals(t.get(i).ma_name)){
        						t.get(i).setEnd(System.currentTimeMillis());
        						t.get(i).setConc((t.get(i).endtime)-(t.get(i).arrivaltime));
        						t.get(i).setComp((t.get(i).endtime)-(t.get(i).starttime));
								//grava no BD
								u.setValues(t.get(i).ma_name,t.get(i).arrivaltime,t.get(i).starttime,t.get(i).endtime, t.get(i).conctime, t.get(i).computationtime);//t.get(i).computationtime%1000);
        						//remove nodo de tempo
        						t.remove(i);
        					break;//para o loop
        					}
        				}
					}catch(Exception e){
						e.printStackTrace();
					}

        		/*------------------- END TESTE -----------------------*/
        		if(msg.getContent().equals("LEAVING")){//agente esta deixando o host
        			if(!RoundRobin){ //se nao for o escalonamento round robin
        				//System.out.println("NAO E RR");
        				me.setCurrentMA(null);//nao ha mais agente no recurso
        				me.setLeader(new maIdent(new AID("NULL", AID.ISLOCALNAME))); //nao ha mais lider
        				me.updateStatus(false);

        				for(int i=0;i<me.m_queueSize();i++){
        					if(msg.getSender().getName().equals(me.getElement(i).getName())){
        						me.removeQueueElement(i); //remove da fila o elemento que saiu
        						break;
        					}
        				}
        				me.setPreviewView(me.getCurrentView()); //preview_view = current_view
        				me.setCurrentView(me.getQueue());       //current_view = Queue

						/*--------------------DEBUG -------------------------*/
					//	System.out.println("############# SERVER DEBUG: MA que saiu: "+msg.getSender().getName()+"\nSTATUS DA VISÃO: ");

					/*	for(int i=0;i<me.m_viewSize();i++){
							System.out.println("############# SERVER DEBUG: "+me.getViewElement(i).getName());
						} */

        				if(!me.viewIsEmpty("current_view")){
        			//		System.out.println("Visao não é vazia!");
        					me.chooseLeader("current_view"); 		//chooseLeader(current_view)
        					informLeader();
        					me.setNewView(false);					//newView = false
        				} else{
        					R1 = true;
        				}
        			}//NOT RR
        		}//LEAVING
        		if(msg.getContent().equals("CHANGING")){//mudou de nodo
        		System.out.println("Agente Movel "+msg.getSender()+" ira mudar de nodo");
        			me.setChanged(true);
        			for(int i=0;i<me.getQueue().size();i++){
        				if(me.getQueue().get(i).getName().equals(msg.getSender().getName())){
        					me.removeQueueElement(i); //remove da fila o elemento que saiu
        				}
        				if(me.getCurrentView().get(i).getName().equals(msg.getSender().getName())){
        					me.removeViewElement(i);       //atualiza a visão
        					me.setNewView(true); //newView = true
        					break;
        				}
        			}//END FOR
        		}//END IF (CHANGING)

        	//		//System.out.println("############# SERVER DEBUG: Preview="+me.viewIsEmpty("preview_view")+" LEADER="+me.getLeader().getName()+" LOCKED="+locked);
        		break;

           /*############################################################*/
           /*       AGENTE MOVEL REQUISITA BUSCA NA BASE DE DADOS        */
           /*############################################################*/

        	case ACLMessage.QUERY_REF: //informa o que deve ser buscado na base de dados
        		me.setLocked();
        	//	System.out.println("O agente "+msg.getSender().getName()+" Requisita busca. \nNextMA: "+nextMA.getName());
				me.setCurrentMA(nextMA);
        		try{
        			double start=0.0;
        			double end=0.0;
        			String r = "";
        			String allRS = "";
        			while(!me.getCurrentMA().isEmptyRS()){
        				r = me.getCurrentMA().removeRS(0);
        				if(allRS.equals("")){
        					allRS= r;
        				}else{
        					allRS= allRS+":"+r; //mapeia os recursos que serão usados
        				}

						if(r.equals("R1")){
							start=System.currentTimeMillis();//inicio
							int calculo = (int)Math.random()*3000;//calculo qualquer so para simulação
							end = System.currentTimeMillis();//fim
							reply = reply+"\n Calculos: "+calculo+"\n";
						}
						if(r.equals("R2")){
							//buscar no BD o que deve ser achado
							start=System.currentTimeMillis();//inicio
        					searchQuery(msg.getContent());
        					end = System.currentTimeMillis();//fim
						}
        			}//while
					sendReply(msg.getSender(),allRS);
					u.setResourceCT(r,end-start);//grava no BD
        		}catch(Exception e){
        			e.printStackTrace();
        		}finally{
        			me.setUnlocked();
        		}
        	break;

        	 /*############################################################*/
           	/*       LIDER ENVIA AGENTE MOVEL COM PREEMPÇÃO               */
           /*############################################################*/

        	case ACLMessage.PROXY: //houve scheduling
        		try{
        				me.setCurrentView((ArrayList<maIdent>)msg.getContentObject());  //pega a visao atual ordenada
        				current = me.getViewElement(0);//pega primeiro elemento da fila (o que deve ir primeiro)
        				p_higher = current.getAgent();
        				//suspende o agente movel corrente
                        ContainerController c = myAgent.getContainerController();
                        AID cMA = me.getCurrentMA().getAgent();

                        if(cMA != null){
                           		System.out.println("##### SOLICITANDO CONTROLLER DE : "+cMA.getLocalName());
						   		AgentController ac = c.getAgent(cMA.getLocalName());
						   		me.setSuspended(ac);
						   		//ac.suspend();
						   		me.setCurrentMA(null);
                           }else{
                           		me.removeViewElement(1);//remove da visao o agente que estava no recurso
                        }

        				if(p_higher != null){ //se nao for nulo
        					higher = p_higher.getName();

        				if(me.getCurrentMA()==null && higher != null){
        					me.removeViewElement(0);//agente c maior prioridade
        					for(int i=0;i<me.m_queueSize();i++){
        						if(p_higher.getName().equals(me.getElement(i).getName())){//encontra-o na fila
        							me.removeQueueElement(i);
        							break;
        						}
        					}
        					me.setCurrentMA(current);
        					ACLMessage reply = new ACLMessage(ACLMessage.REQUEST);
        					reply.addReceiver(new AID(higher, AID.ISGUID));//dono do objeto
							reply.setContent("MISSION");
							myAgent.send(reply);	//requisita missão ao agente movel
							//System.out.println(me.getName()+"> Pedido de missao enviado a "+higher);
        					}
        				}

        		}catch(Exception e){
        			e.printStackTrace();
        		}

        	break;
        	/*############################################################*/
           	/*         AGENTE MOVEL SOLICITA ESTADO DA FILA              */
           /*############################################################*/
			case ACLMessage.REQUEST:
				//irá informar tempo estimado de espera caso este agente seja inserido na fila
				long status=0;
				//soma o tempo de todos os agentes na visão atual
				for(int i=0;i<me.m_queueSize();i++){
					status = status + me.getElement(i).totalTime();
				}
				ACLMessage reply = msg.createReply();
        		reply.setPerformative(ACLMessage.INFORM);
				reply.setContent("QSTATUS:"+status);
				myAgent.send(reply);	//requisita missão ao agente movel
				break;

        	}//switch

        }else{
        	block();
        }
	}//action
}