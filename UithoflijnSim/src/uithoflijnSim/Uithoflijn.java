package uithoflijnSim;
import discreteEventSimulation.*;
import java.util.*;
import util.*;


public class Uithoflijn extends Simulation {
	private InputModel inputModel;
	
	// travel speed distribution
	private IDistribution travelSpeedDistribution;
	
	// this keeps track of passengers that have been served
	private ArrayList<Passenger> servedPassengers;
	
	public InputModel getInputModel() { return inputModel; }

	// the stops (Name - toCS)
	public List<Triple<String, Boolean, ITrainReceiver>> stops;
	
	private Depot depot;
	private Timetable timetable;
	
	public Uithoflijn(InputModel inputModel, 
			          Timetable timetable,
			          IDistribution tsb) throws SimulationException {
		Debug.out("Initializing Uithoflijn\n");
		this.inputModel = inputModel;
		servedPassengers = new ArrayList<Passenger>();
		this.timetable = timetable;
		
		travelSpeedDistribution = tsb;
		
		stops = new ArrayList<Triple<String,Boolean,ITrainReceiver>>();
		
		Debug.out("Setting up stops\n");
		setUpStops();
		
		Debug.out("Done!");
	}
	
	public String dumpPassengerData() {
		StringBuilder out = new StringBuilder();
		
		// passenger data (how much they have had to wait)
		for (Passenger p : servedPassengers) {
			out.append(p.csv());
		}
		
		return out.toString();
	}
	
	public String dumpTrainData() {
		StringBuilder out = new StringBuilder();
		// tram data (diff between runs and timetable)
		for (Train t : depot.finishedTrains()) {
			out.append(t.csv());
		}
		return out.toString();
	}
	
	/**
	 * Turnaround time
	 * @return turnaround time (in seconds)
	 */
	public int getTurnaroundTime() {
		return 180;
	}
	
	public IDistribution getTravelSpeedDistribution() {
		return travelSpeedDistribution;
	}
	
	public ITrainReceiver getStop(String name, boolean dirFromCS) {
	//	Debug.out(" getStop " + name + " " + dirFromCS + "\n");
		for (Triple<String, Boolean, ITrainReceiver> stop : stops) {
			if (stop.getX().equals(name) && stop.getY().equals(dirFromCS)) {
	//			Debug.out("     :: returning: " + stop.getX() + "\n");
				return stop.getZ();
			}
		}
		
		throw new IllegalStateException("No such stop: " + name + " towards " + (dirFromCS?"CS":"PR"));
	}
	
	public void addServedPassengers(List<Passenger> passengers) {
		this.servedPassengers.addAll(passengers);
	}
	
