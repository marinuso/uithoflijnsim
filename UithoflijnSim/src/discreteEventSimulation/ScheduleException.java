package discreteEventSimulation;

public class ScheduleException extends SimulationException {
	private static final long serialVersionUID = 4196718368298484525L;

	ScheduledEvent se; // the event that couldn't be scheduled
	public ScheduleException(ScheduledEvent se, int time) {
		super("Cannot schedule at " + time + ": " + se.description());
		this.se = se;
	}
}
