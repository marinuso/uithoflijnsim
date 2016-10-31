package uithoflijnSim;

import discreteEventSimulation.*;
import util.*;
import java.util.*;

/* The stop at the end of the line, including a crossover.
 * 
 * 
 * +----------.---.-<<<-A--
 *   [---]a    \ /
 *              X
 *   [---]b    / \
 * +----------'---'->>>-B--
 * 
 * 
 * Trains that want to enter will wait at place A.
 * Once one of the stops is free, the train will enter that stop.
 * (This means there can be two trains in the stop at once.)
 * When a train leaves, a new one will be allowed in.
 * Only one train can pass the crossover at once, in either direction.
 * 
 */

public class EndStop extends UithoflijnObject implements ITrainReceiver {

	/* minimum turnaround time */
	public final static int MIN_TURNAROUND_TIME = 60*3;
	
	/* state */
	protected Train posATrain = null; // pos a in diagram, null=empty, otherwise there's a train
	protected Train posBTrain = null; // pos b in diagram
	
	protected boolean posADepartureReady = false;
	protected boolean posBDepartureReady = false;
	
	protected LinkedList<Train> trains; // trains waiting at position A
	protected int nextArrivalTime = 0;  // keep track of arrival time so trains can't queue-jump
	
	protected ITrainReceiver nextStop; // whatever is connected to position B
	protected int nextStopDistance;    // distance in meters to whatever is connected to B
	protected double avgSpeed;         // average speed with which avgSpeed
	
	protected LinkedList<Passenger> passengers; // passengers who are waiting
	
	private final String name;
	private final boolean dirFromCS; // this means where the passengers will be traveling to 
	
	protected boolean crossoverOccupied = false; // whether crossover is OK.
	
	private Depot depot; // depot, if there is one.
	
	protected EndStop(Uithoflijn u, String name, boolean dirFromCS) throws SimulationException {
		this(u, name, dirFromCS, null);
	}
	
	/**
	 *  place a train on a free spot, if there is a free spot, without blocking.
	 *  (this is for 'spawning' trains at the depot)
	 */
	public void placeTrain(Train t) throws SimulationException {
		// is there space?
		if (!hasFreePosition()) throw new IllegalStateException("Cannot place train, no space.");
		
		// place train in space
		Position p = getFreePosition();
		setTrainAtPos(p, t);
		
		// dwell immediately (this'll take care of its departure &c.)
		dwell(p, true);
	}

	protected EndStop(Uithoflijn u, String name, boolean dirFromCS, Depot depot) 
	throws SimulationException {
		super(u);
		
		this.name = name;
		this.dirFromCS = dirFromCS;
		trains = new LinkedList<Train>();
	
		this.depot = depot;
		this.passengers = new LinkedList<Passenger>();
		
		if (depot != null) {
			// ask it for the first train
			depot.nextTrainReady(this);
		}
	}
	
	public void setDepot(Depot depot) throws SimulationException {
		if (this.depot != null) throw new IllegalStateException("Cannot reset depot.");
		this.depot = depot;
		depot.nextTrainReady(this);
	}
	
	public void setNextStop(ITrainReceiver nextStop, int distance, double avgSpeed) {
		this.nextStop=nextStop;
		this.nextStopDistance=distance;
		this.avgSpeed=avgSpeed;
	}
	
	// schedule passenger arrival
	public void schedulePassengerArrival() throws ScheduleException {
		double meanArrival = uithoflijn.getInputModel().meanPassengerArrivalTime(uithoflijn, name, dirFromCS);
		if (meanArrival > 3600*24) { // no more passengers
			// check back in a minute
			//uithoflijn.scheduleRelative(60, new ESPassengerArrival(this, true));
			return;
		}
		int nextArrival = DistribUtil.poisson(meanArrival);
	    uithoflijn.scheduleRelative(nextArrival, new ESPassengerArrival(this, false));		
	}
	
	public void addPassenger(Passenger p) {
		passengers.add(p);
	}
	
	public String name() { return name; }
	public boolean dirFromCS() { return dirFromCS; }
	
	public String description() {
		return "[EndStop] " + name;
	}
	
	public boolean hasFreePosition() { return posATrain==null || posBTrain==null; }
	public Position getFreePosition() {
		if (posATrain==null) return Position.A;
		if (posBTrain==null) return Position.B;
		return null;
	}
	
