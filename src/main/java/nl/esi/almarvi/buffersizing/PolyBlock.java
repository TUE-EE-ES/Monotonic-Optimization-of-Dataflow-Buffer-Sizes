package nl.esi.almarvi.buffersizing;

import java.util.Collection;

public interface PolyBlock {

    /**
     * @param p the point to add to the front of the polyblock
     * @return true if and only if the point is not already contained in the polyblock
     */
    boolean add(Point p);

    Collection<Point> getFront();

    Collection<Point> getKnees();

    long getMinCost();

    Point getCheapestKnee();
}
