package com.artemistechnica.federation.services;

import com.artemistechnica.commons.utils.EitherE;

import java.util.function.Function;

import static com.artemistechnica.federation.processing.Pipeline.Step.step;
import static com.artemistechnica.federation.processing.Pipeline.pipeline;

public interface Federation extends Authorization, Metrics {

    default Function<Context, EitherE<String>> federate(Context ctx, Function<Context, Context> proxyFn) {

        Authorization.Context authCtx = new Authorization.Context();
        Metrics.Context metricsCtx = new Metrics.Context();

        Function<Authorization.Context, EitherE<String>> authFn = authorization(authCtx, c -> {
            System.out.printf("Authorizing %s\n", c);
            return c;
        });
        Function<Metrics.Context, EitherE<String>> metricsFn = metrics(metricsCtx, c -> {
            System.out.printf("Metrics %s\n", c);
            return c;
        });


        return pipeline(
                (c) -> "Success!",
                step((c) -> metricsFn.apply(c.metrics).map(result -> c.tick(String.format("Step 1 Federation Pipeline\n\tResult: %s", result)))),
                step(this::preCheck),
                step((c) -> metricsFn.apply(c.metrics).map(result -> c.tick(String.format("Step 2 Federation Pipeline\n\tResult: %s", result)))),
                step((c) -> authFn.apply(c.mkAuthContext()).map(c::setAuthValue)),
                step((c) -> metricsFn.apply(c.metrics).map(result -> c.tick(String.format("Step 3 Federation Pipeline\n\tResult: %s", result)))),
                step((c) -> proxy(c, proxyFn)),
                step((c) -> metricsFn.apply(c.metrics).map(result -> c.tick(String.format("Step 4 Federation Pipeline\n\tResult: %s", result)))),
                step(this::postCheck),
                step((c) -> metricsFn.apply(c.metrics).map(result -> c.tick(String.format("Step 5 Federation Pipeline\n\tResult: %s", result))))
        );
    }

    default <A> EitherE<A> proxy(Context ctx, Function<Context, A> proxyFn) {
        return retry(3, () -> proxyFn.apply(ctx));
    }

    private EitherE<Context> preCheck(Context ctx) {
        return retry(3, () -> ctx);
    }

    private EitherE<Context> postCheck(Context ctx) {
        return retry(3, () -> ctx);
    }


    class Context {

        public String value = "";
        public Metrics.Context metrics = new Metrics.Context();
        public String authValue = "";

        public Context(String value) {
            this.value = value;
        }

        public Authorization.Context mkAuthContext() {
            return new Authorization.Context();
        }

        public Context tick(String value) {
            metrics = metrics.tick(value);
            return this;
        }

        public Context setAuthValue(String value) {
            this.authValue = value;
            return this;
        }

        public static Context mk(String value) {
            return new Context(value);
        }
    }
}
