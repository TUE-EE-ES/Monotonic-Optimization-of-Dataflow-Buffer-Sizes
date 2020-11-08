package nl.esi.almarvi.buffersizing;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MonotonicOptimizerImpl implements Optimizer {

    private static final Comparator<Point> POINT_CMP = new Comparator<Point>() {

        public int compare(Point p1, Point p2) {
            return Long.compare(p1.getCost(), p2.getCost());
        }
    };

    private final NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);

    private final Oracle oracle;

    private final int[] cost;

    private final PolyBlock unsat;

    private List<Point> bestSoFar = new ArrayList<Point>();

    private long bestCostSoFar = Long.MAX_VALUE;

    private long costLowerBound = 0;

    private long startTimeNs = 0l;

    private long totalOracleTimeNs = 0l;

    private int numOracleCalls = 0;

    private Set<Point> satisfied = new HashSet<Point>();

    private final long sumCost;

    /**
     * @param o the oracle
     * @param d the number of dimensions
     */
    public MonotonicOptimizerImpl(Oracle o, int d) {
        this(o, PointImpl.getUnitCost(d));
    }

    /**
     * @param o the oracle
     * @param cost the cost vector
     */
    public MonotonicOptimizerImpl(Oracle o, int[] cost) {
        this.oracle = o;
        this.unsat = new PolyBlockImpl(cost);
        this.cost = cost;
        this.nf.setMaximumFractionDigits(1);
        costLowerBound = 0;
        long tmp = 0;
        for (int i = 0; i < cost.length; i++) {
            tmp += cost[i];
        }
        sumCost = tmp;
    }

    public int[] getCostVector() {
        return cost;
    }

    /**
     * {@inheritDoc}
     */
    public void setInitialSatPoint(int[] bestPoint) {
        if (!satisfied.isEmpty()) {
            throw new IllegalStateException("a SAT point already exists!");
        }
        PointImpl p = new PointImpl(bestPoint, cost);
        bestSoFar.add(p);
        bestCostSoFar = p.getCost();
        satisfied.add(p);
    }

    /**
     * {@inheritDoc}
     */
    public void setLowerBound(int d, int lb) {
        int[] p = new int[cost.length];
        for (int i = 0; i < cost.length; i++) {
            p[i] = Integer.MAX_VALUE;
        }
        p[d] = lb - 1;
        unsat.add(new PointImpl(p, cost));
        costLowerBound = Math.max(unsat.getMinCost() + sumCost, costLowerBound);
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        run(-1, 0);
    }

    /**
     * {@inheritDoc}
     */
    public void run(int maxOracleCalls, int delta) {
        startTimeNs = System.nanoTime();
        if (bestSoFar.isEmpty()) {
            initPhase();
        }
        boolean done = optimizationPhase(maxOracleCalls, delta);
        if (!done) {
            exhaustivePhase();
        }
    }

    private void initPhase() {
        System.out.println("Entering initialization phase:");
        Point k = unsat.getCheapestKnee();
        int[] d = k.getDataCopy();
        int[] v = new int[d.length];
        for (int i = 0; i < d.length; i++) {
            d[i] = d[i] + 1;
            v[i] = 1;
        }
        Point p = new PointImpl(d, cost);
        OracleResult or = callOracle(p);
        while (!or.satisfied()) {
            unsat.add(p);
            addCut(p, or);
            costLowerBound = unsat.getMinCost() + sumCost;
            int[] pd = p.getDataCopy();
            boolean[] bufferHint = or.isStorageDependency();
            for (int i = 0; i < pd.length; i++) {
                if (bufferHint == null || bufferHint[i]) {
                    v[i] = v[i] * 2;
                    pd[i] = pd[i] + v[i];
                }
            }
            p = new PointImpl(pd, cost);
            System.out.println("\ttrying " + p);
            or = callOracle(p);
        }
        bestSoFar.add(p);
        bestCostSoFar = p.getCost();
        satisfied.add(p);
        System.out.println("\nEntering optimization phase:\n");
    }

    private boolean optimizationPhase(int maxOracleCalls, int delta) {
        boolean stateChanged = true;
        CandidateSelector sel = new CandidateSelector(this);
        Point toExplore = sel.selectCandidate(true);
        while ((maxOracleCalls <= 0 || numOracleCalls < maxOracleCalls)
                && (bestCostSoFar - costLowerBound) > delta && toExplore != null) {
            if (stateChanged) {
                printIterationInfo();
                stateChanged = false;
            }
            OracleResult or = callOracle(toExplore);
            boolean sat = or.satisfied();
            if (sat) {
                addToSat(toExplore);
                if (toExplore.getCost() < bestCostSoFar) {
                    bestSoFar.clear();
                    bestSoFar.add(toExplore);
                    bestCostSoFar = toExplore.getCost();
                    stateChanged = true;
                } else if (toExplore.getCost() == bestCostSoFar) {
                    bestSoFar.add(toExplore);
                    stateChanged = true;
                }
            } else {
                unsat.add(toExplore);
                addCut(toExplore, or);
                if (unsat.getMinCost() + sumCost > costLowerBound) {
                    costLowerBound = unsat.getMinCost() + sumCost;
                    stateChanged = true;
                }
            }
            toExplore = sel.selectCandidate(sat);
        }
        if (stateChanged) {
            printIterationInfo();
        }
        return toExplore != null; // true iff max number of iterations has been reached
    }

    /**
     * Adapted algorithm of <br/>
     * <code>
     * Stuijk, S., Geilen, M., Basten, T.: Throughput-buffering trade-off exploration for cyclo-static and synchronous 
     * dataflow graphs. IEEE Transactions on Computers 57(10), 1331-1345 (2008).
     * DOI 10.1109/TC.2008.58
     * </code> <br/>
     * 
     */
    private void exhaustivePhase() {
        System.out.println("Entering exhaustive enumeration phase...");
        List<Point> U = new ArrayList<Point>();
        for (Point knee : unsat.getKnees()) {
            Point p = cheapestForwardSuccessor(knee);
            if (p.getCost() < bestCostSoFar) {
                U.add(p);
            }
        }
        Collections.sort(U, POINT_CMP);
        // Explore cheapest first:
        boolean found = false;
        while (!found && !U.isEmpty()) {
            Point p = removeCheapestPoint(U);
            System.out.print("# states to explore : " + U.size() + ",");
            System.out.println(" checking: " + p + " (cost = " + p.getCost() + ")");
            OracleResult r = callOracle(p);
            if (r.satisfied()) {
                if (p.getCost() < bestCostSoFar) {
                    bestSoFar.clear();
                }
                bestSoFar.add(p);
                bestCostSoFar = p.getCost();
                found = true;
            } else {
                unsat.add(p);
                // add successors to U based on criticality:
                boolean[] dep = r.isStorageDependency();
                // increase each critical buffer by 1 or every buffer if there is no criticality information
                for (int j = 0; j < p.numDims(); j++) {
                    if (dep == null || dep[j]) {
                        int[] d = p.getDataCopy();
                        d[j] = d[j] + 1; // TODO: step size
                        Point suc = new PointImpl(d, cost);
                        if (suc.getCost() < bestCostSoFar && !U.contains(suc)) {
                            U.add(suc);
                            System.out.println("\tadding: " + suc + " (cost = " + suc.getCost() + ")");
                        }
                    }
                }
                Collections.sort(U, POINT_CMP);
            }
        }
        System.out.println("\n\nBest point(s)               : " + bestSoFar);
        System.out.println("Best cost                   : " + bestCostSoFar);
        System.out.println("Number of oracle calls      : " + numOracleCalls);
        long totalTime = System.nanoTime() - startTimeNs;
        System.out
            .println("Total exploration time (s)  : " + nf.format((double) totalTime / (double) 1000000000l));
    }

    private Point removeCheapestPoint(List<Point> U) {
        return U.remove(0);
    }

    private Point cheapestForwardSuccessor(Point knee) {
        int[] d = knee.getDataCopy();
        for (int j = 0; j < d.length; j++) {
            if (d[j] < Integer.MAX_VALUE) {
                d[j] = d[j] + 1;
            }
        }
        return new PointImpl(d, cost);
    }

    private void addCut(Point p, OracleResult or) {
        // Check for critical buffers and bound point p further:
        if (or.isStorageDependency() != null) {
            int[] cut = p.getDataCopy();
            boolean isCut = false;
            for (int i = 0; i < cost.length; i++) {
                if (!or.isStorageDependency()[i]) {
                    cut[i] = Integer.MAX_VALUE;
                    isCut = true;
                }
            }
            if (isCut) { // only add if p has been extended in some dimension
                unsat.add(new PointImpl(cut, cost));
            }
        }
    }

    private OracleResult callOracle(Point p) {
        numOracleCalls++;
        long start = System.nanoTime();
        OracleResult result = oracle.eval(p);
        long end = System.nanoTime();
        totalOracleTimeNs += (end - start);
        return result;
    }

    private void addToSat(Point p) {
        List<Point> toRemove = new ArrayList<Point>();
        for (Point s : satisfied) {
            if (p.inForwardConeOf(s)) {
                return;
            } else if (s.inForwardConeOf(p)) {
                toRemove.add(s);
            }
        }
        satisfied.removeAll(toRemove);
        satisfied.add(p);
    }

    public boolean satContains(Point p) {
        for (Point s : satisfied) {
            if (p.inForwardConeOf(s)) {
                return true;
            }
        }
        return false;
    }

    public boolean unsatContains(Point p) {
        for (Point s : unsat.getFront()) {
            if (p.inBackwardConeOf(s)) {
                return true;
            }
        }
        return false;
    }

    public Point getCheapestKnee() {
        return unsat.getCheapestKnee();
    }

    public long getBestCostSoFar() {
        return bestCostSoFar;
    }

    private void printIterationInfo() {
        long totalTime = System.nanoTime() - startTimeNs;
        double percentageOracle = (100 * totalOracleTimeNs) / totalTime;
        System.out.print("# oracle calls              : " + numOracleCalls);
        System.out.print(" (" + satisfied.size() + " sat, ");
        System.out.print(unsat.getFront().size() + " unsat, ");
        System.out.println(unsat.getKnees().size() + " knees)");
        System.out.println("Best points so far          : " + bestSoFar);
        System.out.println("Cost                        : " + bestCostSoFar);
        System.out.println("Cost lower bound            : " + costLowerBound);
        long delta = bestCostSoFar - costLowerBound;
        double percentage = (100d * (double) delta) / (double) bestCostSoFar;
        System.out.println(
            "Maximal error               : " + delta + " (" + nf.format(percentage) + "% of best so far)");
        System.out.println("Total exploration time (ms) : " + (totalTime / 1000000));
        System.out.println("% of time in Oracle         : " + percentageOracle + "\n");
    }
}
