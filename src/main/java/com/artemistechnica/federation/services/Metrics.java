package com.artemistechnica.federation.services;

import com.artemistechnica.commons.utils.EitherE;
import com.artemistechnica.commons.utils.Retry;

import java.util.function.Function;

public interface Metrics extends Retry {

    default Function<Context, EitherE<Context>> metrics(Context ctx) {
        return (c) -> applyMetrics(c);
    }

    default EitherE<Context> applyMetrics(Context ctx) {
        return retry(3, () -> {
            System.out.println("APPLYING METRICS!");
            return ctx;
        });
    }

    private EitherE<Context> preCheck(Context ctx) {
        return retry(3, () -> { System.out.printf("Metrics pre-check"); return ctx; });
    }

    private EitherE<Context> postCheck(Context ctx) {
        return retry(3, () -> { System.out.printf("Metrics post-check"); return ctx; });
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
