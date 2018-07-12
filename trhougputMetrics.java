
import java.lang.String;

public class trhougputMetrics{

public String name;
public long startTime;
public int dl;

public trhougputMetrics() {}

public trhougputMetrics(String name, int dl, long startTime) {
	this.name = name;
	this.dl = dl;
	this.startTime = startTime;
}

}