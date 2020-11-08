package nl.esi.almarvi.buffersizing.test;

import org.junit.Test;

import nl.esi.almarvi.buffersizing.MonotonicOptimizerImpl;
import nl.esi.almarvi.buffersizing.Optimizer;
import nl.esi.almarvi.buffersizing.Oracle;
import nl.esi.almarvi.buffersizing.OracleResult;
import nl.esi.almarvi.buffersizing.OracleResultImpl;
import nl.esi.almarvi.buffersizing.Point;

public class OptimizerTest {

    @Test
    public void test2D() {
        Oracle o = new TestOracle2D();
        Optimizer opt = new MonotonicOptimizerImpl(o, new int[]{2, 3});
        // (43,15) weight 131
        opt.run();
    }

    @Test
    public void test2DB() {
        Oracle o = new TestOracle2DB();
        MonotonicOptimizerImpl opt = new MonotonicOptimizerImpl(o, new int[]{1, 1});
        opt.run();
    }

    @Test
    public void test3D() {
        Oracle o = new TestOracle3D();
        MonotonicOptimizerImpl opt = new MonotonicOptimizerImpl(o, new int[]{1, 1, 1});
        // min: (25, 10, 35) weight 70
        opt.run();
    }

    private static final class TestOracle2D implements Oracle {

        public OracleResult eval(Point p) {
            int x = p.get(0);
            int y = p.get(1);
            return new OracleResultImpl((2 * x + y >= 100) && x >= 20 && y >= 15, null);
        }
    }

    private static final class TestOracle3D implements Oracle {

        public OracleResult eval(Point p) {
            int x = p.get(0);
            int y = p.get(1);
            int z = p.get(2);
            return new OracleResultImpl(x >= 25 && y >= 10 && z >= 35, null);
        }
    }

    public static final class TestOracle2DB implements Oracle {

        private boolean inOpenCircle(int a, int b, int r, int x, int y) {
            return ((x - a) * (x - a) + (y - b) * (y - b) <= r * r || (x >= a && y >= b - r)
                    || (x >= a - r && y >= b));
        }

        public OracleResult eval(Point p) {
            int x = p.get(0);
            int y = p.get(1);
            return new OracleResultImpl(inOpenCircle(85, 35, 20, x, y) || inOpenCircle(50, 80, 20, x, y), null);
        }
    }
}
