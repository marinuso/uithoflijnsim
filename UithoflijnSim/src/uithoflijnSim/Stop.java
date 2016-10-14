package uithoflijnSim;
import java.util.*;

public class Stop extends UithoflijnObject {

	private String name;
	private boolean dirFromCS;
	
	// passengers who are waiting
	private PriorityQueue<Passenger> passengers;
	
	public Stop(Uithoflijn u, String name, boolean dirFromCS) {
		super(u);
		
		this.name = name;
		this.dirFromCS = dirFromCS;
		
		passengers = new PriorityQueue<Passenger>();
	}
	
	
	
  
}
