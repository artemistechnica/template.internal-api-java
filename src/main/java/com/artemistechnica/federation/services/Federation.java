package com.artemistechnica.federation.services;

import com.artemistechnica.commons.utils.EitherE;
import com.artemistechnica.federation.processing.Pipeline;

import java.util.UUID;
import java.util.function.Function;

public interface Federation extends Pipeline, Authorization, Manager, Metrics {

    default Function<Context, EitherE<PipelineResult.Materializer<Context>>> federate(Function<Context, Context> proxyFn) {
        return pipeline(
                this::preCheck,
                this::authorize,
                this::manage,
                proxy(proxyFn),
                this::postCheck
        );
    }

    private EitherE<Context> authorize(Context ctx) {
        System.out.println("Beginning Authorization");
        Function<Authorization.Context, EitherE<PipelineResult.Materializer<Authorization.Context>>> authFn = authorization(ctx.authContext, c -> {
            System.out.printf("Authorization: Authorizing (Federation Provided)%s\n", c);
            c.value = UUID.randomUUID().toString();
            c.logging += "\n\t3. |X| Authorization: authorizing (Federation provided)";
            return c;
        });
        return authFn.apply(ctx.authContext).flatMapE(mat -> mat.materialize(ctx::setAuthContext));
    }

    private EitherE<Context> manage(Context ctx) {
        System.out.println("Beginning Managerial Duties");
        Function<Manager.Context, EitherE<PipelineResult.Materializer<Manager.Context>>> manageFn = manage(ctx.managerContext, c -> {
            System.out.printf("Manager: Managerial Duties (Federation Provided)%s\n", c);
            c.logging += "\n\t6. |X| Managerial Duties: managing (Federation provided)";
            return c;
        });
        return manageFn.apply(ctx.managerContext).flatMapE(mat -> mat.materialize(ctx::setManagerContext));
    }

    default <A> Function<Context, EitherE<A>> proxy(Function<Context, A> proxyFn) {
        return (Context context) -> retry(3, () -> proxyFn.apply(context));
    }

    private EitherE<Context> preCheck(Context ctx) {
        return retry(3, () -> {
            System.out.println("Federation pre-check");
            ctx.value += "\n\t1. |X| Federation: pre-check";
            return ctx;
        });
    }

    private EitherE<Context> postCheck(Context ctx) {
        return retry(3, () -> {
            System.out.println("Federation post-check");
            ctx.value += "\n\t9. |X| Federation: post-check";
            return ctx;
        });
    }


    class Context {

        public String value = "";
        public Metrics.Context metrics = new Metrics.Context();
        public Authorization.Context authContext = new Authorization.Context("");
        public Manager.Context managerContext = new Manager.Context("");

        public Context(String value) {
            this.value = value;
        }

        public Context tick(String value) {
            metrics = metrics.tick(value);
            return this;
        }

        public Context setAuthContext(Authorization.Context authContext) {
            this.authContext = authContext;
            this.value += authContext.logging;
            return this;
        }

        public Context setManagerContext(Manager.Context managerContext) {
            this.managerContext = managerContext;
            this.value += managerContext.logging;
            return this;
        }

        public static Context mk(String value) {
            return new Context(value);
        }
    }
}
