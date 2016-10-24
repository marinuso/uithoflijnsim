package discreteEventSimulation;
import java.util.*;
import java.nio.file.*;
import java.io.*;

/**
 * Empirical Distribution
 */
public class EmpiricalDistribution implements IDistribution {
	private ArrayList<Double> dataset;
	private double average;
	
	public EmpiricalDistribution(String path) throws IOException {
		dataset = new ArrayList<Double>();
		
		// get path, correct for windows
		Path p = null;
		try {
			p = Paths.get(path);
		} catch (InvalidPathException e) {
			// windows's fault
			p = Paths.get(path.substring(1));
		}
		
		List<String> data = Files.readAllLines(p);
		
		double tally = 0;
		
		for (String item : data) {
			try {
				double d = Double.parseDouble(item);
				dataset.add(d);
				tally += d;
			} catch (NumberFormatException ne) {
				// doesn't matter
			}
		}
		
		average = tally/dataset.size();
	}
	
	public double average() { return average; }
	
	public double normalizedSample() { return sample() / average; }
	
	public double sample() {
		return dataset.get(RNG.intBetween(0, dataset.size()));
	}
	
	public double sampleSum(int amount) {
		double value = 0;
		for (int i=0; i<amount; i++) value += sample();
		return value;
	}
	
	
}
