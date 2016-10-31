package uithoflijnSim;

import util.*;
import java.util.*;
import discreteEventSimulation.*;

public class Train extends UithoflijnObject {

	public final static int CAPACITY = 210*2; // occupants
	public final static int LENGTH = 55; // meters
	
	private static int nTrains = 0;
	private int trainN = 0;
	
	public int trainN(){ return trainN; }
	
	private ArrayList<Passenger> passengers;
	
	private LinkedList<Integer> departures; // departures at end stations
	
	private LinkedList<Pair<Integer,Integer>> actualDepartures;
	
	public String csv() {
		StringBuilder sb = new StringBuilder();
		for (Pair<Integer,Integer> p : actualDepartures) {
			sb.append(trainN);
			sb.append(",");
			sb.append(p.getX());
			sb.append(",");
			sb.append(p.getY());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public Train(Uithoflijn u, int prDeparture, LinkedList<Integer> departures) {
		super(u);
		
		trainN = ++nTrains;
		
		
		passengers = new ArrayList<Passenger>();
		this.departures = departures;
		
		if (departures == null) throw new IllegalArgumentException("Departures is null!");

		Debug.out("Train #" + trainN + " created, final departure = " + departures.getLast() + ".\n");

		this.actualDepartures = new LinkedList<Pair<Integer,Integer>>();
	}
	
	public int nextDeparture() { 
		if (departures == null) throw new IllegalStateException("This is exceptionally weird.");
		return departures.peek(); 
	}
	
	public List<Integer> getDepartures() { return departures; }
	
	public boolean isDone() { return departures.size() == 0; } 
	
	public void recordDeparture() {
		int supposed = departures.remove();
		int actual = uithoflijn.getCurrentTime();
		
		Debug.out( "Train #" + trainN + " leaving @ " + actual + " / sched " + supposed + "\n");
		actualDepartures.add(new Pair<>(supposed, actual));
	}
	
	/**
	 * Current occupancy
	 */
	public int occupancy() { return passengers.size(); }
	
	/**
	 * Update all passenger occupancy
	 */
	private void updateOccupancy() {
		for (Passenger p : passengers) p.updateOccupancy();
	}
	
	/**
	 * A passenger boards
	 * @param p
	 */
	public void board(Passenger p) throws SimulationException {
		if (passengers.size() < CAPACITY) {
			p.board(this);
			passengers.add(p);
			updateOccupancy();
		} else {
			throw new SimulationException("Passenger attempted to board full train.");
		}
	}
	
	/**
	 * All passengers disembark
	 */
	public ArrayList<Passenger> disembarkAll() {
		return disembark(true, null);
	}
	
	/**
	 * Passengers disembark at stop
	 */
	public ArrayList<Passenger> disembark(Stop s) {
		return disembark(false, s);
	}
	
	private ArrayList<Passenger> disembark(boolean isEnd, Stop s) {
		ArrayList<Passenger> dis = new ArrayList<Passenger>();
		
		ListIterator<Passenger> iter = passengers.listIterator();
		Passenger p;
		
		while (iter.hasNext()) {
			p = iter.next();
			
			// is p supposed to disembark here?
			if (isEnd || p.getDestination() == s) {
				p.arrive();        // p arrives
				dis.add(p);        // p is added to list of arrivals
				iter.remove();     // p is removed from list of passengers
			}
		}
		
		updateOccupancy();
		return dis;
	}
}
