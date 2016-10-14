package uithoflijnSim;

/**
 *
 * Passenger on the Uithoflijn
 *
 */
public class Passenger extends UithoflijnObject {
	private Stop origin;
	private Stop destination;
	private Train train;
	
	private int tStart; // arrival at station
	private int tBoard; // boarding time
	private int tArrive; // arrival time
	
	private double tramOccupancy; // amount of people in tram on average
	private int nStops; // amount of stops had
	
	/**
	 * Passenger
	 * 
	 */
	public Passenger(Uithoflijn u, Stop origin, Stop destination) {
		super(u);
		this.origin = origin;
		this.destination = destination;
		
		tramOccupancy = 0;
		nStops = 0;
		
		// passenger arrives at station when he is created
		this.tStart = uithoflijn.getCurrentTime();
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
	public Stop getOrigin() { return origin; }
	
	/**
	 * Destination 
	 */
	public Stop getDestination() { return destination; }
	
	/**
	 * Average crowdedness
	 */
	public double crowdedness() { return tramOccupancy; } 
}
