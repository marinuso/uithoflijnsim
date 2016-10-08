package uithoflijnSim;

/**
 * A piece of track
 */
public class Track {
	int length;
	
	// return length of the track
	public int getLength() { return length; }
	
	// train capacity
	public int getTrainCapacity() { return length/Train.LENGTH; }
	
}
