package uithoflijnSim;

/**
 * A piece of track
 */
public class Track extends UithoflijnObject {
	int length;
	
	public Track(Uithoflijn u) {
		super(u);
	}
	
	// return length of the track
	public int getLength() { return length; }
	
	// train capacity
	public int getTrainCapacity() { return length/Train.LENGTH; }
	
}
