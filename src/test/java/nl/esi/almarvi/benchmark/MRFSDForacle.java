package nl.esi.almarvi.benchmark;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import nl.esi.almarvi.buffersizing.Oracle;
import nl.esi.almarvi.buffersizing.OracleResult;
import nl.esi.almarvi.buffersizing.Point;
import nl.esi.almarvi.buffersizing.PointImpl;
import nl.esi.almarvi.buffersizing.SDF3util;

public class MRFSDForacle implements Oracle {

    private final File binary;

    private final File template;

    private final double threshold;

    private final Map<String, Integer> channelToId = new HashMap<String, Integer>();

    private final int[] mask = new int[16];

    public MRFSDForacle(File binary, File template, int w) {
        this.binary = binary;
        this.template = template;

        channelToId.put("SRC2SUB1", 0);
        channelToId.put("L-downsampler-L12SUB2", 1);
        channelToId.put("L-filter-L12ADD1", 2);
        channelToId.put("L-upsamplerrec-L22ADD2", 3);
        channelToId.put("SRC2II-downsampler-L1", 4);
        channelToId.put("L-downsampler-L12II-upsamplerdec-L1", 5);
        channelToId.put("L-upsamplerdec-L12SUB1", 6);
        channelToId.put("SUB12II-filter-L1", 7);
        channelToId.put("L-upsamplerrec-L12ADD1", 8);
        channelToId.put("ADD22II-upsamplerrec-L1", 9);
        channelToId.put("L-downsampler-L12II-downsampler-L2", 10);
        channelToId.put("L-downsampler-L22II-upsamplerdec-L2", 11);
        channelToId.put("L-upsamplerdec-L22SUB2", 12);
        channelToId.put("SUB22II-filter-L2", 13);
        channelToId.put("L-filter-L22ADD2", 14);
        channelToId.put("L-downsampler-L22II-upsamplerrec-L2", 15);

        final int w2 = w / 2;
        final int w3 = w2 / 2;

        mask[4] = 2;
        mask[5] = 2;
        mask[6] = 2;
        mask[7] = w + 1;
        mask[8] = 2;
        mask[9] = 2;
        mask[10] = 2;
        mask[11] = 2;
        mask[12] = 2;
        mask[13] = w2 + 1;
        mask[14] = 2;
        mask[15] = w3 + 1;

        // compute the threshold
        try {
            threshold = SDF3util.getThroughputSDF3(binary, template,
                mergePoint(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public double getSTthroughput() {
        return threshold;
    }

    private Point mergePoint(int... v) {
        int[] data = Arrays.copyOf(mask, mask.length);
        for (int i = 0; i < v.length; i++) {
            data[i] = v[i];
        }
        return new PointImpl(data);
    }

    private Point mergePoint(Point p) {
        int[] data = Arrays.copyOf(mask, mask.length);
        for (int i = 0; i < p.numDims(); i++) {
            data[i] = p.get(i);
        }
        return new PointImpl(data);
    }

    public OracleResult eval(Point p) {
        try {
            OracleResult r = SDF3util.runSDF3throughput(binary, template, mergePoint(p), channelToId, threshold); 
            return new Wrapper(r);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static final class Wrapper implements OracleResult {

        private final OracleResult r;

        public Wrapper(OracleResult r) {
            this.r = r;
        }

        public boolean[] isStorageDependency() {
            return Arrays.copyOf(r.isStorageDependency(), 4);
        }

        public boolean satisfied() {
            return r.satisfied();
        }
    }
}
