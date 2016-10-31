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
		// freq q im td pd n
		
		/*
		if (args.length != 6) {
			System.err.print("Usage: [freq] [q] [input model csv] [train output] [pass output] [n]\n");
			return;
		}
        // */
		
		// /*
		//InputModel im_ = new ArtificialInputModel("E:\\U\\input-data-passengers-01.csv");
		InputModel im_ = new FileInputModel("P:\\bus\\realistic.csv");
		runSimulation(15, 5, im_, Paths.get("E:\\trainout.csv"), Paths.get("E:\\passout.csv"));
		// */
		
		//return;
		/*
		int freq = Integer.parseInt(args[0]);
		int q = Integer.parseInt(args[1]);
		InputModel im = new ArtificialInputModel(args[2]);
		
		String train_pf = args[3];
		String passenger_pf = args[4];
		
		int nSim = Integer.parseInt(args[5]);
		
		Uithoflijn[] sims = new Uithoflijn[nSim];
		
		for (int i=0; i<nSim; i++) {
			sims[i] = runSimulation(freq, q, im, Paths.get(train_pf+i+".csv"), 
					 Paths.get(passenger_pf+i+".csv"));
		}
		// */
	}

	public static Uithoflijn runSimulation(int freq, int q, InputModel im, Path td, Path pd) throws Exception {

/*
       ClassLoader classloader = Thread.currentThread().getContextClassLoader();
       String path = Paths.get(classloader.getResource("sm_ls.csv").toURI()).toString();
*/
		
		InputStream is = UithoflijnSim.class.getResourceAsStream("/uithoflijnSim/sm_ls.csv");
		
		EmpiricalDistribution ed = new EmpiricalDistribution(is);
		
		//EmpiricalDistribution ed = new EmpiricalDistribution(UithoflijnSim.class.
			//	getResource("/uithoflijnSim/sm_ls.csv").getPath());

       
        //EmpiricalDistribution ed = new EmpiricalDistribution(path);
       
		Timetable tm = new Timetable(0, UithofTime.t(19), freq, q);
		
		Debug.out("Creating simulation.\n");
		Uithoflijn u = new Uithoflijn(im, tm, ed);
		
		Debug.out("Starting simulation.\n");
		
		u.runUntilTime(UithofTime.t(21));
		
		Debug.out("Simulation completed.\n");
		
		Files.write(td, u.dumpTrainData().getBytes());
		Files.write(pd, u.dumpPassengerData().getBytes());
		
		return u;
	}

}

