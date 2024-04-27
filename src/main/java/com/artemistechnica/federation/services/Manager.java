package com.artemistechnica.federation.services;

import com.artemistechnica.commons.utils.EitherE;
import com.artemistechnica.commons.utils.Retry;

import java.util.function.Function;

public interface Manager extends Retry {

    default <A> EitherE<A> manage(Context ctx, Function<Context, A> accessFn) {
        return retry(3, () -> accessFn.apply(ctx));
    }

    class Context {

    }
}
