package com.artemistechnica.federation.services;

import com.artemistechnica.commons.utils.EitherE;
import com.artemistechnica.commons.utils.Retry;
import com.artemistechnica.federation.processing.Pipeline;

import java.util.function.Function;

public interface Manager extends Pipeline, Retry {

    default Function<Context, EitherE<PipelineResult.Materializer<Context>>> manage(Context ctx, Function<Context, Context> manageFn) {
        return pipeline(
                this::preCheck,
                (c) -> manageInternal(c, manageFn),
                this::postCheck
        );
    }

    private <A> EitherE<A> manageInternal(Context ctx, Function<Context, A> manageFn) {
        return retry(3, () -> manageFn.apply(ctx));
    }

    private EitherE<Context> preCheck(Context ctx) {
        return retry(3, () -> {
            System.out.println("Manager pre-check");
            ctx.logging += "\n\t5. |X| Manager: pre-check";
            return ctx;
        });
    }

    private EitherE<Context> postCheck(Context ctx) {
        return retry(3, () -> {
            System.out.println("Manager post-check");
            ctx.logging += "\n\t7. |X| Manager: post-check";
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
