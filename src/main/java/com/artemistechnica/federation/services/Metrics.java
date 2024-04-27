package com.artemistechnica.federation.services;

import com.artemistechnica.commons.utils.EitherE;
import com.artemistechnica.commons.utils.Retry;

import java.util.function.Function;

import static com.artemistechnica.federation.processing.Pipeline.Step.step;
import static com.artemistechnica.federation.processing.Pipeline.pipeline;

public interface Metrics extends Retry {

    default Function<Context, EitherE<String>> metrics(Context ctx, Function<Context, Context> metricsFn) {
        return pipeline(
                (c) -> "Metrics Success!",
                step(this::preCheck),
                step((c) -> applyMetrics(c, metricsFn)),
                step(this::postCheck)
        );
    }

    default <A> EitherE<A> applyMetrics(Context ctx, Function<Context, A> metricsFn) {
        return retry(3, () -> metricsFn.apply(ctx));
    }

    private EitherE<Context> preCheck(Context ctx) {
        return retry(3, () -> ctx);
    }

    private EitherE<Context> postCheck(Context ctx) {
        return retry(3, () -> ctx);
    }

    class Context {
        public String value;

        public Context() {
            this.value = "";
        }

        public Context(String value) {
            this.value = value;
        }

        public Context tick(String value) {
            this.value = String.format("%s\n%s", this.value, value);
            return this;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static Context mk(String value) {
            return new Context(value);
        }
    }
}