	protected Train getTrainAtPos(Position x) {
		if (x==Position.A) return posATrain;
		if (x==Position.B) return posBTrain;
		throw new IllegalStateException("Invalid position: " + x);
	}
	
	private void setTrainAtPos(Position x, Train t) {
		if (x==Position.A) posATrain=t;
		else if (x==Position.B) posBTrain=t;
		else throw new IllegalStateException("Invalid position: " + x);
	}
	
	private boolean posDepartureReady(Position x) {
		if (x==Position.A) return posADepartureReady;
		if (x==Position.B) return posBDepartureReady;
		throw new IllegalStateException("Invalid position: " + x);
	}
	
	private void setDepartureReady(Position x, boolean ready) {
		if (x==Position.A) posADepartureReady=ready;
		else if (x==Position.B) posBDepartureReady=ready;
		else throw new IllegalStateException("Invalid position: " + x);
	}

	protected void addTrain(Train t) throws SimulationException {
		trains.add(t);
		// if this is the only waiting train, and a position is free, pick a side and dwell
		// a position only frees up if the previous train has passed through the crossover.
		
		if (trains.size() == 1 && hasFreePosition() && !crossoverOccupied) {
			// we can go into the crossover
			uithoflijn.scheduleNow(new ESTransitCrossover(this));
		}
	}

	protected void transitCrossover() throws ScheduleException {
		if (trains.size() == 0) {
			crossoverOccupied = false;
			return; // no trains
		}
		
		if (!hasFreePosition()) {
			throw new IllegalStateException("No free position for incoming train.");
		}
		
		crossoverOccupied = true;
		
		// in SAFETY_MARGIN, unblock the crossover to potentially allow a new train in
		uithoflijn.scheduleRelative(Stop.SAFETY_MARGIN, new ESUnblockCrossover(this));
		
		// take the train and put it in the free spot
		Train t = trains.remove();
		Position p = getFreePosition();
		setTrainAtPos(p,t);
		uithoflijn.scheduleRelative(Stop.SAFETY_MARGIN, new ESDwell(this, p));
		
	}
	
	protected void unblockCrossover() throws SimulationException {
		// unblock the crossover
		crossoverOccupied = false;
		
		// if a train is waiting to leave at position A, let it depart
		if (posDepartureReady(Position.A)) {
			uithoflijn.scheduleNow(new ESTrainDeparture(this, Position.A));
		}
		// if a train is waiting to leave at position B, let *it* depart
		else if (posDepartureReady(Position.B)) {
			uithoflijn.scheduleNow(new ESTrainDeparture(this, Position.B));
		}
		// if a train is waiting to enter, and there is a position free, let it in
		else if (hasFreePosition()) {
			if (depot != null && depot.hasTrain()) depot.nextTrainReady(this);
			else uithoflijn.scheduleNow(new ESTransitCrossover(this));
		}
		
		// and if nothing, just leave it unblocked and wait for a new train to arrive
	}
	
	protected void dwell(Position p) throws SimulationException {
		dwell(p,false);
	}
	
	private void dwell(Position p, boolean isPlacedTrain) throws SimulationException {
		// calculate when it's due to leave		
		Train train = getTrainAtPos(p);
		
		// remove passengers
		ArrayList<Passenger> disembarked = train.disembarkAll();
		uithoflijn.addServedPassengers(disembarked);
		
		// if the train is done, remove it.
		if (train.isDone() || (train.getDepartures().size() == 1 && depot!=null)) {
			if (depot == null) { // if train doesn't end at beginning...
				throw new SimulationException("Train ended up at CS! - @ " + uithoflijn.getCurrentTime());
			}
			// remove train from tracks
			setDepartureReady(p, false);
			setTrainAtPos(p, null);
			
			// send it back to the depot
			depot.trainDone(train);
			
			return;
		}
		
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
		
		double turnaround = uithoflijn.getTurnaroundTime();
		
		
		turnaround = Math.max(turnaround, MIN_TURNAROUND_TIME);
		
		// trains placed by the depot are a special case.
		if (! isPlacedTrain) gammaDwell = Math.max(gammaDwell, turnaround);
		
		int minLeaveTime = (int) (uithoflijn.getCurrentTime() + gammaDwell);
		
		int shouldLeaveTime = train.nextDeparture();
		
		int departureTime = Math.max(shouldLeaveTime, minLeaveTime);
		
		// this position is not ready to leave
		setDepartureReady(p, false);
		
		Debug.out("Train #" + train.trainN() + " scheduled to leave from " + description() + " @ " + departureTime + "\n");
		// schedule train departure after set time
		uithoflijn.scheduleAbsolute(departureTime, new ESDepartureReady(this, p));
	}
	
