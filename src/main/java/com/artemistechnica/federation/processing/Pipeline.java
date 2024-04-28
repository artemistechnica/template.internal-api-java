package com.artemistechnica.federation.processing;

import com.artemistechnica.commons.utils.EitherE;
import com.artemistechnica.commons.utils.Retry;
import com.artemistechnica.federation.services.Metrics;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Pipeline {

//    public static <A, B> Function<A, EitherE<B>> pipeline(List<Function<A, EitherE<A>>> steps, Function<A, B> mapFn) {
//        return (A ctx) -> {
//            EitherE<Supplier<EitherE<A>>> x = steps.stream().reduce(
//                    EitherE.success(() -> EitherE.success(ctx)),
//                    (acc, s) -> acc.map(r -> () -> r.get().flatMapE(c -> s.apply(c))),
//                    (a, a2) -> a2
//            );
//
//            return (EitherE<B>) x.flatMapE(fn -> fn.get().map(c -> mapFn.apply(c)));
//        };
//    }

//    default <A, B> Function<A, EitherE<B>> pipeline(Function<A, B> mapFn, Function<A, EitherE<A>>... steps) {
//        return (A ctx) -> {
//            EitherE<Supplier<EitherE<A>>> x = Arrays.stream(steps).reduce(
//                    EitherE.success(() -> EitherE.success(ctx)),
//                    (acc, s) -> acc.map(r -> () -> r.get().flatMapE(c -> s.apply(c))),
//                    (a, a2) -> a2
//            );
//
//            return (EitherE<B>) x.flatMapE(fn -> fn.get().map(c -> mapFn.apply(c)));
//        };
//    }

    default <A, B> Function<A, EitherE<B>> pipeline(Function<A, B> mapFn, Function<A, EitherE<A>>... stages) {
        return (A ctx) -> {
            EitherE<Supplier<EitherE<A>>> x = Arrays.stream(stages).map(fn -> Step.<A>step((c) -> fn.apply(c))).reduce(
                    EitherE.success(() -> EitherE.success(ctx)),
                    (acc, s) -> acc.map(r -> () -> r.get().flatMapE(c -> s.apply(c))),
                    (a, a2) -> a2
            );

            return (EitherE<B>) x.flatMapE(fn -> fn.get().map(c -> mapFn.apply(c)));
        };
    }

    default <A> Function<A, EitherE<PipelineResult.Materializer<A>>> pipeline2(Function<A, EitherE<A>>... stages) {
        return (A ctx) -> {
            EitherE<Supplier<EitherE<A>>> resultE = Arrays.stream(stages)
                    .map(fn -> Step.<A>step((c) -> fn.apply(c)))
                    .reduce(
                            EitherE.success(() -> EitherE.success(ctx)),
                            (acc, s) -> acc.map(r -> () -> r.get().flatMapE(c -> s.apply(c))),
                            (a, a2) -> a2
                    );
            return resultE.flatMapE(fn -> fn.get().map(result -> PipelineResult.construct(result)));
        };
    }

     class Step implements Metrics {

//        public static <A extends Context> Function<A, Supplier<EitherE<A>>> step(Function<A, EitherE<A>> fn) {
//            return (A ctx) -> () -> fn.apply(ctx);//EitherE.failure(SimpleError.create("Method not implemented"));
//        }

        static <A> Function<A, EitherE<A>> step(Function<A, EitherE<A>> fn) {
            Function<Context, EitherE<Context>> m = new Metrics() {
            }.metrics(new Context("PIPELINE STEP"));
            return (A ctx) -> {
                return m.apply(new Metrics.Context())
                        .flatMapE(c -> fn.apply(ctx));
//                return fn.apply(ctx);
            };
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
