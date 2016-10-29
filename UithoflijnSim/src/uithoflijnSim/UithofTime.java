package uithoflijnSim;

/* deal with times to t */

public class UithofTime {
	public static final double START_TIME = 6; // we start at 6 o'clock
	
	public static int tFromHours(double hours) {
		hours -= START_TIME;
		hours *= 3600;
		return (int) hours;
	}
	
	public static int tFromHoursMinutes(int hours, int minutes) {
		return tFromHours(hours + (double)minutes/60.);
	}
	
	public static int t(double h)     { return tFromHours(h); }
	public static int t(int h, int m) { return tFromHoursMinutes(h,m); }
}
