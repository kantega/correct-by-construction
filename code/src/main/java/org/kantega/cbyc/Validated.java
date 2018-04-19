package org.kantega.cbyc;

import fj.*;
import fj.data.NonEmptyList;
import fj.data.Option;

import java.util.Optional;

/**
 * A Validated represents a value that has been validated. It can have one of two states. Either it is a Fail or it is a Valid.
 * The Fail state contains a nonempty list of messages. The Valid state contains the validated value. To use the Validated object
 * one can use several methods to introspect its state.
 * fold() will give access to both states, calling the correct provided function depending on its state (like a visitor).
 * map() will transform the contents of the Validated iff it is Valid.
 * flatMap() lets you validate a value that depends on another Validated
 * apply() is like map(), but "inside" a Validated.
 * @param <A> the type of the value that is validated
 */
public interface Validated<A> {

    /**
     * Lets the user introspect the state of the validated object.
     * @param onFail The function that is called if this is a Fail
     * @param onSuccess The function that is called if this i a Valid
     * @param <T> The returned type
     * @return The result of the corresponding function that has been applied
     */
    <T> T fold(F<NonEmptyList<String>, T> onFail, F<A, T> onSuccess);

    /**
     * If the Validated is Valid, then this method return a new Validated with the function applied to its contents. If the Validated is
     * Failed, then it has no effect.
     * @param f The transformation function
     * @param <B> the type of the value that the function returns
     * @return a new Validated that contains the transformed value, or the original failure.
     */
    <B> Validated<B> map(F<A, B> f);

    /**
     * Applies the given function to the value if this is a Valid and returns the result. If this is a Fail, the failure message is
     * returned.
     * @param f the function that applies a new validation
     * @param <B> the type of the value the next validation validates
     * @return either a new validation based on this one, or this.
     */
    <B> Validated<B> flatMap(F<A, Validated<B>> f);

    /**
     * Applies the function of the supplied validation if both are Valid. Accumulates the failure messages if either or both are
     * a Fail.
     * @param vf The Validated function to apply
     * @param <B> the type of the return value of the function.
     * @return a new Validated.
     */
    <B> Validated<B> apply(Validated<F<A, B>> vf);

    /**
     * Creates a Validated that is Valid and contains the value.
     * @param value The value the Valid contains
     * @param <A> The type of the validated value
     * @return A Validated that is Valid
     */
    static <A> Validated<A> valid(A value) {
        return new Valid<>(value);
    }

    /**
     * Creates a Validated that is Fail and contains the message.
     * @param msg The fail message
     * @param <A> the type of the validated value
     * @return a Validated that is in the Fail state
     */
    static <A> Validated<A> fail(String msg) {
        return new Fail<>(NonEmptyList.nel(msg));
    }


    /**
     * Turns an Optional into a Validated with the supplied message if the Optional is empty
     * @param optional The optional to check
     * @param msg the message if the optional is empty
     * @param <A> the type of the validated object
     * @return a new Validated
     */
    static <A> Validated<A> of(Optional<A> optional, String msg){
        return optional.map(Validated::valid).orElseGet(()->fail(msg));
    }

    /**
     * Turns an fj.data.Option into a Validated with the supplied message if the Optional is empty
     * @param optional The optional to check
     * @param msg the message if the optional is empty
     * @param <A> the type of the validated object
     * @return a new Validated
     */
    static <A> Validated<A> of(Option<A> optional, String msg){
        return optional.map(Validated::valid).orSome(()->fail(msg));
    }

    /**
     * Accumulates the values of two Validated values. If both are Valid, the values are applied to the provided function, returning
     * a Valid with the result of the application.
     * If either Validated is Fail, the Fail is returned. If both are Fail, their messages are append into a new Fail.
     * @param va A valdiated value a
     * @param vb A validated value b
     * @param f the function that joins the values
     * @param <A> the type of a
     * @param <B> the type of b
     * @param <T> the return type of the pfrovided function
     * @return a new Validated.
     */
    static <A, B, T> Validated<T> accum(Validated<A> va, Validated<B> vb, F<A, F<B, T>> f) {
        return vb.apply(va.map(f));
    }

