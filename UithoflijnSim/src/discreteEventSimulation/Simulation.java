package discreteEventSimulation;

import java.util.*;

import util.Debug;

/**
 * Discrete event simulation framework
 *
 */
public abstract class Simulation {
	protected int currentTime; // current time of simulation
	protected LinkedList<ScheduledEvent> schedule; // schedule
	
	public Simulation() {
		Debug.out("Simulation init.\n");
		currentTime = 0;
		schedule = new LinkedList<ScheduledEvent>();
	}
	
	/**
	 * Get current simulation timestamp 
	 */
	public int getCurrentTime() {
		return currentTime;
	}
	
	
	/**
	 * Schedule an event to run right now
	 * @param event
	 */
	public void scheduleNow(Event event) throws ScheduleException {
		scheduleAbsolute(currentTime, event);
	}
	
	/**
	 * Schedule an event after a certain time has passed
	 * @param timestamp
	 * @param event
	 */
	public void scheduleRelative(int timestamp, Event event) throws ScheduleException {
		scheduleAbsolute(timestamp+currentTime, event);
	}
	
	/**
	 * Schedule an event at a certain time
	 * @param timestamp
	 * @param event
	 */
	public void scheduleAbsolute(int timestamp, Event event) throws ScheduleException {
		scheduleAbsolute(new ScheduledEvent(timestamp, event));
	}
	
	protected void scheduleAbsolute(ScheduledEvent se) throws ScheduleException {
		//Debug.out("Schedule: " + se.description() + "\n");
		// cannot schedule in the past
		if (se.getTimestamp() < currentTime) throw new ScheduleException(se, currentTime);
		
		// schedule it to run after all other element w/same time
		ListIterator<ScheduledEvent> iter = schedule.listIterator();
		ScheduledEvent cur;
		while (iter.hasNext()) {
			cur = iter.next();
			if (cur.getTimestamp() > se.getTimestamp()) {
				// found the event this one should go in front of
				iter.previous();
				iter.add(se);
				return;
			}
		}
		
		// we're at the end and haven't added it, so add it at the end
		iter.add(se);
	}
	
	
	/**
	 * has next event?
	 */
	public boolean hasNextEvent() {
		return !schedule.isEmpty();
	}
	
	/**
	 * run the next event
	 */
	public void runNextEvent() throws SimulationException {

		
		if (schedule.isEmpty()) throw new SimulationException("Empty event list.");
		
		ScheduledEvent se = schedule.removeFirst();
		currentTime = se.getTimestamp();
		
		//Debug.out("Event: " + se.description() + " @ " + currentTime + "\n");
		
		se.run();
	}
	
	/** 
	 * run until empty or time exceeded
	 */
	public void runUntilTime(int time) throws SimulationException {
		while (hasNextEvent() && currentTime <= time) runNextEvent();
	}
	
	/**
	 * run until empty
	 */
	public void run() throws SimulationException {
		while (hasNextEvent()) runNextEvent();
	}
}
