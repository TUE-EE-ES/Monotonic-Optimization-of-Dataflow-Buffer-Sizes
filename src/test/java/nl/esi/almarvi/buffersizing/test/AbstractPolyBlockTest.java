package nl.esi.almarvi.buffersizing.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import nl.esi.almarvi.buffersizing.PointImpl;
import nl.esi.almarvi.buffersizing.Point;
import nl.esi.almarvi.buffersizing.PolyBlock;

public abstract class AbstractPolyBlockTest {

    private PolyBlock pb2;

    private PolyBlock pb3;

    @Before
    public void setup() {
        pb2 = getPolyBlock(2);
        pb3 = getPolyBlock(3);
    }

    public abstract PolyBlock getPolyBlock(int numDims);

    @Test
    public void testDim2_empty() {
        assertEquals(1, pb2.getKnees().size());
        assertEquals(0, pb2.getFront().size());
    }

    @Test
    public void testDim2_singlePoint() {
        pb2.add(new PointImpl(2, 5));
        assertEquals(2, pb2.getKnees().size());
        assertTrue(pb2.getKnees().contains(getPoint(2, 0)));
        assertTrue(pb2.getKnees().contains(getPoint(0, 5)));
    }

    @Test
    public void testDim2_twoPoints() {
        pb2.add(new PointImpl(1, 2));
        pb2.add(new PointImpl(2, 1));
        assertEquals(3, pb2.getKnees().size());
        assertTrue(pb2.getKnees().contains(getPoint(2, 0)));
        assertTrue(pb2.getKnees().contains(getPoint(0, 2)));
        assertTrue(pb2.getKnees().contains(getPoint(1, 1)));
    }

    @Test
    public void testDim2_twoPoints_dominated() {
        pb2.add(new PointImpl(1, 2));
        pb2.add(new PointImpl(2, 1));
        pb2.add(new PointImpl(3, 3));
        assertEquals(2, pb2.getKnees().size());
        assertTrue(pb2.getKnees().contains(getPoint(3, 0)));
        assertTrue(pb2.getKnees().contains(getPoint(0, 3)));
    }

    @Test
    public void testDim2_twoPoints_dominatedOneDim() {
        pb2.add(new PointImpl(1, 2));
        pb2.add(new PointImpl(2, 1));
        pb2.add(new PointImpl(3, 1));
        assertEquals(3, pb2.getKnees().size());
        assertTrue(pb2.getKnees().contains(getPoint(3, 0)));
        assertTrue(pb2.getKnees().contains(getPoint(1, 1)));
        assertTrue(pb2.getKnees().contains(getPoint(0, 2)));
    }

    @Test
    public void testDim3_singlePoint() {
        pb3.add(new PointImpl(100, 100, 100));
        assertEquals(3, pb3.getKnees().size());
        assertTrue(pb3.getKnees().contains(getPoint(100, 0, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 100, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 0, 100)));
    }

