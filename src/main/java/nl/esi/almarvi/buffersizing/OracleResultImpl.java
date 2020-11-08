package nl.esi.almarvi.buffersizing;

import java.util.Arrays;

public final class OracleResultImpl implements OracleResult {

    private final boolean satisfied;

    private double tp;

    private final boolean[] storageDep;

    public OracleResultImpl(boolean satisfied, boolean[] bufferHint) {
        this.tp = -1d;
        this.satisfied = satisfied;
        this.storageDep = bufferHint;
    }
    
    public OracleResultImpl(double tp, double threshold, boolean[] bufferHint) {
        this.tp = tp;
        this.satisfied = tp >= threshold;
        this.storageDep = bufferHint;
    }

    public boolean satisfied() {
        return satisfied;
    }

    public boolean[] isStorageDependency() {
        return storageDep;
    }

    @Override
    public String toString() {
        String tps = Double.toString(tp);
        if (tp < 0d) {
            tps = "n/a";
        }
        return "OracleResultImpl [tp=" + tps + ", satisfied=" + satisfied + ", deps="
                + Arrays.toString(storageDep) + "]";
    }

}
