package User;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;

import java.util.Scanner;
import java.util.ArrayList;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.lang.String;
import java.util.StringTokenizer;

public class heavyUser extends Agent{

	private ArrayList<String> sitios = new ArrayList<String>();

	private static final String N0  = "N0@kokoro-fed232e1:1099/JADE";
	private static final String url = "http://kokoro-fed232e1:7778/acc";

	private float total=0;
    private int resp=0;
    private int nMA = 25;
    private String request="";

	public heavyUser(){
	}

 protected void setup()
    {

// **********************************************************
    	sitios.add("UOL");
    	sitios.add("AOL");
    	sitios.add("YAHOO");
    	sitios.add("GOOGLE");
    	sitios.add("TERRA");
    	sitios.add("UFSC");
    	sitios.add("PUCPR");
    	sitios.add("PGEAS");
    	sitios.add("ANIMEBLADE");
    	sitios.add("MERCADOLIVRE");
    	sitios.add("GMAIL");
    	sitios.add("ORKUT");
    	sitios.add("YOUTUBE");
// **********************************************************

		for(int i=0;i<nMA;i++){
			if(i != 99)
				request = request + sitios.get((int) (Math.random()*sitios.size()))+":"+sitios.get((int) (Math.random()*sitios.size()))+"|";
			else
				request = request + sitios.get((int) (Math.random()*sitios.size()))+":"+sitios.get((int) (Math.random()*sitios.size()));
		}

		//System.out.println(request);

		AID receiver = new AID(N0,AID.ISGUID);
		receiver.addAddresses(url);
		ACLMessage r = new ACLMessage(ACLMessage.REQUEST); //mensagem do tipo informando request
		r.addReceiver(receiver);//dono do objeto

    			//aguarda requests()
		Scanner read = new Scanner(System.in);

		System.out.println("##################################################");
		System.out.println("SIMULACAO DE COMPORTAMENTO DE "+nMA+" AGENTES MOVEIS");
		System.out.println("Escolha uma das seguintes opções:");
		System.out.println("1. FIFO\n2. EDF\n3. LIFO\n4. Preemptive EDF\n5. Deadline Monotonic\n6. PRIO\n7. Preemptive PRIO");
		System.out.println("##################################################");


			/*
				 * REQUEST:  FORMATO: Requisição#Nro de agentes#Escalonamento#Recurso (separador '#')
				 *
				 *			 Requisicao para 1 agente: separador ':' para n requests. Ex. req1:req2:reqn
				 *           Separando requisicao para n agentes: caracter '|'. Ex. reqAg01|reqAg02|reqAgN
				 *
				 **/

		int sch = read.nextInt();
		switch(sch){
			case 1:
				System.out.println("Iniciando Envio de Solicitação FIFO...");
				request = request+"#"+nMA+"#"+sch+"#R2";
				r.setContent(request);
				send(r);
				System.out.println("FIFO enviada");
				break;
			case 2:
				System.out.println("Iniciando Envio de Solicitação EDF...");
				request = request+"#"+nMA+"#"+sch+"#R2";
				r.setContent(request);
				send(r);
				System.out.println("EDF enviada");
				break;
			case 3:
				System.out.println("Iniciando Envio de Solicitação LIFO...");
				request = request+"#"+nMA+"#"+sch+"#R2";
				r.setContent(request);
				send(r);
				System.out.println("LIFO enviada");
				break;
			case 4:
				System.out.println("Iniciando Envio de Solicitação PEDF...");
				request = request+"#"+nMA+"#"+sch+"#R2";
				r.setContent(request);
				send(r);
				System.out.println("PEDF enviada");
				break;
			case 5:
				System.out.println("Iniciando Envio de Solicitação DM...");
				request = request+"#"+nMA+"#"+sch+"#R2";
				r.setContent(request);
				send(r);
				System.out.println("DM enviada");
				break;
			case 6:
				System.out.println("Iniciando Envio de Solicitação PRIO...");
				request = request+"#"+nMA+"#"+sch+"#R2";
				r.setContent(request);
				send(r);
				System.out.println("PRIO enviada");
				break;
			case 7:
				System.out.println("Iniciando Envio de Solicitação PPRIO...");
				request = request+"#"+nMA+"#"+sch+"#R2";
				r.setContent(request);
				send(r);
				System.out.println("PPRIO enviada");
				break;
		}
		        System.out.print("StartTime\t\t Deadline\t RTT\t Fulfill\n");







        addBehaviour(new CyclicBehaviour(this)
        {

             public void action()
             {


                ACLMessage msg = receive();

                if (msg!=null) {

                    System.out.println(msg.getContent() );
					resp++;
                    	StringTokenizer st2 = new StringTokenizer(msg.getContent(),"\t");
						st2.nextToken();
						st2.nextToken();

						StringTokenizer st3 = new StringTokenizer(st2.nextToken(),"ms");
						total = total + Integer.parseInt(st3.nextToken().trim());

                    //	if(resp == n){
                    		System.out.println("MEDIA: "+(total/nMA)+"ms");
                    //	}


                 }
                 block();
             }
        });
    }
}