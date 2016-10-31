package uithoflijnSim;
import util.*;

/**
 *
 * Passenger on the Uithoflijn
 *
 */
public class Passenger extends UithoflijnObject {
	private ITrainReceiver origin;
	private ITrainReceiver destination;
	private Train train;
	
	private int tStart; // arrival at station
	private int tBoard; // boarding time
	private int tArrive; // arrival time
	
	private double tramOccupancy; // amount of people in tram on average
	private int nStops; // amount of stops had
	
	private static int passengers = 0;
	
	/**
	 * csv string
	 * @return start,board,arrive,occupancy
	 */
	public String csv() {
		return "" + tStart + "," + tBoard + "," + tArrive + "," + tramOccupancy + "\n";
	}
	
	/**
	 * Passenger
	 * 
	 */
	public Passenger(Uithoflijn u, ITrainReceiver origin, ITrainReceiver destination) {
		super(u);
		this.origin = origin;
		this.destination = destination;
		
		tramOccupancy = 0;
		nStops = 0;
		
		// passenger arrives at station when he is created
		this.tStart = uithoflijn.getCurrentTime();
		
		//Debug.out(" Passenger #" + ++passengers + "\n");
	}
	
	/**
	 * Update tram occupancy
	 */
	public void updateOccupancy() {
		tramOccupancy = (tramOccupancy * nStops + train.occupancy()) / (nStops + 1);
		nStops++;
	}
	
	/**
	 * Board at current time
	 */
	public void board(Train train) {
		tBoard = uithoflijn.getCurrentTime();
		this.train = train;
	}
	
	/**
	 * Arrive at current time
	 */
	public void arrive() {
		tArrive = uithoflijn.getCurrentTime();
	}
	
	/**
	 * Start time
	 */
	public int getStartTime() { return tStart; }
	/**
	 * Boarding time
	 */
	public int getBoardTime() { return tBoard; }
	/**
	 * Arrival time
	 */
	public int getArrivalTime() { return tArrive; }
	
	/**
	 * Origin
	 */
	public ITrainReceiver getOrigin() { return origin; }
	
	/**
	 * Destination 
	 */
	public ITrainReceiver getDestination() { return destination; }
	
	/**
	 * Average crowdedness
	 */
	public double crowdedness() { return tramOccupancy; } 
}
