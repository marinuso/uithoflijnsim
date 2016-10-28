package util;

/**
 * Pair
 */
public final class Pair<T, U> {
	
	private T x;
	private U y;
  
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
