package com.artemistechnica.federation;

import com.artemistechnica.commons.datatypes.EitherE;
import com.artemistechnica.commons.services.Federation;
import com.artemistechnica.commons.utils.HelperFunctions;

import java.util.function.Function;

public class SimpleApp implements Federation {

    public void doWork() {

        Function<Context, EitherE<PipelineResult.Materializer<Context>>> r = federate(HelperFunctions::identity);

        /**
         * pipeline( ctx -> {
         *      step(ctx.step(), ctx -> { }),
         *      step(ctx.step(), ctx -> { }),
         *      step(ctx.step(), ctx -> { })
         *  }
         * ).apply(new Context())
         *
         * <A> </A>Function<Context, A> step(ctx -> A) { (ctx) -> 
         */
    }
}
