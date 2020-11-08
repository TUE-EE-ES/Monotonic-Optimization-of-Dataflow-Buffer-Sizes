package nl.esi.almarvi.buffersizing;

import java.util.Arrays;

/**
 * Immutable.
 */
public class PointImpl implements Point {

    // between 0 and Integer.MAX_VALUE; the latter is to be interpreted as \infty
    private final int[] xs;

    // FIXME: a long for cost to avoid overflows may still overflow
    private final long cost;

    private int hash;

    public PointImpl(int... xs) {
        this(xs, getUnitCost(xs.length));
    }

    public PointImpl(int[] xs, int[] cost) {
        if (xs.length != cost.length) {
            throw new IllegalArgumentException();
        }
        this.xs = xs;
        long c = 0;
        for (int i = 0; i < xs.length; i++) {
            c = c + (xs[i] * cost[i]);
        }
        this.cost = c;
        if (this.cost < 0) {
            throw new IllegalStateException();
        }
        this.hash = 31 + Arrays.hashCode(xs);
    }

    public int numDims() {
        return xs.length;
    }

    public int get(int index) {
        return xs[index];
    }

    public long getCost() {
        return cost;
    }

    public int[] getDataCopy() {
        return Arrays.copyOf(xs, xs.length);
    }

    public boolean inBackwardConeOf(Point s) {
        for (int i = 0; i < xs.length; i++) {
            if (xs[i] > s.get(i)) {
                return false;
            }
        }
        // this point is not larger than s in every dimension
        return true;
    }

    public boolean inForwardConeOf(Point s) {
        for (int i = 0; i < xs.length; i++) {
            if (xs[i] < s.get(i)) {
                return false;
            }
        }
        // this point is not smaller than s in every dimension
        return true;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PointImpl other = (PointImpl) obj;
        if (!Arrays.equals(xs, other.xs))
            return false;
        return true;
    }

    public static int[] getUnitCost(int d) {
        int[] cost = new int[d];
        for (int i = 0; i < d; i++) {
            cost[i] = 1;
        }
        return cost;
    }

    @Override
    public String toString() {
        return Arrays.toString(xs);
    }
}
