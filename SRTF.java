package jade.realtime.scheduler;

import jade.core.behaviours.OneShotBehaviour;
import jade.core.AID;
import jade.core.Agent;

import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.Collections;
import queueUtils.maIdent;

public class SRTF extends OneShotBehaviour{
	private static final long serialVersionUID = 1L;
	private ArrayList<maIdent>	current_view = new ArrayList <maIdent>   ();
	private ArrayList<String> 	prioQueue    = new ArrayList <String>();
	private String host		=	"";
	private maIdent Aj  	=   null;
	private long myJobSize 	= 0;


	public SRTF(Agent a,maIdent Aj, ArrayList<maIdent> current_view, String host, long myJobSize){
		super(a);
		this.host = host;
		this.Aj	  = Aj;
		this.current_view  = current_view;
		this.myJobSize = myJobSize;
	}

	public void action(){
		System.out.println("SRTF> "+myAgent.getName()+" > Started!");
		//verificar antes se é escalonamento ou se é preempção (se Aj startsWith NULL é sch)
		if(!(Aj.getName().startsWith("NULL"))){
			makeQuestion(Aj);
		}else{
			schedule();
		}
	}
	private void makeQuestion(maIdent Aj){
		if(Aj.totalTime()<(myJobSize - System.currentTimeMillis())){//example: ends in t=600. Now is t=550, so need more 50. if Aj.totalTime = 40, it's preemptive.
			try{
				ACLMessage reply;
				current_view.add(0,Aj);//retorna currentMA para inicio da fila
				for(int i=0;i<current_view.size();i++){
					if(current_view.get(i).getName().equals(myAgent.getName())){
						current_view.add(0,current_view.remove(i)); //retira Ai da visão e insere no início da fila
						break;
					}
				}//for
				reply = new ACLMessage(ACLMessage.PROXY);
     			reply.addReceiver(new AID(host, AID.ISLOCALNAME));//dono do objeto
				reply.setContentObject(current_view);
				myAgent.send(reply);
			}catch(Exception e){
				e.printStackTrace();
			}
		}//if
	}
	private void requestJobSize(){
		for(int i=0;i<current_view.size();i++){
			prioQueue.add(i+":"+current_view.get(i).totalTime());//fatia de tempo total ue irá utilizar
		}
	}
	private void schedule(){
		try{
			requestJobSize();
			current_view = new FastQSortAlgorithm().schedule(prioQueue,current_view);

			ACLMessage r = new ACLMessage(ACLMessage.INFORM);
     		r.addReceiver(new AID(host, AID.ISLOCALNAME));//dono do objeto
			r.setContentObject(current_view);
			myAgent.send(r);
			//System.out.println("SRTF> Done.");

		}catch(Exception e){
			e.printStackTrace();
		}
	}


}