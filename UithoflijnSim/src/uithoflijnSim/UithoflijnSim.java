package uithoflijnSim;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import discreteEventSimulation.*;
import util.Debug;

import java.util.*;
import java.nio.file.*;

public class UithoflijnSim {

	public static void main(String[] args) throws Exception {
		//SimulationTests.runSimFrameworkTests();
		
		// test
 
		InputModel im = new ArtificialInputModel("E:\\U\\input-data-passengers-01.csv");
		runSimulation(15, 5, im, Paths.get("E:\\trainout.csv"), Paths.get("E:\\passout.csv"));
		
		
	}

	public static void runSimulation(int freq, int q, InputModel im, Path td, Path pd) throws Exception {
		EmpiricalDistribution ed = new EmpiricalDistribution(UithoflijnSim.class.getResource("sm_ls.csv").getPath());

		Timetable tm = new Timetable(0, UithofTime.t(19), freq, q);
		
		Debug.out("Creating simulation.\n");
		Uithoflijn u = new Uithoflijn(im, tm, ed);
		
		Debug.out("Starting simulation.\n");
		
		u.runUntilTime(UithofTime.t(21));
		
		Debug.out("Simulation completed.\n");
		
		Files.write(td, u.dumpTrainData().getBytes());
		Files.write(pd, u.dumpPassengerData().getBytes());
		
	}

}

