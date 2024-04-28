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
            EitherE<Supplier<EitherE<A>>> resultE = Arrays.stream(stages)
                    .map(fn -> Step.<A>step(fn::apply))
                    .reduce(
                            EitherE.success(() -> EitherE.success(ctx)),
                            (acc, s) -> acc.map(r -> () -> r.get().flatMapE(s::apply)),
                            (a, a2) -> a2
                    );
            return resultE.flatMapE(fn -> fn.get().map(PipelineResult::construct));
        };
    }

     class Step implements Metrics {

        static <A> Function<A, EitherE<A>> step(Function<A, EitherE<A>> fn) {
            Function<Context, EitherE<Context>> m = new Metrics() {}.metrics(new Context("PIPELINE STEP"));
            return (A ctx) -> m.apply(new Context()).flatMapE(c -> fn.apply(ctx));
        }
    }

    interface PipelineResult {

        class Materializer<A> implements Retry {

            private final A result;

            public Materializer(A result) { this.result = result; }

            public <B> EitherE<B> materialize(Function<A, B> matFn) {
                return retry(3, () -> matFn.apply(result));
            }
        }

        static <A> Materializer<A> construct(A result) {
            return new Materializer<>(result);
        }
    }

    public static class Context {
        public String value;
        public Context(String value) {
            this.value = value;
        }
        public static Context mk(String value) {
            return new Context(value);
        }
    }
}
