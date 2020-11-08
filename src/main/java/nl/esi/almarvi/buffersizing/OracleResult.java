package nl.esi.almarvi.buffersizing;

public interface OracleResult {

    boolean satisfied();

    /**
     * Uses the causal dependency analysis of:
     * <br/><code>
     * Stuijk, S., Geilen, M., Basten, T.: Throughput-buffering trade-off exploration for cyclo-static and synchronous 
     * dataflow graphs. IEEE Transactions on Computers 57(10), 1331-1345 (2008).
     * DOI 10.1109/TC.2008.58
     * </code> <br/>
     * 
     * @return result from causal dependency analysis
     */
    boolean[] isStorageDependency();
}
