package com.artemistechnica.commons;

import java.util.Optional;
import java.util.function.Function;

/**
 * Either
 * @param <A>
 * @param <B>
 */
public class Either<A, B> {
    public final Optional<A> left;
    public final Optional<B> right;

    protected Either(A left, B right) {
        this.left   = Optional.ofNullable(left);
        this.right  = Optional.ofNullable(right);
        if (this.left.isEmpty() && this.right.isEmpty())
            throw new RuntimeException("Either must have exactly one defined member. Found both members as null");
        if (this.left.isPresent() && this.right.isPresent())
            throw new RuntimeException("Either must have exactly one defined member. Found both members as defined");
    }

    /**
     * Map
     * @param fn
     * @return
     * @param <C>
     */
    public <C> Either<A, C> map(Function<B, C> fn) {
        return left.map(l -> Either.<A, C>left(l)).orElseGet(() -> right.map(right -> Either.<A, C>right(fn.apply(right))).get());
    }

    /**
     * Flatmap
     * @param fn
     * @return
     * @param <C>
     */
    public <C, D extends Either<A, C>> Either<A, C> flatMap(Function<B, D> fn) {
        return left.map(l -> Either.<A, C>left(l)).orElseGet(() -> right.map(fn).get());
    }

    /**
     * Constructor
     * @param left
     * @return
     * @param <A>
     * @param <B>
     */
    public static <A, B> Either<A, B> left(A left) {
        return new Either<>(left, null);
    }

    /**
     * Constructor
     * @param right
     * @return
     * @param <A>
     * @param <B>
     */
    public static <A, B> Either<A, B> right(B right) {
        return new Either<>(null, right);
    }

    /**
     *
     * @return
     */
    public boolean isRight() {
        return right.isPresent();
    }

    /**
     *
     * @return
     */
    public boolean isLeft() {
        return left.isPresent();
    }
}
