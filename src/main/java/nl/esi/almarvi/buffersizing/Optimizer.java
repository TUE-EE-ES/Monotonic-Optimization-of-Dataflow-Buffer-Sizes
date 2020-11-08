package nl.esi.almarvi.buffersizing;

public interface Optimizer {

    /**
     * Specifies an initial sat point.
     * 
     * @param bestPoint the best sat point known.
     */
    void setInitialSatPoint(int[] bestPoint);

    /**
     * Specifies a lower bound for a buffer.
     * 
     * @param d the dimension of the buffer
     * @param lb the lower bound (any valid buffer sizing has at least value lb for buffer d) 
     */
    void setLowerBound(int d, int lb);

    /**
     * Equivalent to {@link #run(int maxIterations)}} with {@code maxIterations == -1} and {@code delta == 0}.
     */
    void run();

    /**
     * Optimizes the buffer sizes. Each improvement is logged to the console on stdout.
     * 
     * @param maxIterations the maximal number of calls made to the oracle; -1 means that there is no bound
     * @param delta the maximal tolerated difference to the optimal solution; 0 finds the optimal solution 
     */
    void run(int maxIterations, int delta);
}
