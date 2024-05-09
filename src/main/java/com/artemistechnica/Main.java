package com.artemistechnica;

import com.artemistechnica.commons.datatypes.Either;
import com.artemistechnica.commons.datatypes.EitherE;
import com.artemistechnica.commons.errors.Try;
import com.artemistechnica.commons.processing.Pipeline;
import com.artemistechnica.federation.services.Federation;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;


public class Main implements Try {
    public static void main(String[] args) {
        App app = new App();
//        app.doThing();

        Function<Federation.Context, EitherE<Pipeline.PipelineResult.Materializer<Federation.Context>>> federationFn = app.federate(ctx -> {
            ctx.value += "\n\t8. |X| Federation: proxy (App provided)";
            return ctx;
        });
        Federation.Context context = new Federation.Context("App Pipeline Stages: ");
        EitherE<String> federationResult = federationFn.apply(context).flatMapE(f -> f.materialize(c -> c.value));
        System.out.println(federationResult.right.get());
    }

    public static class App implements Federation {

        public void doThing() {
            Either<String, Integer> e0 = Either.right(1);
            Either<String, Integer> e1 = e0.map(i -> i + 1);
            Either<String, String> e2 = e0.flatMap(i -> Either.right(Integer.toString(i + 42)));

            Either<String, Integer> e3 = Either.<String, Integer>left("ERROR");
            Either<String, Integer> e4 = e3.map(i -> i + 100);

            try {
                Either<String, Integer> e5 = Either.<String, Integer>right(null);
            } catch (Exception e) {
                System.out.printf("EXPECTED ERROR: %s", e.getMessage());
            }

            EitherE<Integer> e6 = tryFunc(() -> {
                throw new RuntimeException("Something bad happened");
            });

            AtomicReference<Integer> count = new AtomicReference<>(0);
            EitherE<Integer> res0 = retry(3, () -> {
                int c = count.get();
                count.set(c + 1);
                return 1 / c;
            });


            List<Function<Context, EitherE<Context>>> stps = List.of(
                    this::preCheck,
                    (ctx) -> tryFunc(() -> Context.mk(doWork(ctx))),
                    this::postCheck
            );


            Function<Context, EitherE<PipelineResult.Materializer<Context>>> pipelineFn0 = pipeline(
                    this::preCheck,
                    (ctx) -> tryFunc(() -> Context.mk(doWork(ctx))),
                    this::postCheck
            );

            Function<Context, EitherE<Context>>[] arr = stps.toArray(new Function[0]);

            Function<Context, EitherE<PipelineResult.Materializer<Context>>> pipelineFn1 = pipeline(arr);

            Function<Context, EitherE<PipelineResult.Materializer<Context>>> pipelineFn2 = pipeline(
                    this::preCheck,
                    (ctx) -> tryFunc(() -> Context.mk(doWorkBad(ctx))),
                    this::postCheck
            );

            EitherE<String> result0 = pipelineFn0.apply(new Context("")).flatMapE(mat -> mat.materialize(c -> c.value));
            EitherE<String> result1 = pipelineFn1.apply(new Context("")).flatMapE(mat -> mat.materialize(c -> c.value));
            EitherE<String> result2 = pipelineFn2.apply(new Context("Is this going to work?")).flatMapE(mat -> mat.materialize(c -> c.value));;
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