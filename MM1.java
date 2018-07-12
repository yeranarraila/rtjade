public class MM1{
	private int lambda,miu;
	private double Wg,Wgq,Lg,Lgq,U,Tw,Ts,T,N,Nsigma,En,Er,Enq,Varn;

	public MM1(int lambda, int miu){
		this.lambda = lambda;
		this.miu = miu;
	}

	public double calcU(){//utilização
		this.U = lambda/miu;
		return U;
	}
	public double calcN(){//nro de elementos na fila
		this.N = lambda/(miu - lambda);
		return N;
	}
	public void setN(int N){
		this.N = N;
	}
	public double calcTw(){//espera na fila
		this.Tw = N/miu;
		return Tw;
	}
	public double calcTs(){//tempo de serviço
		this.Ts = 1/miu;
		return Ts;
	}
	public double calcT(){//atraso médio do sistema
		this.T = 1/(miu - lambda);
		return T;
	}
	public double calcNsigma(){ //ocupação media do sistema em funcao do desvio padrao
		this.Nsigma = U + ((Math.pow(U,2))/(1-U));
		return Nsigma;
	}
	public double calcEn(){ //nro medio de jobs no sistema
		this.En = U/(1-U);
		return En;
	}
	public double CalcEr(){ //tempo medio de resposta
		this.Er = (1/miu) / (1-U);
		return Er;
	}
	public double CalcEnq(){ //nro medio de jobs na fila
		this.Enq = (Math.pow(U,2)) / (1 - U);
		return Enq;
	}
	public double CalcVarn(){ //variancia do nro de jobs
		double x = Math.pow((1 - U),2);
		this.Varn = U / x;
		return Varn;
	}
	public void nonPreemptive(){

	}
}