	// set up the stops
	private void setUpStops() throws SimulationException {
		
		Debug.out(" -- Creating stops -> CS\n");
		// stops where trams go to CS
		//Stop PR_Uithof_toCS      = makeStop("P+R Uithof",     false, false);
	    Stop WKZ_toCS            = makeStop("WKZ",            false, false);
	    Stop UMC_toCS            = makeStop("UMC",            false, false);
	    Stop Heidelberglaan_toCS = makeStop("Heidelberglaan", false, false);
	    Stop Padualaan_toCS      = makeStop("Padualaan",      false, false);
	    Stop KrommeRijn_toCS     = makeStop("Kromme Rijn",    false, false);
	    Stop Galgenwaard_toCS    = makeStop("Galgenwaard",    false, false);
	    Stop Vaartscherijn_toCS  = makeStop("Vaartscherijn",  false, false);
	    EndStop CS               = new EndStop(this, "Centraal Station Centrumzijde", true);
	    
	    Debug.out(" -- Creating CS.\n");
	    stops.add(new Triple<>("Centraal Station Centrumzijde", false, CS));
	    stops.add(new Triple<>("Centraal Station Centrumzijde", true, CS));
	    
	    
	    Debug.out(" -- Creating stops -> PR\n");
	    // stops where trams go to P+R
	    //Stop CS_CZ_toPR          = makeStop("Centraal Station Centrumzijde", true, false);
	    Stop Vaartscherijn_toPR  = makeStop("Vaartscherijn",  true,  false);
	    Stop Galgenwaard_toPR    = makeStop("Galgenwaard",    true,  false);
	    Stop KrommeRijn_toPR     = makeStop("Kromme Rijn",    true,  false);
	    Stop Padualaan_toPR      = makeStop("Padualaan",      true,  false);
	    Stop Heidelberglaan_toPR = makeStop("Heidelberglaan", true,  false);
	    Stop UMC_toPR            = makeStop("UMC",            true,  false);
	    Stop WKZ_toPR            = makeStop("WKZ",            true,  false);
	    EndStop PR_Uithof        = new EndStop(this, "P+R Uithof", true);
	    
	    Debug.out(" -- Creating PR\n");
	    stops.add(new Triple<>("P+R Uithof", false, PR_Uithof));
	    stops.add(new Triple<>("P+R Uithof", true, PR_Uithof));

	    Debug.out(" -- Creating depot.\n");
	    depot = new Depot(this, UithofTime.t(19), PR_Uithof, timetable);
	    PR_Uithof.setDepot(depot);
	    
	    /* (these have moved into EndStop)
	    // crossovers on both sides
	    Crossover CSCrossover = new Crossover(this, "CS Crossover");
	    Crossover PRCrossover = new Crossover(this, "PR Crossover");
	    */
	    
	    // connect them all up
	    
	    // PR->CS
	    //PR_Uithof_toCS.setNextStop(PRCrossover.inputA(), 0, 1);
	    //PRCrossover.setOutputA(WKZ_toCS, 600, 600./1400.);
	    
	    Debug.out(" -- Connecting stops.\n");
	    PR_Uithof.setNextStop(WKZ_toCS, 600, 600./1400.);
	    WKZ_toCS.setNextStop(UMC_toCS, 600, 78./600.);
	    UMC_toCS.setNextStop(Heidelberglaan_toCS, 400, 82./400.);
	    Heidelberglaan_toCS.setNextStop(Padualaan_toCS, 400, 60./400.);
	    Padualaan_toCS.setNextStop(KrommeRijn_toCS, 800, 100./800.);
	    KrommeRijn_toCS.setNextStop(Galgenwaard_toCS, 600, 59./600.);
	    Galgenwaard_toCS.setNextStop(Vaartscherijn_toCS, 3100, 243./3100.);
	    Vaartscherijn_toCS.setNextStop(CS, 1400, 135./1400.);

	    
	    // CS->PR
	    CS.setNextStop(Vaartscherijn_toPR, 1400, 134./1400.);
	    Vaartscherijn_toPR.setNextStop(Galgenwaard_toPR, 3100, 243./3100.);
	    Galgenwaard_toPR.setNextStop(KrommeRijn_toPR, 600, 59./600.);
	    KrommeRijn_toPR.setNextStop(Padualaan_toPR, 800, 101./800.);
	    Padualaan_toPR.setNextStop(Heidelberglaan_toPR, 400, 60./400.);
	    Heidelberglaan_toPR.setNextStop(UMC_toPR, 400, 86./400.);
	    UMC_toPR.setNextStop(WKZ_toPR, 600, 78./600.);
	    WKZ_toPR.setNextStop(PR_Uithof, 600, 113./600.);
	    
	    Debug.out(" -- Stops are set up.\n");
	}
	
	
	/* initialize a stop */
	private Stop makeStop(String name, boolean dirFromCS, boolean isTurnaroundStop)
	throws ScheduleException {
		Debug.out(" ---- makeStop: " + name + "(" + dirFromCS + ")\n");
		Stop s = new Stop(this, name, dirFromCS, isTurnaroundStop);
		Debug.out(" -------\n");
		Triple<String, Boolean, ITrainReceiver> t = new Triple<>(name, dirFromCS, s);
		stops.add(t);
		return s;
	}
}


