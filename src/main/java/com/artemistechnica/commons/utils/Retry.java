package com.artemistechnica.commons.utils;

import com.artemistechnica.commons.errors.SimpleError;

import java.util.function.Supplier;

public interface Retry extends Try {

    /**
     *
     * @param times
     * @param fn
     * @return
     * @param <A>
     */
    default <A> EitherE<A> retry(int times, Supplier<A> fn) {
        EitherE<A> result = EitherE.failure(SimpleError.create("Function did not execute"));
        while (times > 0) {
            --times;
            result = tryFn(fn);
            if (result.isRight()) break;
        }
        return result;
    }
}
