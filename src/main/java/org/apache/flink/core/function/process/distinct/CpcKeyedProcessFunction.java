package org.apache.flink.core.function.process.distinct;

import org.apache.datasketches.SketchesArgumentException;
import org.apache.datasketches.cpc.CpcSketch;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.function.process.SketchKeyedProcessFunction;
import org.apache.flink.core.serializer.CpcTypeSerializer;

import static org.apache.datasketches.Util.DEFAULT_UPDATE_SEED;
import static org.apache.datasketches.cpc.CpcSketch.DEFAULT_LG_K;

/**
 * Use CpcSketch {@link CpcSketch} For CpcKeyedProcessFunction
 * @param <K> Type of the key.
 * @param <I> Type of the input elements.
 * @param <O> Type of the output elements.
 */
public abstract class CpcKeyedProcessFunction<K, I, O> extends SketchKeyedProcessFunction<K, I, O> {

    private static final long serialVersionUID = 1L;

    protected ValueState<CpcSketch> cpc;

    protected int lgK;
    protected long seed;

    public CpcKeyedProcessFunction() {
        this.lgK = DEFAULT_LG_K;
        this.seed = DEFAULT_UPDATE_SEED;
    }

    public CpcKeyedProcessFunction(int lgK, long seed) {
        if ((lgK < 4) || (lgK > 26)) {
            throw new SketchesArgumentException("LgK must be >= 4 and <= 26: " + lgK);
        }
        this.lgK = lgK;
        this.seed = seed;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        ValueStateDescriptor<CpcSketch> cpcSketchStateDescriptor = new ValueStateDescriptor<>("CpcSketchState",
                new CpcTypeSerializer(CpcSketch.class, getRuntimeContext().getExecutionConfig()));
        cpc = getRuntimeContext().getState(cpcSketchStateDescriptor);
    }
}
