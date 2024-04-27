package com.artemistechnica.commons.utils;

public class Pair<A, B> {
    public final A left;
    public final B right;

    public Pair(A left, B right) {
        this.left   = left;
        this.right  = right;
    }

    public static <A, B> Pair<A, B> pair(A a, B b) {
        return new Pair<>(a, b);
    }
}
