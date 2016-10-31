package uithoflijnSim;
import discreteEventSimulation.*;
import util.Debug;

import java.util.*;

public class Stop extends UithoflijnObject implements ITrainReceiver {
	
	public final static int SAFETY_MARGIN = 40; // 40 seconds
	
	private String name;
	private boolean dirFromCS;
	
	// trains that are waiting
	private LinkedList<Train> trains;
	private int nextArrivalTime; 
	
	// passengers who are waiting
	private LinkedList<Passenger> passengers;
	
	// next stop
	private ITrainReceiver nextStop = null;
	private int nextStopDistance;
	private double avgSpeed; // seconds per meter
	
	private boolean isTurnaroundStop;
	
	private void checkNextStopSet() {
		// check if stop has been set
		if (nextStop == null) {
			throw new IllegalStateException(description() + " No next stop set.");
		}		
	}
	
	public int getNextStopDistance() {
		checkNextStopSet();
		return nextStopDistance;
	}
	
	public ITrainReceiver getNextStop() { 
		checkNextStopSet();
		return nextStop;
	}
	
	public double getAvgSpeed() {
		checkNextStopSet();
		return avgSpeed;
	}
	
	public void setNextStop(ITrainReceiver nextStop, int distance, double avgSpeed) {
		this.nextStop = nextStop;
		this.nextStopDistance = distance;
		this.avgSpeed = avgSpeed;
	}
	
	public Stop(Uithoflijn u, String name, boolean dirFromCS, boolean isTurnaroundStop) throws ScheduleException {
		super(u);
		
		this.name = name;
		this.dirFromCS = dirFromCS;
		
		passengers = new LinkedList<Passenger>();
		trains = new LinkedList<Train>();
		
		nextArrivalTime = 0;
		
		this.isTurnaroundStop = isTurnaroundStop;
		
		schedulePassengerArrival();
	}
	
	public void addPassenger(Passenger p) {
		passengers.add(p);
	}
	
	protected void addTrain(Train t) throws ScheduleException {
		trains.add(t);
		
		// if this is the only train, dwell.
		if (trains.size() == 1) uithoflijn.scheduleNow(new Dwell(this));
	}
	
	/**
	 * Schedule train arrival 
	 * 
	 * @param t Train
	 * @param distance Distance in meters
	 */
	public void scheduleTrainArrival(Train t, int distance, double avgSpeed) throws ScheduleException {
		
		/* generate next arrival time from distribution */
		int time = (int) (avgSpeed*uithoflijn.getTravelSpeedDistribution().sampleSum(distance));
		int absTime = uithoflijn.getCurrentTime() + time;
		
		/* take into account that trains cannot skip each other,
		 * the minimum arrival time is the arrival time of the last train
		 * plus the safety margin
		 */
		
		int minNextArrival = nextArrivalTime + SAFETY_MARGIN;
		nextArrivalTime = Math.max(minNextArrival, absTime);
		
		/* actually schedule the train arrival */
		uithoflijn.scheduleAbsolute(nextArrivalTime,
				new TrainArrival(this, t));
		
	}
	
	// schedule passenger arrival
	public void schedulePassengerArrival() throws ScheduleException {
		double meanArrival = uithoflijn.getInputModel().meanPassengerArrivalTime(uithoflijn, name, dirFromCS);
		if (meanArrival > 3600*24) {
			// no more passengers - check back in a minute
			//uithoflijn.scheduleRelative(60, new PassengerArrival(this,true));
			return;
		}
	
		int nextArrival = DistribUtil.poisson(meanArrival);
	    uithoflijn.scheduleRelative(nextArrival, new PassengerArrival(this,false));		
	}
	
	
	// dwell
	public void dwell() throws SimulationException {
		if (trains.size() == 0) return; // no trains
		
		// take train from queue
		Train train = trains.remove();
		
		// remove passengers
		ArrayList<Passenger> disembarked = train.disembark(this);
		uithoflijn.addServedPassengers(disembarked);
		
		// add passengers
		
		// amount of passengers we can add
		int freeSpaces = Train.CAPACITY - train.occupancy();

		int boarded = 0;
		while (freeSpaces > 0 && passengers.size() != 0) {
			train.board(passengers.remove());
			boarded++;
			freeSpaces--;
		}
		
		// schedule train departure after set time
		double meanDwell = 12.5 + 0.22*boarded + 0.13*disembarked.size();
		double gammaDwell = DistribUtil.gamma2(meanDwell);
		
		// if this is an end station, then we need to take the turnaround time into account
		if (isTurnaroundStop) gammaDwell = Math.max(gammaDwell, uithoflijn.getTurnaroundTime());
		
		// schedule train departure after set time
		uithoflijn.scheduleRelative((int) gammaDwell, new TrainDeparture(this, train));
		
	}
	
	// schedule another dwell right now if there are still trains left in the queue
	public void dwellAgainIfNecessary() throws ScheduleException {
		if (! trains.isEmpty()) uithoflijn.scheduleNow(new Dwell(this));
	}
	
	public String description() {
		return "[Stop] " + name + " towards " + (dirFromCS ? "P+R" : "CS");
	}
	
	public String name() { return name; }
	public boolean dirFromCS() { return dirFromCS; }
}

class TrainArrival extends Event {
	Stop stop;
	Train train;
	
	public TrainArrival(Stop s, Train t) {
		super();
		stop=s; train=t;
	}
	
	public String description() {
		return "[Arrival] " + stop.description();
	}
	
	public void run() throws SimulationException {
		stop.addTrain(train);
	}
}

class Dwell extends Event {
	Stop stop;
	
	public Dwell(Stop s) {
		super();
		stop=s;
	}
	
	public String description() {
		return "[Dwell] " + stop.description();
	}
	
	public void run() throws SimulationException {
		stop.dwell();
	}
	
}

class TrainDeparture extends Event {
	Stop stop;
	Train train;
	
	public TrainDeparture(Stop s, Train t) {
		super();
		stop=s;
		train=t;
	}
	
	public String description() {
		return "[Departure] " + stop.description();
	}

	public void run() throws SimulationException {
		//Debug.out("Train #" + train.trainN() 
		//  +" leaving from " + stop.description() + " @ " + train.uithoflijn.getCurrentTime() + "\n");
		
		
		ITrainReceiver ts = stop.getNextStop();
		int dst = stop.getNextStopDistance();
		double avgSpeed = stop.getAvgSpeed();
		ts.scheduleTrainArrival(train, dst, avgSpeed);
		
		// if still trains left in queue, dwell again
		stop.dwellAgainIfNecessary();
	}
		
}

class PassengerArrival extends Event {
	Stop stop;
	private final boolean keepalive;
	public PassengerArrival(Stop s, boolean keepalive) {
		super();
		stop=s;
		this.keepalive=keepalive;
	}
	
	public String description() {
		return "[PassengerArrival] " + stop.description();
	}

	public void run() throws SimulationException {
		if (!keepalive) {
			// generate a passenger
			Passenger p = stop.uithoflijn.getInputModel().generatePassenger(stop.uithoflijn, stop.name(), stop.dirFromCS());
			// add it to the stop
			stop.addPassenger(p);
		}
		// schedule next
		stop.schedulePassengerArrival();
	}
}
