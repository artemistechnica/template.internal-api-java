package com.artemistechnica.federation.services;

import com.artemistechnica.commons.utils.EitherE;
import com.artemistechnica.commons.utils.Retry;
import com.artemistechnica.federation.processing.Pipeline;

import java.util.function.Function;


public interface Authorization extends Pipeline, Retry {

    default Function<Context, EitherE<PipelineResult.Materializer<Context>>> authorization(Context ctx, Function<Context, Context> accessFn) {
        return pipeline(
                this::preCheck,
                (c) -> access(c, accessFn),
                this::postCheck
        );
    }

    default <A> EitherE<A> access(Context ctx, Function<Context, A> accessFn) {
        return retry(3, () -> accessFn.apply(ctx));
    }

    private EitherE<Context> preCheck(Context ctx) {
        return retry(3, () -> {
            System.out.println("Authorization pre-check");
            ctx.logging += "\n\t2. |X| Authorization: pre-check";
            return ctx;
        });
    }

    private EitherE<Context> postCheck(Context ctx) {
        return retry(3, () -> {
            System.out.println("Authorization post-check");
            ctx.logging += "\n\t4. |X| Authorization: post-check";
            return ctx;
        });
    }

    class Context {
        public String value;
        public String logging = "";
        public Context(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
