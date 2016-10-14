package uithoflijnSim;
import discreteEventSimulation.*;
import java.util.*;

public class Stop extends UithoflijnObject {
	private String name;
	private boolean dirFromCS;
	
	// trains that are waiting
	private LinkedList<Train> trains;
	
	// passengers who are waiting
	private LinkedList<Passenger> passengers;
	
	public Stop(Uithoflijn u, String name, boolean dirFromCS) {
		super(u);
		
		this.name = name;
		this.dirFromCS = dirFromCS;
		
		passengers = new LinkedList<Passenger>();
		trains = new LinkedList<Train>();
	}
	
	public void addPassenger(Passenger p) {
		passengers.add(p);
	}
	
	public void addTrain(Train t) throws ScheduleException {
		trains.add(t);
		
		// if this is the only train, dwell.
		if (trains.size() == 1) uithoflijn.scheduleNow(new Dwell(this));
	}
	
	// schedule passenger arrival
	public void schedulePassengerArrival() throws ScheduleException {
		double meanArrival = uithoflijn.getInputModel().meanPassengerArrivalTime(uithoflijn, name, dirFromCS);
		if (meanArrival == 0) return; // no more passengers
	
		int nextArrival = DistribUtil.poisson(meanArrival);
	    uithoflijn.scheduleRelative(nextArrival, new PassengerArrival(this));		
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
		// TODO: 
		double meanDwell = 12.5 + 0.22*boarded + 0.13*disembarked.size();
		double gammaDwell = DistribUtil.gamma2(meanDwell);
		
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
		return "[Departure] " + stop.description();
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
		// TODO: schedule arrival at next station
		
		// if still trains left in queue, dwell again
		stop.dwellAgainIfNecessary();
	}
		
}

class PassengerArrival extends Event {
	Stop stop;
	
	public PassengerArrival(Stop s) {
		super();
		stop=s;
	}
	
	public String description() {
		return "[PassengerArrival] " + stop.description();
	}

	public void run() throws SimulationException {
		// generate a passenger
		Passenger p = stop.uithoflijn.getInputModel().generatePassenger(stop.uithoflijn, stop.name(), stop.dirFromCS());
		// add it to the stop
		stop.addPassenger(p);
		// schedule next
		stop.schedulePassengerArrival();
	}
}
