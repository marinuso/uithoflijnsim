package util;


/**
 * Pair
 */
public class Pair<T, U> {
	
	private final T x;
	private final U y;
  
	/**
	 * 2-tuple
	 * 
	 * @param x First item
	 * @param y Second item
	 */
	public Pair(T x, U y) {
		this.x=x;
		this.y=y;
	}
	
	public T getX() { return x; }
	public U getY() { return y; }
}
