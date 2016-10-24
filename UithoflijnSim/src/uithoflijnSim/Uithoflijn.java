package uithoflijnSim;
import discreteEventSimulation.*;
import java.util.*;

public class Uithoflijn extends Simulation {
	private InputModel inputModel;
	
	private IDistribution travelSpeedDistribution;
	
	private ArrayList<Passenger> servedPassengers;
	
	public InputModel getInputModel() { return inputModel; }

	public Uithoflijn(InputModel inputModel) {
		this.inputModel = inputModel;
		servedPassengers = new ArrayList<Passenger>();
		
	}
	
	public IDistribution getTravelSpeedDistribution() {
		return travelSpeedDistribution;
	}
	
	public Stop getStop(String name, boolean dirToCS) {
		return null;
	}
	
	public void addServedPassengers(List<Passenger> passengers) {
		this.servedPassengers.addAll(passengers);
	}
}


