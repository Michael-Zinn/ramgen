package de.michaelzinn.ramgen;

import io.vavr.Function1;

/**
 * Created by michael on 27.05.17.
 */
public class ThreadingMacro {


    public static <A>
    A doSeq(
            A a
    ) {
        return a;
    }


    public static <A, B>
    B doSeq(
            A a,
            Function1<A, B> ab
    ) {
        return ab.apply(a);
    }


    public static <A, B, C>
    C doSeq(
            A a,
            Function1<A, B> ab,
            Function1<B, C> bc
    ) {
        return bc.apply(ab.apply(a));
    }


    public static <A, B, C, D>
    D doSeq(
            A a,
            Function1<A, B> ab,
            Function1<B, C> bc,
            Function1<C, D> cd
    ) {
        return cd.apply(bc.apply(ab.apply(a)));
    }


    public static <A, B, C, D, E>
    E doSeq(
            A a,
            Function1<A, B> ab,
            Function1<B, C> bc,
            Function1<C, D> cd,
            Function1<D, E> de
    ) {
        return de.apply(cd.apply(bc.apply(ab.apply(a))));
    }


    public static <A, B, C, D, E, F>
    F doSeq(
            A a,
            Function1<A, B> ab,
            Function1<B, C> bc,
            Function1<C, D> cd,
            Function1<D, E> de,
            Function1<E, F> ef
    ) {
        return ef.apply(de.apply(cd.apply(bc.apply(ab.apply(a)))));
    }


    public static <A, B, C, D, E, F, G>
    G doSeq(
            A a,
            Function1<A, B> ab,
            Function1<B, C> bc,
            Function1<C, D> cd,
            Function1<D, E> de,
            Function1<E, F> ef,
            Function1<F, G> fg
    ) {
        return fg.apply(ef.apply(de.apply(cd.apply(bc.apply(ab.apply(a))))));
    }


    public static <A, B, C, D, E, F, G, H>
    H doSeq(
            A a,
            Function1<A, B> ab,
            Function1<B, C> bc,
            Function1<C, D> cd,
            Function1<D, E> de,
            Function1<E, F> ef,
            Function1<F, G> fg,
            Function1<G, H> gh
    ) {
        return gh.apply(fg.apply(ef.apply(de.apply(cd.apply(bc.apply(ab.apply(a)))))));
    }


    public static <A, B, C, D, E, F, G, H, I>
    I doSeq(
            A a,
            Function1<A, B> ab,
            Function1<B, C> bc,
            Function1<C, D> cd,
            Function1<D, E> de,
            Function1<E, F> ef,
            Function1<F, G> fg,
            Function1<G, H> gh,
            Function1<H, I> hi
    ) {
        return hi.apply(gh.apply(fg.apply(ef.apply(de.apply(cd.apply(bc.apply(ab.apply(a))))))));
    }


    public static <A, B, C, D, E, F, G, H, I, J>
    J doSeq(
            A a,
            Function1<A, B> ab,
            Function1<B, C> bc,
            Function1<C, D> cd,
            Function1<D, E> de,
            Function1<E, F> ef,
            Function1<F, G> fg,
            Function1<G, H> gh,
            Function1<H, I> hi,
            Function1<I, J> ij
    ) {
        return ij.apply(hi.apply(gh.apply(fg.apply(ef.apply(de.apply(cd.apply(bc.apply(ab.apply(a)))))))));
    }


    public static <A, B, C, D, E, F, G, H, I, J, K>
    K doSeq(
            A a,
            Function1<A, B> ab,
            Function1<B, C> bc,
            Function1<C, D> cd,
            Function1<D, E> de,
            Function1<E, F> ef,
            Function1<F, G> fg,
            Function1<G, H> gh,
            Function1<H, I> hi,
            Function1<I, J> ij,
            Function1<J, K> jk
    ) {
        return jk.apply(ij.apply(hi.apply(gh.apply(fg.apply(ef.apply(de.apply(cd.apply(bc.apply(ab.apply(a))))))))));
    }
}
