package uithoflijnSim;
import util.*;

/* deal with times to t */

public class UithofTime {
	public static final double START_TIME = 7; // we start at 7 o'clock
	
	public static int tFromHours(double hours) {
		hours -= START_TIME;
		hours *= 3600;
		return (int) hours;
	}
	
	public static int tFromHoursMinutes(int hours, int minutes) {
		return tFromHours(hours + (double)minutes/60.);
	}
	
	public static double hour(int t)  { return t/3600. + START_TIME; }
	
	public static int t(double h)     { return tFromHours(h); }
	public static int t(int h, int m) { return tFromHoursMinutes(h,m); }
	
	public static int hoursFromT(int t) {
		return (int) ((t/3600)+START_TIME);
	}
	
	public static int minutesFromT(int t) {
		return (int) (((double)(t%3600))/60.);
	}
	
	public static Pair<Integer,Integer> hoursMinutesFromT(int t) {
		return new Pair<Integer,Integer>(hoursFromT(t), minutesFromT(t));
	}
	
	public static int floorMin(int t) {
		return ((int) t/60) * 60;
	}
}
