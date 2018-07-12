package jade.realtime.scheduler;

import jade.core.behaviours.OneShotBehaviour;
import jade.core.AID;
import jade.core.Agent;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.StringTokenizer;
import queueUtils.maIdent;

public class PEDF extends OneShotBehaviour{
	private static final long serialVersionUID = 1L;
	private ArrayList<maIdent>    current_view = new ArrayList <maIdent>   ();
	private ArrayList<String> prioQueue    = new ArrayList <String>();
	private String host="";
	private maIdent Aj;
	private long remainingTime;


	public PEDF(Agent ai, maIdent Aj, ArrayList<maIdent> current_view, String host,long remainingTime){
		super(ai);
		this.Aj = Aj;
		this.current_view = current_view;
		this.host = host;
		this.remainingTime = remainingTime;
	}

	public void action(){
		System.out.println("PEDF> "+myAgent.getName()+" > Started!");
		//verificar antes se é escalonamento ou se é preempção (se Aj startsWith NULL é sch)
		if(!(Aj.getName().startsWith("NULL"))){
			makeQuestion(Aj);
		}else{
			makeQuestion();
			schedule();
		}

	}

	private void makeQuestion(maIdent Aj){
		try{

			System.out.println("PEDF> "+myAgent.getName()+" > trying a preemption with "+Aj.getName()+"\n\n");
			ACLMessage reply;

			ACLMessage r = new ACLMessage(ACLMessage.QUERY_IF);
        	r.addReceiver(Aj.getAgent());
			r.setContent("PEDF");
			myAgent.send(r); //request ps for Aj

			//wait reply

			MessageTemplate mt =
	       			MessageTemplate.and(
	           			MessageTemplate.MatchPerformative(ACLMessage.INFORM_IF),
	           			MessageTemplate.MatchSender(Aj.getAgent()));
	        reply = myAgent.blockingReceive(mt,300); //timeout 300ms

	        if(reply!= null){
				if(reply.getContent()!= null){
					if(Integer.parseInt(reply.getContent()) > remainingTime ) {// se o remaining time de Aj for maior que o meu
						current_view.add(0,Aj);//poe Aj no inicio da fila

						for(int i=0;i<current_view.size();i++){
							if(current_view.get(i).getName().equals(myAgent.getName())){
								//VERIFICAR SE ESSE REMOVE ESTÁ CORRETO: JA NAO SAIU DA FILA ANTES?
								current_view.add(0,current_view.remove(i)); //retira Ai da visão e insere na fila
								break;
							}
						}//for
					}
				}
			}

			//	//System.out.println("PEDF> prioqueue first elm: "+prioQueue.get(0));
			reply = new ACLMessage(ACLMessage.PROXY);
     		reply.addReceiver(new AID(host, AID.ISLOCALNAME));//dono do objeto
			reply.setContentObject(current_view);
			myAgent.send(reply);
			//System.out.println("PEDF> PrioQueue sent to host");

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void schedule(){
		try{
			current_view = new FastQSortAlgorithm().schedule(prioQueue,current_view);

		//	//System.out.println("PEDF> prioqueue first elm: "+prioQueue.get(0));
			ACLMessage r = new ACLMessage(ACLMessage.INFORM);
     		r.addReceiver(new AID(host, AID.ISLOCALNAME));//dono do objeto
			r.setContentObject(current_view);
			myAgent.send(r);
			//System.out.println("EDF> PrioQueue sent to host");

		}catch(Exception e){
			e.printStackTrace();
		}

	}

	private void makeQuestion(){
		AID m_a;
		ACLMessage replies;
		try{
			for(int i=0;i<current_view.size();i++){ //for each Aj on current view do
				m_a = current_view.get(i).getAgent();

				if(m_a.getName().equals(myAgent.getName())){ //se for o proprio agente
					prioQueue.add(i+":"+remainingTime);// insere o proprio remainingTime na posicao do agente na fila:remainingTime
				}else{//senão
					if(m_a.getName().startsWith("NULL")){ //se for 'agente nulo'
						prioQueue.add(i+":100000000000");// insere remainingTime gigante na posicao do agente na fila:remainingTime
					}else{//se não
						//System.out.println("PEDF> "+myAgent.getName()+" Requesting remainingTime to "+m_a.getName());
						ACLMessage r = new ACLMessage(ACLMessage.QUERY_IF);
            			r.addReceiver(new AID(m_a.getName(), AID.ISGUID));
						r.setContent("PEDF");
						myAgent.send(r); //request ps for Aj

						/*wait replies*/
						MessageTemplate mt =
	       					MessageTemplate.and(
	           					MessageTemplate.MatchPerformative(ACLMessage.INFORM_IF),
	           					MessageTemplate.MatchSender(new AID(m_a.getName(), AID.ISGUID)));
						replies = myAgent.blockingReceive(mt,300); //timeout 300ms
						if(replies!= null){
							if(replies.getContent()!= null){
								prioQueue.add(i+":"+replies.getContent());// posicao do agente na fila:remainingTime
							}else{
								prioQueue.add(i+":100000000000");// posicao do agente na fila:remainingTime
							}
						}//	replies = null;
					}//end else02
				}//end else01

			}//end for

		}catch(Exception err){
			err.printStackTrace();
		}
	}
}