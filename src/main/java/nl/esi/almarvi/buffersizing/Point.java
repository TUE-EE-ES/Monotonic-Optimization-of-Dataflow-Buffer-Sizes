package nl.esi.almarvi.buffersizing;

/**
 * A Point encapsulates a sequence of integers.
 */
public interface Point {

    int numDims();

    int[] getDataCopy();

    int get(int dim);

    long getCost();

    /**
     * @param s a point
     * @return whether this point is in the backward cone of s
     */
    boolean inBackwardConeOf(Point s);
    
    boolean inForwardConeOf(Point s);
}
