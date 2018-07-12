package jade.realtime.scheduler;

import jade.core.behaviours.OneShotBehaviour;
import jade.core.AID;
import jade.core.Agent;

import jade.lang.acl.ACLMessage;

import java.util.ArrayList;

import queueUtils.maIdent;

public class SJF extends OneShotBehaviour{
	private static final long serialVersionUID = 1L;
	private ArrayList<maIdent>    current_view = new ArrayList <maIdent>   ();
	private ArrayList<String> prioQueue    = new ArrayList <String>();
	private String host="";

	public SJF(Agent a,ArrayList<maIdent> current_view, String host){
		super(a);
		this.host = host;
		this.current_view = current_view;
	}

	public void action(){
		System.out.println("SJF> "+myAgent.getName()+" > Started!");
		schedule();
	}

	private void schedule(){
		try{
			requestJobSize();
			current_view = new FastQSortAlgorithm().schedule(prioQueue,current_view);

			ACLMessage r = new ACLMessage(ACLMessage.INFORM);
     		r.addReceiver(new AID(host, AID.ISLOCALNAME));//dono do objeto
			r.setContentObject(current_view);
			myAgent.send(r);
			//System.out.println("SJF> Done.");

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void requestJobSize(){
		for(int i=0;i<current_view.size();i++){
			prioQueue.add(i+":"+current_view.get(i).totalTime());//fatia de tempo total ue irá utilizar
		}
	}

}