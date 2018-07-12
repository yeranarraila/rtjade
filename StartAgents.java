import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;
import java.io.Serializable;


public class StartAgents extends Controller implements Serializable{
	
	protected static Runtime rt = null;
	protected static Profile p = null;
	protected static AgentContainer cc = null;
	
	public void Start()
	{
		//System.out.println("Startando.....");
		
		try
		{
			if (rt == null)
				rt = Runtime.instance();
			if (p == null)
				p = new ProfileImpl("agent.properties"); 
			if (cc == null)
				cc = rt.createMainContainer(p);			 

			Object reference = (Object)this;
			Object args[] = new Object[1];
			args[0] = reference;
			AgentController jadeag;
		
			this.setContainer(cc);
			
			jadeag = cc.createNewAgent(this.getName(), "HOSTS", args);
			jadeag.start();
			//System.out.println("started: " + this.getName());		
				
		
		}
		catch (Exception ex)
		{
			//System.out.println(ex.getMessage());
		}
		
	}

}
