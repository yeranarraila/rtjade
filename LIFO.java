package jade.realtime.scheduler;

import jade.core.behaviours.OneShotBehaviour;
import jade.core.AID;
import jade.core.Agent;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Collections;
import queueUtils.maIdent;

public class LIFO extends OneShotBehaviour{
	private static final long serialVersionUID = 1L;
	private ArrayList<maIdent>    current_view = new ArrayList <maIdent>   ();
	private String host="";

	public LIFO(Agent a,ArrayList<maIdent> current_view, String host){
		super(a);
		this.host = host;
		this.current_view = current_view;
	}

	public void action(){
		System.out.println("LIFO> "+myAgent.getName()+" > Started!");
		schedule();
	}

	private void schedule(){
		try{
			Collections.reverse(current_view);
			ACLMessage r = new ACLMessage(ACLMessage.INFORM);
     		r.addReceiver(new AID(host, AID.ISLOCALNAME));//dono do objeto
			r.setContentObject(current_view);
			myAgent.send(r);
			//System.out.println("LIFO> Done.");

		}catch(Exception e){
			e.printStackTrace();
		}
	}

}