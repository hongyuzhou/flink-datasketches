package org.apache.flink.benchmark.operator.process;

import org.apache.datasketches.cpc.CpcSketch;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.function.process.distinct.CpcKeyedProcessFunction;
import org.apache.flink.util.Collector;

public class CpcKeyedProcess extends CpcKeyedProcessFunction<String, Tuple3<String, Long, String>, Double> {

    private static final long serialVersionUID = 1L;

    public CpcKeyedProcess() {
        super();
    }

    public CpcKeyedProcess(int lgK, long seed) {
        super(lgK, seed);
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
    }

    @Override
    public void processElement(Tuple3<String, Long, String> value, Context ctx, Collector<Double> out) throws Exception {
        CpcSketch sketch = cpc.value();

        if (cpc.value() == null) {
            sketch = new CpcSketch(lgK, seed);
        }

        sketch.update(value.f2);
        pvCountInc();
        cpc.update(sketch);

        out.collect(sketch.getEstimate());
    }
}
