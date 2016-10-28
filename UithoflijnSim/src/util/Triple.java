package util;

/**
 * 3-tuple
 *
 */
public final class Triple<T, U, V> extends Pair<T, U> {
	
	private final V z;
	
	public Triple(T x, U y, V z) {
		super(x,y);
		this.z = z;
	}
	
	public V getZ() { return z; }
}
