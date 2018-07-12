

public class RunHost{
	private Controller m_agente;

	 public static void main(String[] args)
    {
    	RunHost run = new RunHost();
        //System.out.println("Aplicação executada.");
    }

    public RunHost(){
    	//System.out.println("Iniciando construtor...");
    	Class [] classParm = null;
		Object [] objectParm = null;

		try{
			Class cl = Class.forName("StartAgents");
			java.lang.reflect.Constructor co = cl.getConstructor(classParm);
		    m_agente = (Controller)co.newInstance(objectParm);
		    m_agente.setStart(this);
		    m_agente.Start();

		    //System.out.println("DONE!");
		}catch(Exception e){
				e.printStackTrace();
		}
    }
}