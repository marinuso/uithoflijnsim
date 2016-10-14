package uithoflijnSim;
import java.io.*;
import discreteEventSimulation.*;
import java.util.*;

public class UithoflijnSim {

	public static void main(String[] args) throws Exception {
		SimulationTests.runSimFrameworkTests();
		
		EmpiricalDistribution ed = new EmpiricalDistribution(UithoflijnSim.class.getResource("sm_ls.csv").getPath());
		
		for (int i=0; i<10; i++) {
			System.out.println(ed.sample());
		}
	}


}

