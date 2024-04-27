package com.artemistechnica.commons.utils;

import com.artemistechnica.commons.errors.SimpleError;

import java.util.function.Supplier;

public interface Try {

    default <A> EitherE<A> tryFn(Supplier<A> fn) {
        try { return EitherE.success(fn.get()); } catch (Exception e) {
            System.out.printf("CAUGHT EXCEPTION %s\n", e.getMessage());
            return EitherE.failure(SimpleError.create(e));
        }
    }
}
