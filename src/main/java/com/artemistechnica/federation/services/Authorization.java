package com.artemistechnica.federation.services;

import com.artemistechnica.commons.utils.EitherE;
import com.artemistechnica.commons.utils.Retry;

import java.util.function.Function;

import static com.artemistechnica.federation.processing.Pipeline.Step.step;
import static com.artemistechnica.federation.processing.Pipeline.pipeline;

public interface Authorization extends Retry {

    default Function<Context, EitherE<String>> authorization(Context ctx, Function<Context, Context> accessFn) {
        return pipeline(
                (c) -> "Success!",
                step(this::preCheck),
                step((c) -> access(c, accessFn)),
                step(this::postCheck)
        );
    }

//    default EitherE<Context> access(Context ctx, Function<Context, Context> accessFn) {
//        return retry(3, () -> accessFn.apply(ctx));
//    }

    default <A> EitherE<A> access(Context ctx, Function<Context, A> accessFn) {
        return retry(3, () -> accessFn.apply(ctx));
    }

    private EitherE<Context> preCheck(Context ctx) {
        return retry(3, () -> ctx);
    }

    private EitherE<Context> postCheck(Context ctx) {
        return retry(3, () -> ctx);
    }

    class Context {
        @Override
        public String toString() {
            return "Authorization <VALUE>";
        }
    }
}
