package User;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.lang.String;


public class User extends Agent{

	private static final String N0  = "N0@kokoro-fed232e1:1099/JADE";
	private static final String url = "http://kokoro-fed232e1:7778/acc";

	private int nMA = 5;
	private int reply=0;
    private int Mtotal= 0, Mpartial=0,Mnone=0;
	public User(){
	}

 protected void setup()
    {
	/*	ArrayList<String> resources = new ArrayList<String>();
		resources.add("R1");//Calc Server
		resources.add("R2");//Search in DB
	*/

		AID receiver = new AID(N0,AID.ISGUID);
		receiver.addAddresses(url);
		ACLMessage r = new ACLMessage(ACLMessage.REQUEST); //mensagem do tipo informando request
		r.addReceiver(receiver);//dono do objeto

    		//aguarda requests()
		Scanner read = new Scanner(System.in);
		String request="";
		System.out.println("##################################################");
		System.out.println("SIMULACAO DE COMPORTAMENTO DE 5 AGENTES MOVEIS");
		System.out.println("Escolha uma das seguintes opções:\n");
		System.out.print("1. FIFO\t\t\t\t2. EDF\n3. LIFO\t\t\t\t4. Preemptive EDF\n5. Deadline Monotonic\t\t6. PRIO\n7. Preemptive PRIO");
		System.out.println("\t\t8. Preemptive DM\n9. SJF\t\t\t\t10. SRTF\n11. Round Robin\n");
		System.out.println("##################################################");


			/*
				 * REQUEST:  FORMATO: Requisição#Nro de agentes#Escalonamento#Recurso (separador '#')
				 *
				 *			 Requisicao para 1 agente: separador ':' para n requests. Ex. req1:req2:reqn
				 *           Separando requisicao para n agentes: caracter '|'. Ex. reqAg01|reqAg02|reqAgN
				 *
				 **/

		int sch = read.nextInt();
		request = "UOL|YAHOO|GOOGLE:UOL|TERRA|PGEAS:PUCPR#"+nMA+"#"+sch+"#R2";
		r.setContent(request);

		switch(sch){
			case 1:
				System.out.println("Iniciando Envio de Solicitação FIFO...");
				send(r);
				System.out.println("FIFO enviada");
				break;
			case 2:
				System.out.println("Iniciando Envio de Solicitação EDF...");
				send(r);
				System.out.println("EDF enviada");
				break;
			case 3:
				System.out.println("Iniciando Envio de Solicitação LIFO...");
				send(r);
				System.out.println("LIFO enviada");
				break;
			case 4:
				System.out.println("Iniciando Envio de Solicitação PEDF...");
				send(r);
				System.out.println("PEDF enviada");
				break;
			case 5:
				System.out.println("Iniciando Envio de Solicitação DM...");
				send(r);
				System.out.println("DM enviada");
				break;
			case 6:
				System.out.println("Iniciando Envio de Solicitação PRIO...");
				send(r);
				System.out.println("PRIO enviada");
				break;
			case 7:
				System.out.println("Iniciando Envio de Solicitação PPRIO...");
				send(r);
				System.out.println("PPRIO enviada");
				break;
			case 8:
				System.out.println("Iniciando Envio de Solicitação PDM...");
				send(r);
				System.out.println("PDM enviada");
				break;
			case 9:
				System.out.println("Iniciando Envio de Solicitação SJF...");
				send(r);
				System.out.println("SJF enviada");
				break;
			case 10:
				System.out.println("Iniciando Envio de Solicitação SRTF...");
				send(r);
				System.out.println("SRTF enviada");
				break;
			case 11:
				System.out.println("Iniciando Envio de Solicitação Round Robin...");
				send(r);
				System.out.println("Round Robin enviada");
				break;
			}
		        System.out.print("StartTime\t\t Deadline\t RTT\t Deadline\t Mission\n");

        addBehaviour(new CyclicBehaviour(this)
        {
             public void action()
             {
                ACLMessage msg = receive();
                if (msg!=null) {
                	reply++;
                    System.out.println(msg.getContent() );
                    StringTokenizer st2 = new StringTokenizer(msg.getContent(),"\t\t");
					st2.nextToken(); //startTime
					st2.nextToken(); //deadline
					st2.nextToken(); //RTT
					String accomp = st2.nextToken();
					accomp.trim();

					if(accomp.equals(" YES"))
						Mtotal++;
					else{
						if(accomp.equals(" HALF"))
							Mpartial++;
						else
							Mnone++;
					}


					if(reply == 5){
						System.out.println("\nACCOMPLISH MISSION: \tTOTAL\t PARTIAL\t NOTHING");
                    	System.out.println("                   \t"+Mtotal+"\t\t"+Mpartial+"\t\t"+Mnone);
					}



					//300ms\t YES\t\t TOTAL
                    /*	StringTokenizer st2 = new StringTokenizer(msg.getContent(),"\t");
						st2.nextToken();
						st2.nextToken();

						StringTokenizer st3 = new StringTokenizer(st2.nextToken(),"ms");
						total = total + Integer.parseInt(st3.nextToken().trim());

                    	if(resp == 5){
                    		System.out.println("MEDIA: "+(total/5)+"ms");
                    	} */
                 }else{
                 	block();
                 }
             }
        });
    }
}
