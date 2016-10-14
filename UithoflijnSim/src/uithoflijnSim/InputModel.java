package uithoflijnSim;

public abstract class InputModel {
	public static final String[] stopNames = {
		"P+R Uithof",
		"WKZ",
		"UMC",
		"Heidelberglaan",
		"Padualaan",
		"Kromme Rijn",
		"Galgenwaard",
		"Vaartscherijn",
		"Centraal Station Centrumzijde"
	};
	
	public abstract Passenger generatePassenger(Uithoflijn u, String stop, boolean dirFromCS);
    public abstract double meanPassengerArrivalTime(Uithoflijn u, String stop, boolean dirFromCS);
    
}
