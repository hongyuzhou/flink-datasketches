package org.apache.flink.core.function.table.distinct;

import org.apache.datasketches.SketchesArgumentException;
import org.apache.datasketches.hll.HllSketch;
import org.apache.datasketches.hll.TgtHllType;
import org.apache.flink.core.function.table.SketchUDAFunction;
import org.apache.flink.core.serializer.HllTypeSerializer;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.functions.FunctionContext;

import static org.apache.datasketches.hll.HllSketch.DEFAULT_LG_K;

/**
 * Use HllSketch {@link HllSketch} As Accumulator For HllUDAFunction
 *
 * @param <T> final result type of the aggregation
 */
@FunctionHint(
        accumulator = @DataTypeHint(value = "RAW", bridgedTo = HllSketch.class, rawSerializer = HllTypeSerializer.class)
)
public abstract class HllUDAFunction<T> extends SketchUDAFunction<T, HllSketch> {

    private static final long serialVersionUID = 1L;

    protected int lgConfigK;

    protected TgtHllType tgtHllType;

    public HllUDAFunction() {
        this.lgConfigK = DEFAULT_LG_K;
        this.tgtHllType = TgtHllType.HLL_4;
    }

    public HllUDAFunction(int lgConfigK, TgtHllType tgtHllType) {
        if ((lgConfigK < 4) || (lgConfigK > 21)) {
            throw new SketchesArgumentException(
                    "Log K must be between 4 and 21, inclusive: " + lgConfigK);
        }
        this.lgConfigK = lgConfigK;
        this.tgtHllType = tgtHllType;
    }

    @Override
    public void open(FunctionContext context) throws Exception {
        super.open(context);
    }

    @Override
    public HllSketch createAccumulator() {
        return new HllSketch(lgConfigK, tgtHllType);
    }

    public void resetAccumulator(HllSketch acc) {
        acc.reset();
    }
}