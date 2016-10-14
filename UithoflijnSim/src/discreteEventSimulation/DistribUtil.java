package discreteEventSimulation;

public class DistribUtil {

	/*
	 * generate Poisson from lambda
	 * (Law p. 470)
	 */
	public static int poisson(double lambda) {
		double a = Math.pow(Math.E, -lambda);
		double b = 1;
		int i = -1;
		
		while (b>=a) {
			i++;
			b *= RNG.doubleBetween(0,1);
		}
		
		return i;
	}
	
	/*
	 * generate gamma2
	 * 
	 * ( got this from the internet, based on:
	 * https://github.com/SquareBracketAssociates/ArchiveOONumericalMethods/blob/master/NumericalMethods/Java/DhbStatistics/GammaDistribution.java#L196
	 *  )
	 * 
	 */
	public static double gamma2(double scale) {
		double u1, u2, v, y, z, w;
		double a = Math.sqrt(2*2-1);
		double b = 2-Math.log(4.0);
		double q = 2*1/a;
		double d = 1+Math.log(4.5);
		
		while (true) {
			u1 = RNG.doubleBetween(0, 1);
			u2 = RNG.doubleBetween(0, 1);
			v = a*Math.log(u1/(1-u1));
			y = 2*Math.exp(v);
			z = u1*u1*u2;
			w = b+q*v-y;
			if (w+d-4.5*z>=0 || w>=Math.log(z)) return y;
		}
	}
}
