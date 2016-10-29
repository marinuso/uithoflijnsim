package uithoflijnSim;
import discreteEventSimulation.*;
import util.Debug;


/* Spawn N trains, each Q secs apart, send them to station.
 * 
 * Between 6-7am, and 7-9.30pm, it spawns only 4 trains an hour. After 9.30pm, it will stop.
 * 6-7am are t=0..t=3600; 7-9.30pm are t=43200..52200
 * */
public class Depot extends UithoflijnObject {

	private int lastTime, interval;
	private final ITrainReceiver spawnpoint;
	
	private final SpawnTrain event;
	
	/**
	 * Depot (train spawner)
	 * 
	 * @param u Uithoflijn
	 * @param interval Interval between trams
	 * @param lastTime time after which it stops
	 * @param spawnpoint Stop where the trains end up
	 */
	public Depot(Uithoflijn u, int interval, int lastTime, ITrainReceiver spawnpoint) throws ScheduleException {
		super(u);
	
		this.interval=interval;
		this.spawnpoint=spawnpoint;
		this.event = new SpawnTrain(this);
		
		uithoflijn.scheduleNow(event);
	}

	public void spawnTrain() throws ScheduleException {
		Train t = new Train(uithoflijn, uithoflijn.getCurrentTime());
		spawnpoint.scheduleTrainArrival(t, 0,  1);
		
		int i = interval;
		if (uithoflijn.getCurrentTime() < UithofTime.t(7)   // ...7 am
		||  uithoflijn.getCurrentTime() >= UithofTime.t(19) // 7 pm...
				) i = 15*60; // 4 an hour
		   
		
		if (uithoflijn.getCurrentTime()<lastTime
		&&  uithoflijn.getCurrentTime()<UithofTime.t(21,30)) uithoflijn.scheduleRelative(i, event);
	}
	
	
}

class SpawnTrain extends Event {
    private final Depot d;
	
    public SpawnTrain(Depot d) {
    	this.d = d;
    }
    
	@Override
	public String description() {
		return "Train spawner.";
	}

	@Override
	public void run() throws SimulationException {
		d.spawnTrain();
	}
	
}
