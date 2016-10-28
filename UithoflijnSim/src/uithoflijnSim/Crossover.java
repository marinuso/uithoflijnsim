package uithoflijnSim;
import discreteEventSimulation.*;
import java.util.*;
import util.*;

/**
 * 
 * Train crossover: 
 * 
 *  --->---  inA ->-. .-<-  inB ---<---
 *                   X
 *  ---<--- outB -<-' '->- outA --->---
 *  
 *  Only one train can pass at once. Trains will be processed
 *  in order of arrival at both inputs. After crossing one
 *  at a time, the trains will be sent to the outputs as normal.
 *  
 *  It does *NOT* take into account travel time (except for the
 *  40 second safety interval) - this should be done by lengthening
 *  the incoming or outgoing tracks. 
 */

public class Crossover extends UithoflijnObject {
	
	private CrossoverInput inputA, inputB;
	
	private ITrainReceiver outputA=null, outputB=null;
	private int outputAdist, outputBdist;
    private double outputAspeed, outputBspeed;
	private String name;
	
	protected int nextArrivalTimeA;
	protected int nextArrivalTimeB;
	
	private LinkedList<Pair<Train, Output>> trains;
	
	private void checkIfSet(ITrainReceiver o) {
		if (o == null) throw new IllegalStateException(description() + ": output not set");
	}
	
	public Crossover(Uithoflijn u, String name) {
		super(u);
		this.name=name;
		
		nextArrivalTimeA = 0;
		nextArrivalTimeB = 0;
		
		inputA = new CrossoverInput(this, Output.A);
		inputB = new CrossoverInput(this, Output.B);
		
		trains = new LinkedList<Pair<Train, Output>>(); 
	}
	
	protected void addTrain(Train t, Output output) throws ScheduleException {
		trains.add(new Pair<>(t, output));
		
		// if this is the only train, schedule transit
		if (trains.size() == 1) uithoflijn.scheduleNow(new Transit(this));
	}
	
	protected void scheduleTrainArrival(Train t, int distance, double avgSpeed, Output o) throws ScheduleException {
		/* generate next arrival time from distribution */
		int time = (int) (avgSpeed*uithoflijn.getTravelSpeedDistribution().sampleSum(distance));
		int absTime = uithoflijn.getCurrentTime() + time;
		
		/* take into account that trains cannot skip each other,
		 * the minimum arrival time is the arrival time of the last train
		 * plus the safety margin
		 */
		
		int nextArrivalTime;
		
		if (o == Output.A) nextArrivalTime = nextArrivalTimeA;
		else if (o == Output.B) nextArrivalTime = nextArrivalTimeB;
		else throw new IllegalStateException("Invalid output!");
		
		int minNextArrival = nextArrivalTime + Stop.SAFETY_MARGIN;
		nextArrivalTime = Math.max(minNextArrival, absTime);
		
		if (o == Output.A) nextArrivalTimeA = nextArrivalTime;
		else if (o == Output.B) nextArrivalTimeB = nextArrivalTime;
		
		/* actually schedule the train arrival */
		uithoflijn.scheduleAbsolute(nextArrivalTime,
				new CrossTrainArrival(this, t, o));
	}
	
	protected void transit() throws ScheduleException {
		if (trains.size() != 0) {
			// get current train
			Pair<Train,Output> t = trains.remove();
			
			// schedule its arrival at whatever's connected to its output
			ITrainReceiver o=null;
			int d=0;
			double s=0;
			
			if (t.getY() == Output.A) {
				o=outputA; d=outputAdist; s=outputAspeed;
			} else if (t.getY() == Output.B) {
				o=outputB; d=outputBdist; s=outputBspeed;
			} else {
				throw new IllegalStateException("Invalid output!"); 
			}
			
			o.scheduleTrainArrival(t.getX(), d, s);
			
			if (trains.size() != 0) {
				// if there are trains left, schedule another transit after the
				// safety interval
				uithoflijn.scheduleRelative(Stop.SAFETY_MARGIN, new Transit(this));
			}
		}
	}
	
	public ITrainReceiver inputA() {
		return inputA;
	}
	
	public ITrainReceiver inputB() {
		return inputB;
	}
	
	public void setOutputA(ITrainReceiver o, int distance, double avgSpeed) {
		outputA = o;
		outputAdist = distance;
		outputAspeed = avgSpeed;
	}
	
	public void setOutputB(ITrainReceiver o, int distance, double avgSpeed) {
		outputB = o;
		outputBdist = distance;
		outputBspeed = avgSpeed;
	}
	
	public int getOutputADistance() {
		checkIfSet(outputA);
		return outputAdist;
	}
	
	public ITrainReceiver getOutputA() {
		checkIfSet(outputA);
		return outputA;
	}
	
	public double getOutputASpeed() {
		checkIfSet(outputA);
		return outputAspeed;
	}
	
	public int getOutputBDistance() {
		checkIfSet(outputB);
		return outputBdist;
	}
	
	public int getOutputB() {
		checkIfSet(outputB);
		return outputBdist;
	}
	
	public double getOutputBSpeed() {
		checkIfSet(outputB);
		return outputBspeed;
	}
	
	public String description() {
		return "Crossover " + name;
	}

}

enum Output { A, B }

class CrossoverInput implements ITrainReceiver {

	Crossover crossover;
	Output output;
	
	public CrossoverInput(Crossover c, Output o) {
		crossover=c;
		output=o;
	}
	
	@Override
	public void scheduleTrainArrival(Train t, int distance, double avgSpeed) throws ScheduleException {
		crossover.scheduleTrainArrival(t, distance, avgSpeed, output);
	}
	
}

class CrossTrainArrival extends Event {

	Crossover crossover;
	Output output;
	Train train;
	public CrossTrainArrival(Crossover c, Train t, Output o) {
		super();
		
		crossover=c;
		output=o;
		train=t;
	}
	
	@Override
	public String description() {
		return "[TrainArrival]";
	}

	@Override
	public void run() throws SimulationException {
		crossover.addTrain(train,  output);
	}
	
}

class Transit extends Event {

	Crossover crossover;
	public Transit(Crossover c) {
		super();
		this.crossover = c;
	}
	
	@Override
	public String description() {
		return "[Transit] " + crossover.description();
	}

	@Override
	public void run() throws SimulationException {
		crossover.transit();
	}
	
}