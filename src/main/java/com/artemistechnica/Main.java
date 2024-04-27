package com.artemistechnica;

import com.artemistechnica.commons.Either;
import com.artemistechnica.commons.utils.EitherE;
import com.artemistechnica.commons.utils.Retry;
import com.artemistechnica.commons.utils.Try;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.artemistechnica.federation.processing.Pipeline.Context;
import static com.artemistechnica.federation.processing.Pipeline.Step.step;
import static com.artemistechnica.federation.processing.Pipeline.pipeline;

public class Main implements Try {
    public static void main(String[] args) {
        App app = new App();
        app.doThing();
    }

    public static class App implements Retry {

        public void doThing() {
            Either<String, Integer> e0 = Either.right(1);
            Either<String, Integer> e1 = e0.map(i -> i + 1);
            Either<String, String> e2 = e0.flatMap(i -> Either.right(Integer.toString(i + 42)));

            Either<String, Integer> e3 = Either.<String, Integer>left("ERROR");
            Either<String, Integer> e4 = e3.map(i -> i + 100);

            try {
                Either<String, Integer> e5 = Either.<String, Integer>right(null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            EitherE<Integer> e6 = tryFn(() -> {
                throw new RuntimeException("Something bad happened");
            });

            AtomicReference<Integer> count = new AtomicReference<>(0);
            EitherE<Integer> res0 = retry(3, () -> {
                int c = count.get();
                count.set(c + 1);
                return 1 / c;
            });


            List<Function<Context, EitherE<Context>>> stps = List.of(
                    step(this::preCheck),
                    step((ctx) -> tryFn(() -> Context.mk(doWork(ctx)))),
                    step(this::postCheck)
            );


            Function<Context, EitherE<String>> pipelineFn0 = pipeline(
                    List.of(
                            step(this::preCheck),
                            step((ctx) -> tryFn(() -> Context.mk(doWork(ctx)))),
                            step(this::postCheck)
                    ),
                    this::mkResult
            );

            Function<Context, EitherE<String>> pipelineFn1 = pipeline(
                    stps,
                    this::mkResult
            );

            Function<Context, EitherE<String>> pipelineFn2 = pipeline(
                    List.of(
                            step(this::preCheck),
                            step((ctx) -> tryFn(() -> Context.mk(doWorkBad(ctx)))),
                            step(this::postCheck)
                    ),
                    this::mkResult
            );

            EitherE<String> result0 = pipelineFn0.apply(new Context(""));
            EitherE<String> result1 = pipelineFn1.apply(new Context(""));
            EitherE<String> result2 = pipelineFn2.apply(new Context("Is this going to work?"));

        }

        private EitherE<Context> preCheck(Context ctx) {
            return EitherE.success(ctx);
        }

        private EitherE<Context> postCheck(Context ctx) {
            return EitherE.success(ctx);
        }

        private String doWork(Context ctx) {
            return String.format("Previous: %s\nNew: %s, Total: %s %s\n", ctx.value, "Hello, World!", ctx.value, "Hello, World!");
        }

        private String doWorkBad(Context ctx) {
            throw new RuntimeException(String.format("ERROR!\nPrevious value: %s\nError: Method not implemented", ctx.value));
        }

        private String mkResult(Context ctx) {
            return String.format("Job complete!\n\tResult: %s", ctx.value);
        }
    }
}