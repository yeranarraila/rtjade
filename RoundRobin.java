package jade.realtime.scheduler;

import jade.core.behaviours.OneShotBehaviour;
import jade.core.AID;
import jade.core.Agent;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import queueUtils.maIdent;


public class RoundRobin extends OneShotBehaviour{
	private static final long serialVersionUID = 1L;
	private ArrayList<maIdent> current_view = new ArrayList <maIdent>   ();
	private String host="";
	private boolean ok = false;
	private ACLMessage r;

	public RoundRobin(Agent a,ArrayList<maIdent> current_view, String host){
		super(a);
		this.host = host;
		this.current_view = current_view;
	}

	public void action(){
		System.out.println("Round Robin> "+myAgent.getName()+" > Started!");
		this.r = new ACLMessage(ACLMessage.CFP);
     	r.addReceiver(new AID(host, AID.ISLOCALNAME));
		schedule();
	}
	private void sendAgent(){
     	r.setContent("SEND");
     	myAgent.send(r);
	}
	private void suspendAgent(){
     	r.setContent("SUSPEND");
     	myAgent.send(r);
	}

	private void replyDone(){
     	r.setContent("RRDONE");
     	myAgent.send(r);
	}
	private void schedule(){
		try{
			double quantum = (double) 10+(Math.random()*50);//randomic quantum (10ms->60ms)
			while(!ok){
				sendAgent();//send first agent in current_view
     			/*wait a quantum*/
				MessageTemplate mt =
	       				MessageTemplate.and(
	           				MessageTemplate.MatchPerformative(ACLMessage.INFORM_IF),
	           				MessageTemplate.MatchSender(new AID(host, AID.ISGUID)));
				ACLMessage replies = myAgent.blockingReceive(mt,Math.round(quantum)); //wait a quantum
				if(replies == null){//if mobile agent still in view
					suspendAgent();//suspend this agent
				}else{
					current_view.remove(0);//remove this agent from view
					if(current_view.isEmpty()){
						replyDone();
						ok = true;
					}
				}
			}//while
			System.out.println("Round Robin> "+myAgent.getName()+" > Done.");

		}catch(Exception e){
			e.printStackTrace();
		}
	}

}