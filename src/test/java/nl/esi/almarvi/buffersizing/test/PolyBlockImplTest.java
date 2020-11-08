package nl.esi.almarvi.buffersizing.test;

import nl.esi.almarvi.buffersizing.PointImpl;
import nl.esi.almarvi.buffersizing.PolyBlock;
import nl.esi.almarvi.buffersizing.PolyBlockImpl;


public class PolyBlockImplTest extends AbstractPolyBlockTest {

    @Override
    public PolyBlock getPolyBlock(int numDims) {
        return new PolyBlockImpl(PointImpl.getUnitCost(numDims));
    }
}
