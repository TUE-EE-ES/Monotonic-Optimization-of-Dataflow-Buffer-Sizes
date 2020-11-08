package nl.esi.almarvi.buffersizing;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SDForacle implements Oracle {

    private final File binary;

    private final File template;

    private final double threshold;

    private final Map<String, Integer> channelToId = new HashMap<String, Integer>();

    public SDForacle(File binary, File template, Map<String, Integer> channelToId, double threshold,
            int numBufs) {
        this.binary = binary;
        this.template = template;
        this.threshold = threshold;
        if (channelToId == null) {
            for (int i = 0; i < numBufs; i++) {
                this.channelToId.put("b" + i, i);
            }
        } else {
            this.channelToId.putAll(channelToId);
        }
    }

    public SDForacle(File binary, File template, double threshold, int numBufs) {
        this(binary, template, null, threshold, numBufs);
       }

    public OracleResult eval(Point p) {
        try {
            return SDF3util.runSDF3throughput(binary, template, p, channelToId, threshold);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
