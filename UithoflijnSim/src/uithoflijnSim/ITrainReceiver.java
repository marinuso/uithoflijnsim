package uithoflijnSim;

import discreteEventSimulation.*;

/**
 * An object that can receive a train
 *
 */
public interface ITrainReceiver {
	
	/**
	 * Schedule the arrival of a train.
	 * 
	 * @param t The train
	 * @param distance The distance, in meters, to this stop
	 */
	public void scheduleTrainArrival(Train t, int distance) throws ScheduleException;
	

}