    static <A, B, T> Validated<T> accum(Validated<A> va, Validated<B> vb, F2<A, B, T> f) {
        return accum(va,vb,a->b->f.f(a,b));
    }

    static <A, B, C, T> Validated<T> accum(Validated<A> va, Validated<B> vb, Validated<C> vc, F<A, F<B, F<C, T>>> f) {
        return vc.apply(vb.apply(va.map(f)));
    }

    static <A, B, C, T> Validated<T> accum(Validated<A> va, Validated<B> vb, Validated<C> vc, F3<A, B, C, T> f) {
        return accum(va, vb, vc, Function.curry(f));
    }

    static <A, B, C, D, T> Validated<T> accum(Validated<A> va, Validated<B> vb, Validated<C> vc, Validated<D> vd, F<A, F<B, F<C, F<D, T>>>> f) {
        return vd.apply(vc.apply(vb.apply(va.map(f))));
    }

    static <A, B, C, E, T> Validated<T> accum(Validated<A> va, Validated<B> vb, Validated<C> vc, Validated<E> ve, F4<A, B, C, E, T> f) {
        return accum(va, vb, vc, ve, Function.curry(f));
    }

    static <A, B, C, D, E, T> Validated<T> accum(Validated<A> va, Validated<B> vb, Validated<C> vc, Validated<D> vd, Validated<E> ve, F5<A, B, C, D, E, T> f) {
        return accum(va, vb, vc, vd, ve, Function.curry(f));
    }

    static <A, B, C, D, E, T> Validated<T> accum(Validated<A> va, Validated<B> vb, Validated<C> vc, Validated<D> vd, Validated<E> ve, F<A, F<B, F<C, F<D, F<E, T>>>>> f) {
        return ve.apply(vd.apply(vc.apply(vb.apply(va.map(f)))));
    }


    /**
     * The class that represents the Valid state of a Validated.
     * @param <A>
     */
    class Valid<A> implements Validated<A> {

        final A value;

        private Valid(A value) {
            this.value = value;
        }

        public <T> T fold(F<NonEmptyList<String>, T> onFail, F<A, T> onSuccess) {
            return onSuccess.f(value);
        }

        @Override
        public <B> Validated<B> map(F<A, B> f) {
            return valid(f.f(value));
        }

        @Override
        public <B> Validated<B> flatMap(F<A, Validated<B>> f) {
            return f.f(value);
        }


        @Override
        public <B> Validated<B> apply(Validated<F<A, B>> vf) {
            return vf.fold(
              Fail::new,
              f -> valid(f.f(value))
            );
        }

        @Override
        public String toString() {
            return "Valid{" +
              "value=" + value +
              '}';
        }
    }

    /**
     * The class the represents the Fail state of a validated.
     * @param <A>
     */
    class Fail<A> implements Validated<A> {

        final NonEmptyList<String> msgs;

        private Fail(NonEmptyList<String> msgs) {
            this.msgs = msgs;
        }


        public <T> T fold(F<NonEmptyList<String>, T> onFail, F<A, T> onSuccess) {
            return onFail.f(msgs);
        }

        @Override
        public <B> Validated<B> map(F<A, B> f) {
            return new Fail<>(msgs);
        }

        @Override
        public <B> Validated<B> flatMap(F<A, Validated<B>> f) {
            return new Fail<>(msgs);
        }

        @Override
        public <B> Validated<B> apply(Validated<F<A, B>> vf) {
            return vf.fold(
              otherMsgs -> new Fail<>(msgs.append(otherMsgs)),
              s -> new Fail<>(msgs)
            );
        }

        @Override
        public String toString() {
            return "Fail{" +
              "msgs=" + msgs +
              '}';
        }
    }

}
