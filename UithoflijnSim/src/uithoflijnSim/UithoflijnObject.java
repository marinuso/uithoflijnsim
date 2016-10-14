package uithoflijnSim;

/*
 * Any object on the Uithoflijn. It keeps track of a pointer to the
 * Uithoflijn.
 */
public abstract class UithoflijnObject {

	protected final Uithoflijn uithoflijn;
	
	protected UithoflijnObject(Uithoflijn u) {
		uithoflijn = u;
	}
}
