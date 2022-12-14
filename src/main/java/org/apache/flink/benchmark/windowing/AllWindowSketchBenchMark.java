package org.apache.flink.benchmark.windowing;


import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.benchmark.data.Tuple3SourceGenerator;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.benchmark.operator.aggregate.CpcAggregate;
import org.apache.flink.benchmark.operator.aggregate.HllAggregate;
import org.apache.flink.core.datastream.SketchAllWindowedStream;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.AllWindowedStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.datagen.DataGeneratorSource;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataStream API (AllWindowedStream) Benchmark For Sketch
 */
public class AllWindowSketchBenchMark {

    private static final Logger LOG = LoggerFactory.getLogger(AllWindowSketchBenchMark.class);

    public static void main(String[] args) throws Exception {

        final ParameterTool params = ParameterTool.fromArgs(args);

        final StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment();
                //.createLocalEnvironmentWithWebUI(new Configuration());

        env.setParallelism(4);
        env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);

        long rowsPerSecond = Long.parseLong(params.get("rowsPerSecond","25000"));
        long numberOfRows = Long.parseLong(params.get("numberOfRows","1000000000"));

        DataStream<Tuple3<String, Long, String>> source = env
                .addSource(new DataGeneratorSource<>(
                        new Tuple3SourceGenerator(),
                        rowsPerSecond,
                        numberOfRows))
                .returns(new TypeHint<Tuple3<String, Long, String>>() {})
                .name("source");

        AllWindowedStream<Tuple3<String, Long, String>, TimeWindow> windowAll = source
                .windowAll(TumblingProcessingTimeWindows.of(Time.seconds(30)));

        SketchAllWindowedStream<Tuple3<String, Long, String>, TimeWindow> sketchAllWindowedStream =
                new SketchAllWindowedStream<>(windowAll, env.getConfig());

        DataStream<Double> estimate;

        if ("cpc".equals(params.get("sketch", "hll"))) {
            estimate = sketchAllWindowedStream
                    //.cpc(2).name("cpc");
                    .aggregate(new CpcAggregate()).name("cpc");
        } else {
            estimate = sketchAllWindowedStream
                    //.hll(2).name("hll");
                    .aggregate(new HllAggregate()).name("hll");
        }
        estimate.print();
        env.execute("AllWindowSketchBenchMark");
    }
}
