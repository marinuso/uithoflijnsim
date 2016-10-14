package uithoflijnSim;
import discreteEventSimulation.*;

public class Uithoflijn extends Simulation {
	private InputModel inputModel;
	
	public InputModel getInputModel() { return inputModel; }

	public Uithoflijn(InputModel inputModel) {
		this.inputModel = inputModel;
	}
}
