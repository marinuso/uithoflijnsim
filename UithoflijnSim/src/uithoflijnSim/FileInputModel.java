package uithoflijnSim;
import java.nio.file.*;
import java.util.*;

import discreteEventSimulation.RNG;
import util.Debug;

import java.io.*;

/*
 input model read from a file
 */
public class FileInputModel extends InputModel {
	ArrayList<Record> records;
	
	public FileInputModel(String file) throws IOException, IllegalArgumentException {
		List<String> lines = Files.readAllLines(Paths.get(file));
	
		records = new ArrayList<Record>();
		
		for (String line : lines) {
			if (line.contains("From")) continue; // header
			
			Record r = new Record(line);
			records.add(r);
		}
	}
	
	// get mean arrival time given t
	public double meanPassengerArrivalTime(Uithoflijn u, String stop, boolean dirFromCS) {
		//double hour = 6 + ((double)u.getCurrentTime())/3600;
		double hour = UithofTime.hour(u.getCurrentTime());
		
		// if before or after hours, return 0
		if (hour < 6 || hour > 21.5) return 0;
		
		// find stop
		Record stopRecord = null;
		for (Record r : records) {
			if (r.stopName().equals(stop) && r.dirFromCS()==dirFromCS
					&& r.from()<=hour && r.to()>hour) {
				stopRecord = r;
				break;
			}
		}
		
		if (stopRecord == null) {
			Debug.out("Record not found: " + stop + " " + hour + " " + dirFromCS + "\n");
		}
		
		// seconds btw passengers
		double secs = (stopRecord.to()-stopRecord.from())*3600;
		double sec_pass = secs/stopRecord.passin();
		
		//Debug.out(" mPAT s="+stop+"("+dirFromCS+"), passin="+stopRecord.passin()+", sec_pass="+sec_pass+"\n");
		
		// we actually really want seconds per passengers
		return sec_pass;
	}
	
	// get a passenger for a station given t
	public Passenger generatePassenger(Uithoflijn u, String stop, boolean dirFromCS) {
		/* t = seconds, t=0 = 6 o'clock */
		
		double hour = 6 + ((double)u.getCurrentTime())/3600;
		
		// find stop + possible destinations
		Record stopRecord = null;
		ArrayList<Record> destinations = new ArrayList<Record>();
		boolean looking = false;
		for (Record r : records) {
			if (r.stopName().equals(stop) && r.dirFromCS()==dirFromCS
					&& r.from()<=hour && r.to()>hour) {
				stopRecord = r;
				looking = true;
				continue;
			}
			
			
			boolean keepLooking = stopRecord != null
				 && r.dirFromCS() == stopRecord.dirFromCS()
				 && r.from() == stopRecord.from()
				 && r.to() == stopRecord.to();
			
			if (looking	&& !keepLooking) {
				looking = false;
				break;
			}
			
			if (looking && keepLooking) {
				destinations.add(r);
			}
		}
		
		// total passengers possible in destinations
		double totPassenger = 0;
		for (Record r : destinations) totPassenger += r.passout();
		
		// find stop
		destinations.sort((Record r1, Record r2) -> new Double(r1.passout()).compareTo(r2.passout()));
		double passengerNo = RNG.doubleBetween(0, totPassenger);
		
		double nPassenger = 0;
		Record passStop = null; 
		for (Record r : destinations) {
			nPassenger += r.passout();
			if (nPassenger >= passengerNo) {
				passStop = r;
				break;
			}
		}
		
		// stop gotten from Uithoflijn
		ITrainReceiver origin = u.getStop(stop, dirFromCS);
		ITrainReceiver destination = u.getStop(passStop.stopName(), passStop.dirFromCS());
		
		// make passenger
		Passenger p = new Passenger(u, origin, destination);
		return p;
	}
	
}

class Record {
	private String stopName;
	private boolean dirFromCS;
	private double from, to;
	private double passin, passout;
	
	public Record(String stopName,
				  boolean dirFromCS,
				  double from,
				  double to,
				  double passin,
				  double passout) {
		this.stopName=stopName;
		this.dirFromCS=dirFromCS;
		this.from=from;
		this.to=to;
		this.passin=passin;
		this.passout=passout;
	}
	
	public Record(String record) throws IllegalArgumentException {
		String[] fields = record.split(";");
		if (fields.length != 6) {
			throw new IllegalArgumentException("Malformed record entry for artificial input: '" + record + "'");
		}
		
		this.stopName = fields[0];
		this.dirFromCS = fields[1].trim().equals("1");
		this.from = Double.parseDouble(fields[2]);
	    this.to = Double.parseDouble(fields[3]);
	    this.passin = Double.parseDouble(fields[4]);
	    this.passout = Double.parseDouble(fields[5]);
	    
	}
	
	public String stopName() { return stopName; }
	public boolean dirFromCS() { return dirFromCS; }
	public double from() { return from; }
	public double to() { return to; }
	public double passin() { return passin; }
	public double passout() { return passout; }
}