package discreteEventSimulation;

/**
 * Simulation exception
 *
 */
public class SimulationException extends Exception {
	private static final long serialVersionUID = 5512118714164459579L;
	
	public SimulationException() { super(); }
	public SimulationException(String m) { super(m); }
	public SimulationException(String m, Throwable c) { super(m,c); }
	public SimulationException(Throwable c) { super(c); } 
}
