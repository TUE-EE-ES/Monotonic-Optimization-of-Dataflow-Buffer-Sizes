package nl.esi.almarvi.buffersizing;

public final class CandidateSelector {

    private final MonotonicOptimizerImpl opt;

    private final int[] cost;

    private Point x;

    private double[] hpVector = null;

    private int m = 1;

    public CandidateSelector(MonotonicOptimizerImpl opt) {
        this.opt = opt;
        this.cost = opt.getCostVector();
    }

    public Point selectCandidate(boolean sat) {
       //return selectCandidateSimple();
       return selectCandidateExt(sat);
    }

    private Point selectCandidateSimple() {
        // Exploration of the cheapest knee:
        Point knee = opt.getCheapestKnee();
        double[] theHP = computeClosestHyperplanePoint(knee);
        hpVector = new double[cost.length];
        for (int i = 0; i < cost.length; i++) {
            hpVector[i] = (theHP[i] - (double) knee.get(i)) / 2d;
        }
        Point r = createPoint(knee, hpVector, 1);
        if (r.getCost() >= opt.getBestCostSoFar() || opt.unsatContains(r)) {
            return null;
        }
        return r;
    }

    private Point selectCandidateExt(boolean sat) {
        if (!sat) { // continue with bounding the cheapest knee
            m = m * 2;
            Point p = createPoint(x, hpVector, m);
            if (!opt.satContains(p)) {
                return p;
            }
        }
        // start exploration of the cheapest knee:
        x = opt.getCheapestKnee();
        double[] theHP = computeClosestHyperplanePoint(x);
        hpVector = new double[cost.length];
        for (int i = 0; i < cost.length; i++) {
            hpVector[i] = (theHP[i] - (double) x.get(i)) / 2d;
        }
        m = 1;
        Point r = createPoint(x, hpVector, m);
        if (opt.satContains(r) || opt.unsatContains(r)) {
            return null;
        }
        return r;
    }

    private double[] computeClosestHyperplanePoint(Point knee) {
        // compute closest point on the hyperplane to minKnee:
        double k = ((double) (knee.getCost() - opt.getBestCostSoFar())) / sumSquareWeights(cost);
        double[] xs = new double[cost.length];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = Math.floor(knee.get(i) - (k * cost[i]));
        }
        return xs;
    }

    private Point createPoint(Point g, double[] v, int n) {
        int[] xs = new int[cost.length];
        for (int i = 0; i < cost.length; i++) {
            xs[i] = (int) g.get(i) + (int) Math.floor(v[i] * n);
        }
        return new PointImpl(xs, cost);
    }

    private double sumSquareWeights(int[] weights) {
        double r = 0d;
        for (int i = 0; i < weights.length; i++) {
            r = r + (weights[i] * weights[i]);
        }
        return r;
    }
}
