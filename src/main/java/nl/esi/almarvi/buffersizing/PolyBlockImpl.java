package nl.esi.almarvi.buffersizing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation based on JSPS paper.
 */
public class PolyBlockImpl implements PolyBlock {

    private final int dims;

    private final int[] cost;

    private final Set<Point> unsat = new HashSet<Point>();

    private final Set<Point> knees = new HashSet<Point>();

    public PolyBlockImpl(int dim) {
        this(getUnitCost(dim));
    }

    public PolyBlockImpl(int[] cost) {
        this.dims = cost.length;
        this.cost = cost;
        // Init knee point at origin
        PointImpl origin = new PointImpl(new int[dims], cost);
        knees.add(origin);
    }

    private static int[] getUnitCost(int d) {
        int[] cost = new int[d];
        for (int i = 0; i < d; i++) {
            cost[i] = 1;
        }
        return cost;
    }

    public long getMinCost() {
        long min = Long.MAX_VALUE;
        for (Point p : knees) {
            if (p.getCost() < min) {
                min = p.getCost();
            }
        }
        return min;
    }

    public Collection<Point> getFront() {
        return unsat;
    }

    public Collection<Point> getKnees() {
        return knees;
    }

    public Point getCheapestKnee() {
        long min = Integer.MAX_VALUE;
        Point k = null;
        for (Point p : knees) {
            if (p.getCost() < min) {
                min = p.getCost();
                k = p;
            }
        }
        return k;

    }

    public int getNumMinKnees() {
        int size = 1;
        long minCost = getMinCost();
        for (Point k : knees) {
            if (k.getCost() == minCost) {
                size++;
            }
        }
        return size;
    }

    public boolean add(Point p) {
        if (!addToUnsat(p)) {
            return false;
        }
        // Find dominated knees:
        List<Point> dominated = new ArrayList<Point>();
        for (Point k : knees) {
            if (k.inBackwardConeOf(p)) {
                dominated.add(k);
            }
        }
        knees.removeAll(dominated);
        // Create extensions:
        List<Point> extensions = new ArrayList<Point>();
        for (Point k : dominated) {
            for (int i = 0; i < dims; i++) {
                // extend k to p in direction i
                int[] data = k.getDataCopy();
                data[i] = p.get(i);
                PointImpl k2 = new PointImpl(data, cost);
                addToExtensions(extensions, k2);
            }
        }
        knees.addAll(extensions);
        return true;
    }

    private void addToExtensions(List<Point> extensions, Point k) {
        List<Point> dominated = new ArrayList<Point>();
        for (Point s : extensions) {
            if (s.inForwardConeOf(k)) {
                dominated.add(s);
            } else if (k.inForwardConeOf(s)) {
                return;
            }
        }
        extensions.removeAll(dominated);
        extensions.add(k);
    }

    /**
     * @param p a new unsat point
     * @return returns whether p is on the pareto front
     */
    private boolean addToUnsat(Point p) {
        List<Point> dominated = new ArrayList<Point>();
        for (Point s : unsat) {
            if (s.inBackwardConeOf(p) && !s.equals(p)) {
                dominated.add(s);
            } else if (p.inBackwardConeOf(s)) {
                return false;
            }
        }
        unsat.removeAll(dominated);
        unsat.add(p);
        return true;
    }
}
