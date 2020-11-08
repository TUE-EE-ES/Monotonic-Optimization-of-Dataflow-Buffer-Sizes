package nl.esi.almarvi.benchmark;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import nl.esi.almarvi.buffersizing.MonotonicOptimizerImpl;
import nl.esi.almarvi.buffersizing.Optimizer;
import nl.esi.almarvi.buffersizing.SDF3util;
import nl.esi.almarvi.buffersizing.SDForacle;

public class BenchmarkTest {

    private static final File MODEL_DIR = new File("sdf3-models");

    private static final File SDF3_EXEC = new File("sdf3analyze-csdf.exe");

    private static final File MP3 = new File(MODEL_DIR, "mp3playback.xml");

    private static final File H263D = new File(MODEL_DIR, "h263decoder.xml");

    private static final File SAMPLE = new File(MODEL_DIR, "samplerate.xml");

    private static final File SAT = new File(MODEL_DIR, "satellite.xml");

    @Test
    public void testMp3Playback() throws IOException, InterruptedException {
        Optimizer opt = createMP3playbackOptimizer();
        opt.run();
    }

    @Test
    public void testMp3PlaybackDelta() throws IOException, InterruptedException {
        Optimizer opt = createMP3playbackOptimizer();
        opt.run(-1, 18);
    }

    private Optimizer createMP3playbackOptimizer() throws IOException, InterruptedException {
        double stt = SDF3util.getThroughputSDF3(SDF3_EXEC, MP3, null);
        System.out.println("STT : " + stt);
        Map<String, Integer> channelToId = new HashMap<String, Integer>();
        channelToId.put("ch0", 0);
        channelToId.put("ch1", 1);
        SDForacle oracle = new SDForacle(SDF3_EXEC, MP3, channelToId, stt, 2);
        Optimizer opt = new MonotonicOptimizerImpl(oracle, 2);
        opt.setLowerBound(0, 1152);
        opt.setLowerBound(1, 441);
        return opt;
    }

    @Test
    public void testH263Dec() throws IOException, InterruptedException {
        Optimizer opt = createH263DecOptimizer();
        opt.run();
    }

    @Test
    public void testH263DecDelta() throws IOException, InterruptedException {
        Optimizer opt = createH263DecOptimizer();
        opt.run(-1, 27);
    }

    private Optimizer createH263DecOptimizer() throws IOException, InterruptedException {
        double stt = SDF3util.getThroughputSDF3(SDF3_EXEC, H263D, null);
        System.out.println("STT : " + stt);
        Map<String, Integer> channelToId = new HashMap<String, Integer>();
        channelToId.put("alpha", 0);
        channelToId.put("beta", 1);
        channelToId.put("gamma", 2);
        SDForacle oracle = new SDForacle(SDF3_EXEC, H263D, channelToId, stt, 3);
        Optimizer opt = new MonotonicOptimizerImpl(oracle, 3);
        opt.setLowerBound(0, 2376);
        opt.setLowerBound(2, 2376);
        return opt;
    }

    @Test
    public void testSampleRate() throws IOException, InterruptedException {
        double stt = SDF3util.getThroughputSDF3(SDF3_EXEC, SAMPLE, null);
        System.out.println("STT : " + stt);
        Map<String, Integer> channelToId = new HashMap<String, Integer>();
        channelToId.put("ch1", 0);
        channelToId.put("ch2", 1);
        channelToId.put("ch3", 2);
        channelToId.put("ch4", 3);
        channelToId.put("ch5", 4);
        SDForacle oracle = new SDForacle(SDF3_EXEC, SAMPLE, channelToId, stt, 5);
        Optimizer opt = new MonotonicOptimizerImpl(oracle, 5);
        // These initial sizes are computed by the SDF3 tool!
        opt.setLowerBound(0, 1);
        opt.setLowerBound(1, 4);
        opt.setLowerBound(2, 8);
        opt.setLowerBound(3, 14);
        opt.setLowerBound(4, 5);
        opt.run();
    }

    @Test
    public void testSatellite() throws IOException, InterruptedException {
        double stt = SDF3util.getThroughputSDF3(SDF3_EXEC, SAT, null);
        System.out.println("STT : " + stt);
        Map<String, Integer> channelToId = new HashMap<String, Integer>();
        for (int i = 0; i < 26; i++) {
            channelToId.put("ch" + (i + 1), i);
        }
        SDForacle oracle = new SDForacle(SDF3_EXEC, SAT, channelToId, stt, 26);
        Optimizer opt = new MonotonicOptimizerImpl(oracle, 26);
        // These lower bounds are computed by SDF3
        opt.setLowerBound(0, 4);
        opt.setLowerBound(1, 11);
        opt.setLowerBound(3, 10);
        opt.setLowerBound(4, 4);
        opt.setLowerBound(5, 11);
        opt.setLowerBound(7, 10);
        opt.setLowerBound(9, 11);
        opt.setLowerBound(10, 10);
        opt.setLowerBound(12, 11);
        opt.setLowerBound(13, 10);
        opt.setLowerBound(14, 240);
        opt.setLowerBound(15, 240);
        opt.setLowerBound(16, 240);
        opt.setLowerBound(17, 240);
        opt.setLowerBound(24, 240);
        opt.setLowerBound(25, 240);
        opt.run();
    }

    @Test
    public void testMRF() throws IOException, InterruptedException {
        Optimizer opt = createMRFOptimizer(32);
        opt.run();
    }

    @Test
    public void testMRFDelta() throws IOException, InterruptedException {
        Optimizer opt = createMRFOptimizer(32);
        opt.run(-1, 16);
    }

    private Optimizer createMRFOptimizer(int w) {
        File ph = new File(MODEL_DIR, "mrf-" + w + "x" + w + ".xml");
        MRFSDForacle oracle = new MRFSDForacle(SDF3_EXEC, ph, w);
        System.out.println("STT = " + oracle.getSTthroughput());
        Optimizer opt = new MonotonicOptimizerImpl(oracle, 4);
        return opt;
    }
}
