package com.artemistechnica.federation.services;

import com.artemistechnica.commons.utils.EitherE;
import com.artemistechnica.federation.processing.Pipeline;

import java.util.function.Function;

public interface Federation extends Pipeline, Authorization, Metrics {

    default Function<Context, EitherE<String>> federate(Context ctx, Function<Context, Context> proxyFn) {

        Authorization.Context authCtx = new Authorization.Context();

        Function<Authorization.Context, EitherE<String>> authFn = authorization(authCtx, c -> {
            System.out.printf("Authorizing %s\n", c);
            return c;
        });

        return pipeline(
                (c) -> "Success!",
                this::preCheck,
                (c) -> {
                    System.out.println("Beginning Authorization");
                    return authFn.apply(c.mkAuthContext()).map(c::setAuthValue);
                },
                (c) -> {
                    System.out.println("Beginning Proxy");
                    return proxy(c, proxyFn);
                },
                this::postCheck
        );
    }

    default <A> EitherE<A> proxy(Context ctx, Function<Context, A> proxyFn) {
        return retry(3, () -> proxyFn.apply(ctx));
    }

    private EitherE<Context> preCheck(Context ctx) {
        return retry(3, () -> {
            System.out.println("Federation pre-check");
            return ctx;
        });
    }

    private EitherE<Context> postCheck(Context ctx) {
        return retry(3, () -> {
            System.out.println("Federation post-check");
            return ctx;
        });
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
