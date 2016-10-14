package uithoflijnSim;
import java.nio.file.*;
import java.util.*;
import java.io.*;

/*
 * Artificial input model read from a file
 */
public class ArtificialInputModel extends InputModel {
	
	
	public ArtificialInputModel(String file) throws IOException, IllegalArgumentException {
		List<String> lines = Files.readAllLines(Paths.get(file));
	
		for (String line : lines) {
			Record r = new Record(line);
		}
	}
	
	
}

class Record {
	private String stopName;
	private boolean dirFromCS;
	private double from, to;
	private double passin, passout;
	
	public Record(String stopName,
				  boolean dirToCS,
				  double from,
				  double to,
				  double passin,
				  double passout) {
		this.stopName=stopName;
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
	public boolean dirToCS() { return dirFromCS; }
	public double from() { return from; }
	public double to() { return to; }
	public double passin() { return passin; }
	public double passout() { return passout; }
}