package jade.realtime.scheduler;

import jade.core.behaviours.OneShotBehaviour;
import jade.core.AID;
import jade.core.Agent;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Collections;

import queueUtils.maIdent;

public class PRIO extends OneShotBehaviour{
	private static final long serialVersionUID = 1L;
	private ArrayList<maIdent>    current_view = new ArrayList <maIdent>   ();
	private ArrayList<String> prioQueue    = new ArrayList <String>();
	private String host="";
	private long priority;


	public PRIO(Agent a,ArrayList<maIdent> current_view, String host,long priority){
		super(a);
		this.current_view = current_view;
		this.host = host;
		this.priority = priority;//prioridade 0 = menos urgente. prioridade 7 = mais urgente.
	}

	public void action(){
		System.out.println("PRIO> "+myAgent.getName()+" > Started!");
		makeQuestion();
		schedule();
	}

	private void schedule(){
		try{
			current_view = new FastQSortAlgorithm().schedule(prioQueue,current_view);//ordena em ordem crescente de prioridade
			Collections.reverse(current_view);//inverte, pois maior prioridade = maior nro
		//	//System.out.println("PRIO> prioqueue first elm: "+prioQueue.get(0));
			ACLMessage r = new ACLMessage(ACLMessage.INFORM);
     		r.addReceiver(new AID(host, AID.ISLOCALNAME));//dono do objeto
			r.setContentObject(current_view);
			myAgent.send(r);
			//System.out.println("PRIO> PrioQueue sent to host");

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
					prioQueue.add(i+":"+priority);// insere o proprio priority na posicao do agente na fila:priority
				}else{//senão
					if(m_a.getName().startsWith("NULL")){ //se for 'agente nulo'
						prioQueue.add(i+":0");// insere priority 0 na posicao do agente na fila:priority
					}else{//se não
						//System.out.println("PRIO> "+myAgent.getName()+" Requesting priority to "+m_a.getName());
						ACLMessage r = new ACLMessage(ACLMessage.QUERY_IF);
            			r.addReceiver(new AID(m_a.getName(), AID.ISGUID));
						r.setContent("PRIO");
						myAgent.send(r); //request ps for Aj

						/*wait replies*/
						MessageTemplate mt =
	       					MessageTemplate.and(
	           					MessageTemplate.MatchPerformative(ACLMessage.INFORM_IF),
	           					MessageTemplate.MatchSender(new AID(m_a.getName(), AID.ISGUID)));
						replies = myAgent.blockingReceive(mt,300); //timeout 300ms
						if(replies!= null){
							if(replies.getContent()!= null){
								prioQueue.add(i+":"+replies.getContent());// posicao do agente na fila:priority
							}else{
								prioQueue.add(i+":0");// posicao do agente na fila:priority
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