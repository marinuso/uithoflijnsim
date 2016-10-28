package util;

import java.io.*;

public class Debug {
	public static final boolean DEBUG = true;
	
	public static void out(String s) {
		if (DEBUG) System.err.print("DEBUG: " + s);
	}
}
