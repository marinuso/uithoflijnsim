package uithoflijnSim;

import java.util.*;
import discreteEventSimulation.*;

public class Train extends UithoflijnObject {

	public final static int CAPACITY = 210*2; // occupants
	public final static int LENGTH = 55; // meters
	
	private ArrayList<Passenger> passengers;
	
	public Train(Uithoflijn u, int prDeparture) {
		super(u);
		
		passengers = new ArrayList<Passenger>();
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
