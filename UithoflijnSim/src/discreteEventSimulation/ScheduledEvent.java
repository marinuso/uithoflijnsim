package discreteEventSimulation;

/**
 * A scheduled event
 */
public class ScheduledEvent {
    private Event event;
    private int timestamp;
	
    /**
     * A scheduled event
     * 
     * @param timestamp
     * @param event
     */
    public ScheduledEvent(int timestamp, Event event) {
    	this.event = event;
    	this.timestamp = timestamp;
    }
    
    /**
     * Event function
     * @return event function
     */
    public Event getEventFunction() {
    	return event;
    }
    
    public int getTimestamp() {
    	return timestamp;
    }

    public void run() throws SimulationException {
		event.run();
	}

	/**
	 * Description of event, plus schedule
	 */
	public String description() {
		return "[Scheduled at: " + timestamp + "] " + event.description();
	}
}
