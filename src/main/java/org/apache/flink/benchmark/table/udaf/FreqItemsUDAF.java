package org.apache.flink.benchmark.table.udaf;

import org.apache.datasketches.frequencies.ErrorType;
import org.apache.datasketches.frequencies.ItemsSketch;
import org.apache.flink.core.function.table.frequencies.ItemsUDAFunction;

import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of ItemsUDAFunction
 */
public class FreqItemsUDAF extends ItemsUDAFunction<List<Long>, Long> {

    private static final long serialVersionUID = 1L;

    public FreqItemsUDAF(int maxMapSize, int topK) {
        super(maxMapSize, topK);
    }

    @Override
    public ItemsSketch<Long> createAccumulator() {
        return super.createAccumulator();
    }

    public void accumulate(ItemsSketch<Long> acc, Long iValue) {
        if (iValue != null) {
            acc.update(iValue);
        }
    }

    @Override
    public List<Long> getValue(ItemsSketch<Long> accumulator) {
        final ItemsSketch.Row<Long>[] result = accumulator.getFrequentItems(ErrorType.NO_FALSE_POSITIVES);
        List<Long> res = new ArrayList<>();
        for (int i = 0; i < Math.min(result.length, topK); i++) {
            res.add(result[i].getItem());
        }
        return res;
    }

    public void resetAccumulator(ItemsSketch<Long> acc) {
        super.resetAccumulator(acc);
    }

    @Override
    public void merge(ItemsSketch<Long> acc, Iterable<ItemsSketch<Long>> it) {
        super.merge(acc, it);
    }
}