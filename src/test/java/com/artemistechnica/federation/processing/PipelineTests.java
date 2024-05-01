package com.artemistechnica.federation.processing;

import com.artemistechnica.commons.utils.EitherE;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class PipelineTests implements Pipeline {

    @Test
    public void testSimplePipeline() {
        List<Function<Integer, EitherE<Integer>>> fns = IntStream.rangeClosed(1, 1000000).boxed().toList()
                .stream().map(i -> (Function<Integer, EitherE<Integer>>) integer -> EitherE.success(integer + 1)).toList();

        Function<Integer, EitherE<Integer>>[] steps = fns.<Function<Integer, EitherE<Integer>>>toArray(new Function[0]);
        Function<Integer, EitherE<PipelineResult.Materializer<Integer>>> pipelineFn = this.<Integer>pipeline(steps);
        EitherE<Integer> result = pipelineFn.apply(0).flatMapE(mat -> mat.materialize(i -> i));
        assert(result.isRight());
        assert(result.right.isPresent());
        assert(result.right.get() == 1000000);
    }
}