	protected void departureReady(Position p) throws SimulationException {
		setDepartureReady(p, true);
		
		// if the crossing is free, block it and have the train depart
		if (!crossoverOccupied) uithoflijn.scheduleNow(new ESTrainDeparture(this, p));
	}
	
	protected void depart(Position p) throws SimulationException {
		
		// schedule the arrival
		nextStop.scheduleTrainArrival(getTrainAtPos(p), nextStopDistance, avgSpeed);
		getTrainAtPos(p).recordDeparture();
		// block the crossing
		crossoverOccupied = true;
		// remove the train from the position
		setDepartureReady(p, false);
		setTrainAtPos(p, null);
		

		// unblock the crossing after safety margin
		uithoflijn.scheduleRelative(Stop.SAFETY_MARGIN, new ESUnblockCrossover(this));
	}
	
	@Override
	public void scheduleTrainArrival(Train t, int distance, double avgSpeed) throws ScheduleException {
		// schedule the train to arrive at position A
		
		/* generate next arrival time from distribution */
		int time = (int) (avgSpeed*uithoflijn.getTravelSpeedDistribution().sampleSum(distance));
		int absTime = uithoflijn.getCurrentTime() + time;
		
		/* take into account that trains cannot skip each other,
		 * the minimum arrival time is the arrival time of the last train
		 * plus the safety margin
		 */
		
		int minNextArrival = nextArrivalTime + Stop.SAFETY_MARGIN;
		nextArrivalTime = Math.max(minNextArrival, absTime);
		
		/* actually schedule the train arrival */
		uithoflijn.scheduleAbsolute(nextArrivalTime,
				new ESTrainArrival(this, t));
	}

}

enum Position {A, B};

class ESDepartureReady extends Event {
	private final EndStop es;
	private final Position p;
	public ESDepartureReady(EndStop es, Position p) {
		this.es=es; this.p=p;
	}
	public String description() { return "ESDepartureReady"; }
	public void run() throws SimulationException { es.departureReady(p); }
}

class ESTransitCrossover extends Event {
	private final EndStop es;
	
	public ESTransitCrossover(EndStop es) {
		this.es=es;
	}
	
	public String description() { return "ESTransitCrossover"; }
	public void run() throws SimulationException {
		es.transitCrossover();
	}
}

class ESUnblockCrossover extends Event {
	private final EndStop es;
	public ESUnblockCrossover(EndStop es) { this.es=es; }
	public String description() { return "ESUnblockCrossover"; }
	public void run() throws SimulationException {
		es.unblockCrossover();
	}
}

class ESDwell extends Event{
	private final EndStop es;
	private final Position p;
	
	public ESDwell(EndStop es, Position p) {
		this.es=es;
		this.p=p;
		
		if (p==null) throw new IllegalStateException("Cannot dwell w/o position.");
	}
	
	public String description() {
		return "Dwell at pos " + p.toString();
	}
	
	public void run() throws SimulationException {
		es.dwell(p);
	}
}

class ESTrainArrival extends Event {

	private final EndStop es;
	private final Train t;
	
	public ESTrainArrival(EndStop es, Train t) {
		this.es=es;
		this.t=t;
	}
	
	public String description() {
		return "Train arrival at end point";
	}

	@Override
	public void run() throws SimulationException {
		es.addTrain(t);
	}	
}

class ESTrainDeparture extends Event {
	private final EndStop es;
	private final Position p;
	
	public ESTrainDeparture(EndStop es, Position p) {
		this.es=es;
		this.p=p;
	}
	
	@Override
	public String description() {
		return "Train departure";
	}

	@Override
	public void run() throws SimulationException {
		Debug.out("Train leaving from " + es.description() + " @ " + es.uithoflijn.getCurrentTime() + "\n");
		
		StringBuilder s = new StringBuilder();
		for (int i : es.getTrainAtPos(p).getDepartures()) s.append(","+i);
		Debug.out("left "+s+"\n");
		es.depart(p);
	}
	
}

class ESPassengerArrival extends Event {
	EndStop stop;
	private final boolean keepalive;
	
	public ESPassengerArrival(EndStop s, boolean keepalive) {
		super();
		stop=s;
		this.keepalive = keepalive;
	}
	
	public String description() {
		return "[ESPassengerArrival] " + stop.description();
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