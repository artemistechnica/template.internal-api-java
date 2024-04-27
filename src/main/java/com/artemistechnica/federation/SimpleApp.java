package com.artemistechnica.federation;

import com.artemistechnica.federation.services.Federation;

public class SimpleApp implements Federation {

    public void doWork() {

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
