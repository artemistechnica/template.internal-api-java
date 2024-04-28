package com.artemistechnica.federation.processing;

import com.artemistechnica.commons.utils.EitherE;
import com.artemistechnica.commons.utils.Retry;
import com.artemistechnica.federation.services.Metrics;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Pipeline {

    default <A> Function<A, EitherE<PipelineResult.Materializer<A>>> pipeline(Function<A, EitherE<A>>... stages) {
        return (A ctx) -> {
            // Wrap the stages as a lazy execution
            EitherE<Supplier<EitherE<A>>> resultE = Arrays.stream(stages)
                    // Wrap invocation in a [[step]] for later execution
                    .map(this::step)
                    // Reduce the pipeline steps into a single result
                    .reduce(
                            // Initial accumulator
                            EitherE.success(() -> EitherE.success(ctx)),
                            // Accumulate
                            (acc, step) -> acc.map(r -> () -> r.get().flatMapE(step)),
                            // Reduce - return the last result
                            (acc0, acc1) -> acc1
                    );
            // Execute
            return resultE.flatMapE(fn -> fn.get().map(PipelineResult::construct));
        };
    }

    private <A> Function<A, EitherE<A>> step(Function<A, EitherE<A>> fn) {
        Function<Metrics.Context, EitherE<Metrics.Context>> m = new Metrics() {}.metrics(new Metrics.Context("PIPELINE STEP"));
        return (A ctx) -> m.apply(new Metrics.Context()).flatMapE(c -> fn.apply(ctx));
    }

    interface PipelineResult {

        class Materializer<A> implements Retry {
            private final A result;

            private Materializer(A result) { this.result = result; }

            public <B> EitherE<B> materialize(Function<A, B> matFn) {
                return retry(3, () -> matFn.apply(result));
            }
        }

        static <A> Materializer<A> construct(A result) {
            return new Materializer<>(result);
        }
    }
}
