package discreteEventSimulation;

import java.util.*;

public class SimulationTests {
	public static void runSimFrameworkTests() throws Exception {
		System.out.println(TestSimulation.runSimA());
		System.out.println(TestSimulation.runSimB());
		System.out.println(TestSimulation.runSimC());
		System.out.println(TestSimulation.runSimD());
		System.out.println(TestSimulation.runSimE());
		System.out.println(TestSimulation.runSimF());
		
		int time = (int) 1e9;
		long begin = new Date().getTime();
		int ts = StressTestSimulation.runStressTest(time);
		long end = new Date().getTime();
		System.out.println("Time: " + (end-begin) 
				         + " - " + (end-begin)/(double)time + " ms/event"
				         + " - " + ts);
	}
}

class StressTestSimulation extends Simulation {
	public StressTestSimulation() {
		super();
	}
	
	public static int runStressTest(int time) throws Exception {
		StressTestSimulation sts = new StressTestSimulation();
		sts.scheduleNow(new StressTestEvent(0, sts));
		sts.runUntilTime(time);
		return sts.getCurrentTime();
	}
}

class StressTestEvent extends Event {
    int i;
    StressTestSimulation sts;
    
    public StressTestEvent(int i, StressTestSimulation sts) {
    	this.i=i;
    	this.sts=sts;
    }
    
	@Override
	public String description() {
		return "STS"+i;
	}

	@Override
	public void run() throws SimulationException {
		sts.scheduleRelative(1,  new StressTestEvent(i+1, sts));
	}
	
}

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
	
	// scheduling one by one absolute forwards
	public static String runSimC() throws Exception {
		TestSimulation ts = new TestSimulation();
		for (int i = 0; i < 26; i++) {
			ts.scheduleAbsolute(i, new TestEvent(""+(char)('A'+i), ts, ""+(char)('A'+i)));
		}
		ts.run();
		return ts.toString();		
	}
	
	// scheduling one by one absolute backwards
	public static String runSimD() throws Exception {
		TestSimulation ts = new TestSimulation();
		for (int i = 25; i >= 0; i--) {
			ts.scheduleAbsolute(i, new TestEvent(""+(char)('A'+i), ts, ""+(char)('A'+i)));
		}
		ts.run();
		return ts.toString();		
	}
	
	// scheduling recursively
	public static String runSimE() throws Exception {
		TestSimulation ts = new TestSimulation();
		ts.scheduleNow(new RecursiveTestEvent('A', ts));
		ts.run();
		return ts.toString();
	}
	
	// scheduling in random order
	public static String runSimF() throws Exception {
		TestSimulation ts = new TestSimulation();
	    ArrayList<Character> chars = new ArrayList<Character>();
	    for (char c='A'; c<='Z'; c++) {
	    	chars.add(c);
	    }
	    Collections.shuffle(chars);
	    for (char c : chars) {
	    	ts.scheduleAbsolute(c, new TestEvent(""+c, ts, ""+c));;
	    }
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
