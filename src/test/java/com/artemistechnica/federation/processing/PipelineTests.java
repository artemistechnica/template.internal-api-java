package com.artemistechnica.federation.processing;

import com.artemistechnica.commons.utils.EitherE;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class PipelineTests implements Pipeline {

    @Test
    public void testSimplePipeline() {
        int count = 100;
        List<Function<Integer, EitherE<Integer>>> fns = IntStream.rangeClosed(1, count).boxed().toList()
                .stream().map(i -> (Function<Integer, EitherE<Integer>>) integer -> EitherE.success(integer + 1)).toList();

        Function<Integer, EitherE<Integer>>[] steps = fns.<Function<Integer, EitherE<Integer>>>toArray(new Function[0]);
        Function<Integer, EitherE<PipelineResult.Materializer<Integer>>> pipelineFn = this.<Integer>pipeline(steps);
        EitherE<Integer> result = pipelineFn.apply(0).flatMapE(mat -> mat.materialize(i -> i));
        assert(result.isRight());
        assert(result.right.isPresent());
        assert(result.right.get() == count);
    }

    @Test
    public void testSimplePipelineWithExtremeNumberOfSteps() {
        long count = 10000000L;
        List<Function<Long, EitherE<Long>>> fns = LongStream.rangeClosed(1, count).boxed().toList()
                .stream().map(i -> (Function<Long, EitherE<Long>>) n -> EitherE.success(n + 1)).toList();

        Function<Long, EitherE<Long>>[] steps = fns.<Function<Long, EitherE<Long>>>toArray(new Function[0]);
        Function<Long, EitherE<PipelineResult.Materializer<Long>>> pipelineFn = this.<Long>pipeline(steps);
        EitherE<Long> result = pipelineFn.apply(0L).flatMapE(mat -> mat.materialize(i -> i));
        assert(result.isRight());
        assert(result.right.isPresent());
        assert(result.right.get() == count);
    }
}
