package uithoflijnSim;
import discreteEventSimulation.*;
import util.Debug;

import java.util.*;

/* Holds the definition of a timetable */

public final class Timetable {
	public final int start, end, freq, q;
	
	public final static int TRIP_CONSTANT = 17; // minutes
	
	private int[] pr_hour_departures; // minutes
	private int[] cs_hour_departures; // minutes
	
	private int nTrams; // amount of trams necessary
	
	public Timetable(int start, int end, int freq, int q) throws SimulationException {
		this.start=start;
		this.end=end;
		this.freq=freq;
		this.q=q;
		
		double mtmin = EndStop.MIN_TURNAROUND_TIME /60.;
				
		if (q<mtmin) 
			throw new SimulationException("Error: q must be at least " + mtmin);
		
		makeTimetable();
	}
	
	
	public int getPRDeparture(int n) {
		return pr_hour_departures[n] * 60; // t is seconds
	}
	
	// make the timetable
	private void makeTimetable() throws SimulationException {
		Debug.out("Making timetable. freq="+freq+" q="+q+"\n");
		pr_hour_departures = new int[freq];
		cs_hour_departures = new int[freq];
		
		for (int i=0; i<freq; i++) {
			pr_hour_departures[i] = (int) ((60./freq)*i);
			Debug.out(" -- departure: " + pr_hour_departures[i] + "\n");
		}
		
		nTrams = 0;
		int round_trip = 2*(TRIP_CONSTANT+q);
		
		Debug.out("round_trip: " + round_trip + "\n");
		
		boolean found = false;
		for (int i=0; i<freq; i++) {
			if (found = pr_hour_departures[i] == round_trip) break;
			nTrams++;
		}
		
		if (!found) {
			// tram cannot depart at P+R departure time
			throw new SimulationException("Cannot create timetable for freq="+freq+ " q="+q);
		}
		
		Debug.out("nTrams: " + nTrams + "\n");
		for (int i=0; i<freq; i++) cs_hour_departures[i] = pr_hour_departures[i] + TRIP_CONSTANT + q;
	}
	
	// get amount of necessary trams
	public int getNTrams() { return nTrams; }
	
	/**
	 * get departures for end stations (starting at P+R)
	 * for a train spawned at t, throughout the simulation
	 * 
	 */
	public LinkedList<Integer> getDepartures(int t, int maxT) {
		Debug.out(" getDepartures(" + t + "," + maxT + ")\n");
		LinkedList<Integer> d = new LinkedList<Integer>();
		
		// find closest match
		int min = UithofTime.minutesFromT(t);
		int nextdepmin = (int) Math.ceil( (freq*min/60.) * (60./freq));
		int nextdepsec = nextdepmin*60;
		
		// starts here
		int startdep = UithofTime.floorMin(t)+nextdepsec;
		
		// needed seconds between departures
		int interval = 60 * (TRIP_CONSTANT + q);
		int dep;
		
		for (dep=startdep; dep<maxT; dep+=interval*2) {
			d.add(dep); // PR ;
			d.add(dep+interval); // CS ;
		}
		
		// add final one
		d.add(dep+2*interval);
		
		return d;
	}
}
