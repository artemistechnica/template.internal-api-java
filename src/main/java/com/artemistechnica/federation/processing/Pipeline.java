package com.artemistechnica.federation.processing;

import com.artemistechnica.commons.utils.EitherE;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class Pipeline {

    public static <A extends Context, B> Function<A, EitherE<B>> pipeline(List<Function<A, EitherE<A>>> steps, Function<A, B> mapFn) {
        return (A ctx) -> {
            EitherE<Supplier<EitherE<A>>> x = steps.stream().reduce(
                    EitherE.success(() -> EitherE.success(ctx)),
                    (acc, s) -> acc.map(r -> () -> r.get().flatMapE(c -> s.apply(c))),
                    (a, a2) -> a2
            );

            return (EitherE<B>) x.flatMapE(fn -> fn.get().map(c -> mapFn.apply(c)));
        };
    }

    public static class Step {

//        public static <A extends Context> Function<A, Supplier<EitherE<A>>> step(Function<A, EitherE<A>> fn) {
//            return (A ctx) -> () -> fn.apply(ctx);//EitherE.failure(SimpleError.create("Method not implemented"));
//        }

        public static <A extends Context> Function<A, EitherE<A>> step(Function<A, EitherE<A>> fn) {
            return (A ctx) -> fn.apply(ctx);//EitherE.failure(SimpleError.create("Method not implemented"));
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
