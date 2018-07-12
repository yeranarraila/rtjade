import java.lang.String;

public class struct_tempo{
	public long starttime,  //tempo de inicio do uso do recurso
			    endtime,    //tempo de término do uso do recurso
			    arrivaltime, //tempo de chegada no nodo
			    conctime,   //arrivaltime - endtime
			    computationtime; //endtime- starttime	
	public String ma_name = "";
	
	public struct_tempo(long st,long et, long at, long ct, long cmt, String ma){
		this.starttime = st;
		this.endtime = et;
		this.arrivaltime = at;
		this.conctime = ct;
		this.computationtime = cmt;
		this.ma_name = ma;		
	}
	
	public struct_tempo(){
	}
	
	public void setStart(long st){
		this.starttime = st;
	}
	
	public void setEnd(long et){
		this.endtime = et;
	}
	
	public void setArrival(long at){
		this.arrivaltime = at;
	}
	
	public void setConc(long ct){
		this.conctime = ct;
	}
	
	public void setComp(long cmt){
		this.computationtime = cmt;
	}
	
	public void setName(String ma){
		this.ma_name = ma;
	}
}