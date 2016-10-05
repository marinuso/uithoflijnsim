package discreteEventSimulation;

/**
 * Event (runnable with a comment)
 *
 */
public abstract class Event {
	public abstract String description();
	public abstract void run() throws SimulationException;
}
