package queueUtils;

import jade.core.AID;
import java.util.ArrayList;

public class maIdent implements java.io.Serializable{
	private AID ma;
	private long W; //wait time
	private ArrayList<rs_ident> resources = new ArrayList<rs_ident>();

	public maIdent(AID ma){
		this.ma = ma;
	}
	public String getName(){
		return this.ma.getName();
	}
	public String getLocalName(){
		return this.ma.getLocalName();
	}
	public AID getAgent(){
		return this.ma;
	}
	public void addRS(String rs, long Ti){
		this.resources.add(new rs_ident(rs,Ti));
	}
	public ArrayList<rs_ident> getResourceList(){
		return this.resources;
	}
	public String getResource(int i){
		return resources.get(i).rs;
	}
	public boolean isEmptyRS(){
		return resources.isEmpty();
	}
	public void setW(long W){
		this.W = W;
	}
	public long getW(){
		return this.W;
	}
	public long totalTime(){ //calcula o tempo total de todas as tarefas daquele agente no nodo corrente
		long total=0;
		for(int i=0;i<resources.size();i++){
			total = total + resources.get(i).Ti;
		}
		return total;
	}
	public String removeRS(int i){
		String r = null;
		try{
			r = getResource(i);
			resources.remove(i);
			return r;
		}catch(Exception e){
			e.printStackTrace();
		}
		return r;
	}
}

class rs_ident implements java.io.Serializable{
	String rs;
	long Ti;
	rs_ident(String rs, long Ti){
		this.Ti = Ti;
		this.rs = rs;
	}
}