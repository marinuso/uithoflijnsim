package uithoflijnSim;
import discreteEventSimulation.*;
import util.*;
import java.util.*;

/* Spawn N trains, each Q secs apart, send them to station.
 * 
 * Between 6-7am, and 7-9.30pm, it spawns only 4 trains an hour. After 9.30pm, it will stop.
 * 6-7am are t=0..t=3600; 7-9.30pm are t=43200..52200
 * */
public class Depot extends UithoflijnObject {

	private final int lastTime; //, interval;
	private final EndStop spawnpoint;
	//private final SpawnTrain event;
	private LinkedList<Train> trains;
	
	private LinkedList<Train> startTrains;
	
	private final Timetable timetable;
	
	public LinkedList<Train> finishedTrains() {
		return trains;
	}
	
	/**
	 * Depot (train spawner)
	 * 
	 * @param u Uithoflijn
	 * @param interval Interval between trams
	 * @param lastTime time after which it stops
	 * @param spawnpoint Stop where the trains end up
	 */
	public Depot(Uithoflijn u, int lastTime, 
			 EndStop spawnpoint, Timetable timetable) 
	throws ScheduleException {
		super(u);
	
		//this.interval=interval;
		this.spawnpoint=spawnpoint;
		
		this.trains = new LinkedList<Train>();
		this.startTrains = new LinkedList<Train>();
		this.timetable = timetable;
		this.lastTime = lastTime;
		
		genStartTrains();
	}

	public void trainDone(Train t) {
		Debug.out("Train #" + t.trainN() + " back at depot @ " + uithoflijn.getCurrentTime() + "\n");
		trains.add(t);
	}
	
	// generate the starting trains
	private void genStartTrains() {
		// make the trains according to the timetable
		int nTrains = timetable.getNTrams();
		for (int i=0; i<nTrains; i++) {
			int dep = timetable.getPRDeparture(i);
			startTrains.add(new Train(uithoflijn, dep, timetable.getDepartures(dep, lastTime)));
		}
	}

	public boolean hasTrain() { return !startTrains.isEmpty(); }
	
	// end stop will call this when ready for next train. 
	public void nextTrainReady(EndStop e) throws SimulationException {
		if (startTrains.isEmpty()) return;
		
		Train t = startTrains.remove();
		Debug.out("Placing train #" + t.trainN() + " @ " + t.uithoflijn.getCurrentTime() + "\n");
		e.placeTrain(t);
	}
}


