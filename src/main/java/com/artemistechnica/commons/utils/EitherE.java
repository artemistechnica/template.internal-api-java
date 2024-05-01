package com.artemistechnica.commons.utils;

import com.artemistechnica.commons.Either;
import com.artemistechnica.commons.errors.SimpleError;

import java.util.function.Function;

public class EitherE<A> extends Either<SimpleError, A> {

    private EitherE(SimpleError left, A right) {
        super(left, right);
    }

    @Override
    public <C> EitherE<C> map(Function<A, C> fn) {
        return left.map(l -> EitherE.<C>failure(l)).orElseGet(() -> right.map(right -> EitherE.<C>success(fn.apply(right))).get());
    }

//    public <B> EitherE<B> flatMapE(Function<A, EitherE<B>> fn) {
////        return (this.isLeft()) ? EitherE.<B>failure(this.left.get()) : right.map(v -> fn.apply(v)).get();
//        return this.left.map(err -> EitherE.<B>failure(err)).orElse(this.right.map(v -> fn.apply(v)).get());//.orElseGet(() -> this.right.map(v -> fn.apply(v)).get());
//    }

    public <B> EitherE<B> flatMapE(Function<A, EitherE<B>> fn) {
        return this.left.map(err -> EitherE.<B>failure(err)).orElse(this.right.map(v -> fn.apply(v)).get());
    }

    public static <A> EitherE<A> failure(SimpleError error) {
        return new EitherE<>(error, null);
    }

    public static <A> EitherE<A> success(A right) {
        return new EitherE<>(null, right);
    }
}
