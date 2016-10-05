package uithoflijnSim;
import java.io.*;
import discreteEventSimulation.*;

public class UithoflijnSim {

	public static void main(String[] args) throws Exception {
		runSimFrameworkTests();
	}

	public static void runSimFrameworkTests() throws Exception {
		System.out.println(TestSimulation.runSimA());
		System.out.println(TestSimulation.runSimB());
		System.out.println(TestSimulation.runSimC());
		System.out.println(TestSimulation.runSimD());

	}
}

/**** TESTING ****/

class TestSimulation extends Simulation {
	StringBuilder sb;
	
	public TestSimulation() {
		super();
		sb = new StringBuilder();
	}
	
	public void callback(String cb) {
		sb.append(cb);
	}

	// scheduling all at once
	public static String runSimA() throws Exception {
		TestSimulation ts = new TestSimulation();
		for (int i = 0; i < 26; i++) {
			ts.scheduleNow(new TestEvent(""+(char)('A'+i), ts, ""+(char)('A'+i)));
		}
		ts.scheduleAbsolute(10000, new TestEvent("!", ts, "!"));;
		ts.run();
		return ts.toString();
	}
	
	// scheduling one by one relative
	public static String runSimB() throws Exception {
		TestSimulation ts = new TestSimulation();
		for (int i = 0; i < 26; i++) {
			ts.scheduleRelative(1, new TestEvent(""+(char)('A'+i), ts, ""+(char)('A'+i)));
		}
		ts.run();
		return ts.toString();
	}
	
	// scheduling one by one absolute
	public static String runSimC() throws Exception {
		TestSimulation ts = new TestSimulation();
		for (int i = 0; i < 26; i++) {
			ts.scheduleAbsolute(i, new TestEvent(""+(char)('A'+i), ts, ""+(char)('A'+i)));
		}
		ts.run();
		return ts.toString();		
	}
	
	// scheduling recursively
	public static String runSimD() throws Exception {
		TestSimulation ts = new TestSimulation();
		ts.scheduleNow(new RecursiveTestEvent('A', ts));
		ts.run();
		return ts.toString();
	}
	
	public String toString() {
		return "[" + currentTime + "] " + sb.toString();
	}
}

class RecursiveTestEvent extends Event {
	char c;
	TestSimulation ts;
	public RecursiveTestEvent(char c, TestSimulation ts) {
		this.c = c;
		this.ts = ts;
	}
	
	public void run() throws ScheduleException {
		ts.callback(""+c);
		if (c < 'Z') ts.scheduleRelative(c, new RecursiveTestEvent((char)(c+1), ts));
	}

	@Override
	public String description() {
		return "Test: " + c;
	}
}
class TestEvent extends Event {
    String description, cb;
    TestSimulation ts;
    
    public TestEvent(String description, TestSimulation ts, String cb) {
    	super();
    	
    	this.description=description;
    	this.ts=ts;
    	this.cb=cb;
    }
    
	@Override
	public void run() {
		ts.callback(cb);
	}

	@Override
	public String description() {
		// TODO Auto-generated method stub
		return "Test: " + description;
	}
	
	
}