    @Test
    public void testDim3_twoPoints() {
        pb3.add(new PointImpl(100, 100, 100));
        pb3.add(new PointImpl(183, 83, 83));
        assertTrue(pb3.getKnees().contains(getPoint(0, 100, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 0, 100)));
        assertTrue(pb3.getKnees().contains(getPoint(183, 0, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(100, 83, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(100, 0, 83)));
    }

    @Test
    public void testDim3_twoPoints_dominated() {
        pb3.add(new PointImpl(10, 10, 10));
        pb3.add(new PointImpl(20, 10, 10));
        assertTrue(pb3.getKnees().contains(getPoint(20, 0, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 10, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 0, 10)));
    }

    @Test
    public void testDim3_complex() {
        // Point 1
        pb3.add(new PointImpl(10, 10, 10));
        assertEquals(3, pb3.getKnees().size());
        assertTrue(pb3.getKnees().contains(getPoint(10, 0, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 10, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 0, 10)));

        // Point 2
        pb3.add(new PointImpl(15, 8, 5));
        assertEquals(5, pb3.getKnees().size());
        assertTrue(pb3.getKnees().contains(getPoint(0, 10, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 0, 10)));
        assertTrue(pb3.getKnees().contains(getPoint(15, 0, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(10, 8, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(10, 0, 5)));

        // Point 3
        pb3.add(new PointImpl(7, 12, 2));
        assertEquals(7, pb3.getKnees().size());
        assertTrue(pb3.getKnees().contains(getPoint(0, 0, 10)));
        assertTrue(pb3.getKnees().contains(getPoint(15, 0, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(10, 8, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(10, 0, 5)));
        assertTrue(pb3.getKnees().contains(getPoint(7, 10, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 12, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 10, 2)));

        // Point 4
        pb3.add(new PointImpl(11, 11, 1));
        assertEquals(9, pb3.getKnees().size());
        assertTrue(pb3.getKnees().contains(getPoint(0, 0, 10)));
        assertTrue(pb3.getKnees().contains(getPoint(15, 0, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(10, 0, 5)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 12, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 10, 2)));
        assertTrue(pb3.getKnees().contains(getPoint(11, 8, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(10, 8, 1)));
        assertTrue(pb3.getKnees().contains(getPoint(7, 11, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(7, 10, 1)));

        // Point 5
        pb3.add(new PointImpl(13, 13, 8));
        assertEquals(7, pb3.getKnees().size());
        assertTrue(pb3.getKnees().contains(getPoint(0, 0, 10)));
        assertTrue(pb3.getKnees().contains(getPoint(15, 0, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 13, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(13, 8, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(13, 0, 5)));
        assertTrue(pb3.getKnees().contains(getPoint(10, 0, 8)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 10, 8)));
    }

    @Test
    public void testDim3_samePlane() {
        // Point 1
        pb3.add(new PointImpl(10, 10, 10));
        assertEquals(3, pb3.getKnees().size());
        assertTrue(pb3.getKnees().contains(getPoint(10, 0, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 10, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 0, 10)));

        // Point 2
        pb3.add(new PointImpl(10, 15, 5));
        assertEquals(4, pb3.getKnees().size());
        assertTrue(pb3.getKnees().contains(getPoint(10, 0, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 15, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 10, 5)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 0, 10)));

        // Point 3
        pb3.add(new PointImpl(12, 13, 3));
        assertEquals(6, pb3.getKnees().size());
        assertTrue(pb3.getKnees().contains(getPoint(12, 0, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 15, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 0, 10)));
        assertTrue(pb3.getKnees().contains(getPoint(10, 0, 3)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 10, 5)));
        assertTrue(pb3.getKnees().contains(getPoint(10, 13, 0)));
    }

    @Test
    public void testDim3_fillCorner() {
        pb3.add(new PointImpl(10, 10, 10));
        pb3.add(new PointImpl(10, 11, 9));
        pb3.add(new PointImpl(9, 11, 10));
        pb3.add(new PointImpl(9, 10, 12));
        assertEquals(6, pb3.getKnees().size());
        assertTrue(pb3.getKnees().contains(getPoint(10, 0, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 11, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 0, 12)));
        assertTrue(pb3.getKnees().contains(getPoint(9, 10, 9)));
        assertTrue(pb3.getKnees().contains(getPoint(9, 0, 10)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 10, 10)));
        // fill the corner:
        pb3.add(new PointImpl(10, 11, 10));
        assertEquals(5, pb3.getKnees().size());
        assertTrue(pb3.getKnees().contains(getPoint(10, 0, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 11, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 0, 12)));
        assertTrue(pb3.getKnees().contains(getPoint(9, 0, 10)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 10, 10)));
    }

    @Test
    public void testDim3_twoConsecutiveBoxes() {
        pb3.add(new PointImpl(10, 10, 10));
        pb3.add(new PointImpl(15, 5, 5));
        assertEquals(5, pb3.getKnees().size());
        assertTrue(pb3.getKnees().contains(getPoint(15, 0, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 10, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 0, 10)));
        assertTrue(pb3.getKnees().contains(getPoint(10, 0, 5)));
        assertTrue(pb3.getKnees().contains(getPoint(10, 5, 0)));
        // add a box next to the smaller one
        pb3.add(new PointImpl(15, 7, 5));
        assertEquals(5, pb3.getKnees().size());
        assertTrue(pb3.getKnees().contains(getPoint(15, 0, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 10, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 0, 10)));
        assertTrue(pb3.getKnees().contains(getPoint(10, 0, 5)));
        assertTrue(pb3.getKnees().contains(getPoint(10, 7, 0)));
    }

    @Test
    public void testDim3_twoConsecutiveBoxes2() {
        pb3.add(new PointImpl(10, 10, 10));
        pb3.add(new PointImpl(15, 5, 5));
        // add a box next to the smaller one
        pb3.add(new PointImpl(15, 5, 10));
        assertEquals(4, pb3.getKnees().size());
        assertTrue(pb3.getKnees().contains(getPoint(15, 0, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 10, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 0, 10)));
        assertTrue(pb3.getKnees().contains(getPoint(10, 5, 0)));
    }

    @Test
    public void testDim3_twoConsecutiveBoxes3() {
        pb3.add(new PointImpl(10, 10, 10));
        pb3.add(new PointImpl(15, 5, 10));
        // add a box next to the smaller one
        pb3.add(new PointImpl(15, 5, 15));
        assertEquals(5, pb3.getKnees().size());
        assertTrue(pb3.getKnees().contains(getPoint(15, 0, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 10, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 0, 15)));
        assertTrue(pb3.getKnees().contains(getPoint(10, 5, 0)));
        assertTrue(pb3.getKnees().contains(getPoint(0, 5, 10)));
    }

    private Point getPoint(int... xs) {
        int[] cost = new int[xs.length];
        return new PointImpl(xs, cost);
    }
}
