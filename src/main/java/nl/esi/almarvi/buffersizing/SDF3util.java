package nl.esi.almarvi.buffersizing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SDF3util {

    public enum Algo {
        THROUGHPUT, CONSISTENCY, BUFFERSIZE, THROUGHPUTANALYSIS;
    };

    private static final String MARKER = "\\$B";

    private SDF3util() {
    }

    public static double getThroughputSDF3(File SDF3_EXEC, File modelTemplate, Point p)
            throws IOException, InterruptedException {
        String s = runSDF3(SDF3_EXEC, modelTemplate, p, Algo.THROUGHPUT);
        // OUTPUT SDF3: thr(...) = <double>
        for (String line : s.split("\\n")) {
            if (line.startsWith("thr")) {
                String[] tok = line.split("=");
                return Double.parseDouble(tok[1]);
            }
        }
        throw new IllegalStateException("Failed to parse throughput from: " + s);
    }

    public static String runSDF3(File SDF3_EXEC, File modelTemplate, Point p, Algo algo)
            throws IOException, InterruptedException {
        File model = null;
        try {
            model = createModel(modelTemplate, p);
            List<String> cmd = new ArrayList<String>();
            cmd.add(SDF3_EXEC.getAbsolutePath());
            cmd.add("--graph");
            cmd.add(model.getName());
            cmd.add("--algo");
            cmd.add(algo.toString().toLowerCase());
            ProcessBuilder probuilder = new ProcessBuilder(cmd.toArray(new String[cmd.size()]));
            probuilder.directory(model.getParentFile());
            Process proc = probuilder.start();
            IOreader r1 = new IOreader(proc.getInputStream());
            IOreader r2 = new IOreader(proc.getErrorStream());
            Thread t1 = new Thread(r1);
            Thread t2 = new Thread(r2);
            t1.start();
            t2.start();
            try {
                proc.waitFor();
            } catch (InterruptedException e) {
                proc.destroyForcibly();
                throw e;
            }
            t1.join();
            t2.join();
            return r1.get() + r2.get();
        } finally {
            if (model != null) {
                model.delete();
            }
        }
    }

    /**
     * @param modelTemplate the csdf model template; channel capacities are $B0, $B1, etc
     * @param p
     * @param channelToPlaceholder
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static OracleResult runSDF3throughput(File SDF3_EXEC, File modelTemplate, Point p,
            Map<String, Integer> channelToPlaceholder, double threshold)
            throws IOException, InterruptedException {
        File model = null;
        try {
            model = createModel(modelTemplate, p);
            List<String> cmd = new ArrayList<String>();
            cmd.add(SDF3_EXEC.getAbsolutePath());
            cmd.add("--graph");
            cmd.add(model.getName());
            cmd.add("--algo");
            cmd.add(Algo.THROUGHPUTANALYSIS.toString().toLowerCase());
            ProcessBuilder probuilder = new ProcessBuilder(cmd.toArray(new String[cmd.size()]));
            probuilder.directory(model.getParentFile());
            Process proc = probuilder.start();
            try {
                proc.waitFor();
            } catch (InterruptedException e) {
                proc.destroyForcibly();
                throw e;
            }
            // parse the resulting xml file
            File xmlFile = new File(model.getParentFile(), "thranalysisresult.xml");
            return parseXMLresult(xmlFile, channelToPlaceholder, threshold);
        } finally {
            if (model != null) {
                model.delete();
            }
        }
    }

    private static OracleResult parseXMLresult(File xml, Map<String, Integer> channelToPlaceholder,
            double threshold) throws IOException {
        boolean[] hint = new boolean[channelToPlaceholder.size()];
        double tp = 0d;
        BufferedReader r = new BufferedReader(new FileReader(xml));
        try {
            String line = r.readLine();
            while (line != null) {
                int index = line.indexOf("thr=");
                if (index > 0) {
                    line = line.substring(index + 5);
                    index = line.indexOf("'");
                    line = line.substring(0, index);
                    tp = Double.parseDouble(line);
                } else if (!line.contains("distribution")) {
                    int[] i = new int[4];
                    int p = 0;
                    for (int k = 0; k < line.length(); k++) {
                        if (line.charAt(k) == '\'') {
                            i[p] = k;
                            p++;
                        }
                    }
                    String name = line.substring(i[0] + 1, i[1]);
                    boolean dep = Integer.parseInt(line.substring(i[2] + 1, i[3])) == 1;
                    if (channelToPlaceholder.containsKey(name)) {
                        hint[channelToPlaceholder.get(name)] = dep;
                    }
                }
                line = r.readLine();
            }
            return new OracleResultImpl(tp, threshold, hint);
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }

    public static File createModel(File modelTemplate, Point p) throws IOException {
        String template = readFile(modelTemplate);
        BufferedWriter w = null;
        try {
            File dir = new File("./tmp/");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File model = new File(dir, modelTemplate.getName() + "-tmp.xml");
            w = new BufferedWriter(new FileWriter(model));
            if (p != null) {
                for (int i = p.numDims() - 1; i >= 0; i--) {
                    // Go backward otherwise $B1 is replaced in $B10 etc...
                    String key = MARKER + i;
                    String value = Integer.toString(p.get(i));
                    template = template.replaceAll(key, value);
                }
            }
            w.write(template);
            return model;
        } finally {
            w.close();
        }
    }

    private static String readFile(File f) throws IOException {
        BufferedReader in = null;
        try {
            StringBuilder b = new StringBuilder();
            in = new BufferedReader(new FileReader(f));
            String line = in.readLine();
            while (line != null) {
                b.append(line).append("\n");
                line = in.readLine();
            }
            return b.toString();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private static final class IOreader implements Runnable {

        private final BufferedReader in;

        private final StringBuilder b = new StringBuilder();

        public IOreader(InputStream in) {
            this.in = new BufferedReader(new InputStreamReader(in));
        }

        public void run() {
            try {
                String line = in.readLine();
                while (line != null) {
                    b.append(line).append("\n");
                    line = in.readLine();
                }
            } catch (IOException e) {

            } finally {
                try {
                    in.close();
                } catch (IOException e) {

                }
            }
        }

        public String get() {
            return b.toString();
        }
    }
}
