package discreteEventSimulation;

import java.util.*;

public class RNG {
	// this is just based on Java's RNG for now
	private static Random random = new Random();
	
	public static Double doubleBetween(double start, double end) {
		return start + random.nextDouble() * (end-start);
	}
	
	public static int intBetween(int start, int end) {
		return start + random.nextInt(end-start);
	}
}
