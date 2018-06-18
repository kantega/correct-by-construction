package org.kantega.cbyc;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A Validated represents a value that has been validated. It can have one of two states. Either it is a Fail or it is a Valid.
 * The Fail state contains a nonempty list of messages. The Valid state contains the validated value. To use the Validated object
 * one can use several methods to introspect its state.
 * fold() will give access to both states, calling the correct provided function depending on its state (like a visitor).
 * map() will transform the contents of the Validated iff it is Valid.
 * flatMap() lets you validate a value that depends on another Validated
 * apply() is like map(), but "inside" a Validated.
 *
 * @param <A> the type of the value that is validated
 */
public interface Validated<A> {

    /**
     * Lets the user introspect the state of the validated object.
     *
     * @param onFail    The function that is called if this is a Fail
     * @param onSuccess The function that is called if this i a Valid
     * @param <T>       The returned type
     * @return The result of the corresponding function that has been applied
     */
    <T> T fold(Function<List<String>, T> onFail, Function<A, T> onSuccess);

    /**
     * If the Validated is Valid, then this method return a new Validated with the function applied to its contents. If the Validated is
     * Failed, then it has no effect.
     *
     * @param f   The transformation function
     * @param <B> the type of the value that the function returns
     * @return a new Validated that contains the transformed value, or the original failure.
     */
    <B> Validated<B> map(Function<A, B> f);

    /**
     * Applies the given function to the value if this is a Valid and returns the result. If this is a Fail, the failure message is
     * returned.
     *
     * @param f   the function that applies a new validation
     * @param <B> the type of the value the next validation validates
     * @return either a new validation based on this one, or this.
     */
    <B> Validated<B> flatMap(Function<A, Validated<B>> f);

    /**
     * Applies the function of the supplied validation if both are Valid. Accumulates the failure messages if either or both are
     * a Fail.
     *
     * @param vf  The Validated function to apply
     * @param <B> the type of the return value of the function.
     * @return a new Validated.
     */
    <B> Validated<B> apply(Validated<Function<A, B>> vf);


    /**
     * If the Validated is Valid, then returns the contained value. If not it return the provided default value.
     * @param defaultValue The value to return of the Validated was a Fail
     * @return The valid value or the default value.
     */
    default A orElse(A defaultValue){
        return fold(
          t->defaultValue,
          v->v
        );
    }
    /**
     * Creates a Validated that is Valid and contains the value.
     *
     * @param value The value the Valid contains
     * @param <A>   The type of the validated value
     * @return A Validated that is Valid
     */
    static <A> Validated<A> valid(A value) {
        return new Valid<>(value);
    }

    /**
     * Creates a Validated that is Fail and contains the message.
     *
     * @param msg The fail message
     * @param <A> the type of the validated value
     * @return a Validated that is in the Fail state
     */
    static <A> Validated<A> fail(String msg) {
        List<String> list = new ArrayList<>();
        list.add(msg);
        return new Fail<>(list);
    }


    /**
     * Turns an Optional into a Validated with the supplied message if the Optional is empty
     *
     * @param optional The optional to check
     * @param msg      the message if the optional is empty
     * @param <A>      the type of the validated object
     * @return a new Validated
     */
    static <A> Validated<A> of(Optional<A> optional, String msg) {
        return optional.map(Validated::valid).orElseGet(() -> fail(msg));
    }



    /**
     * Validates an object by applying it to the supplied predicate.
     * If the predicate holds, the object is valid.
     * If the predicate fails, a Fail is returned with the supplied msg.
     *
     * @param value     The object to validate
     * @param predicate The predicate that must hold
     * @param msg       The message to use if the predicate does ot hold
     * @param <A>       the type of the object
     * @return a Validated
     */
    static <A> Validated<A> validate(A value, Predicate<A> predicate, String msg) {
        return predicate.test(value) ? valid(value) : fail(msg);
    }

    /**
     * Accumulates the values of two Validated values. If both are Valid, the values are applied to the provided function, returning
     * a Valid with the result of the application.
     * If either Validated is Fail, the Fail is returned. If both are Fail, their messages are append into a new Fail.
     *
     * @param va  A valdiated value a
     * @param vb  A validated value b
     * @param f   the function that joins the values
     * @param <A> the type of a
     * @param <B> the type of b
     * @param <T> the return type of the pfrovided function
     * @return a new Validated.
     */
    static <A, B, T> Validated<T> accum(Validated<A> va, Validated<B> vb, Function<A, Function<B, T>> f) {
        return vb.apply(va.map(f));
    }

    static <A, B, T> Validated<T> accumBind(Validated<A> va, Validated<B> vb, Function<A, Function<B, Validated<T>>> f) {
        return vb.apply(va.map(f)).flatMap(i->i);
    }

    static <A, B, T> Validated<T> accum(Validated<A> va, Validated<B> vb, BiFunction<A, B, T> f) {
        return accum(va, vb, a -> b -> f.apply(a, b));
    }

    static <A, B, C, T> Validated<T> accum(Validated<A> va, Validated<B> vb, Validated<C> vc, Function<A, Function<B, Function<C, T>>> f) {
        return vc.apply(vb.apply(va.map(f)));
    }

    static <A, B, C, D, T> Validated<T> accum(Validated<A> va, Validated<B> vb, Validated<C> vc, Validated<D> vd, Function<A, Function<B, Function<C, Function<D, T>>>> f) {
        return vd.apply(vc.apply(vb.apply(va.map(f))));
    }


    static <A, B, C, D, E, T> Validated<T> accum(Validated<A> va, Validated<B> vb, Validated<C> vc, Validated<D> vd, Validated<E> ve, Function<A, Function<B, Function<C, Function<D, Function<E, T>>>>> f) {
        return ve.apply(vd.apply(vc.apply(vb.apply(va.map(f)))));
    }


    /**
     * The class that represents the Valid state of a Validated.
     *
     * @param <A>
     */
    class Valid<A> implements Validated<A> {

        final A value;

        private Valid(A value) {
            this.value = value;
        }

        public <T> T fold(Function<List<String>, T> onFail, Function<A, T> onSuccess) {
            return onSuccess.apply(value);
        }

        @Override
        public <B> Validated<B> map(Function<A, B> f) {
            return valid(f.apply(value));
        }

        @Override
        public <B> Validated<B> flatMap(Function<A, Validated<B>> f) {
            return f.apply(value);
        }


        @Override
        public <B> Validated<B> apply(Validated<Function<A, B>> vf) {
            return
              vf.fold(
                Fail::new,
                f -> valid(f.apply(value))
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
     *
     * @param <A>
     */
    class Fail<A> implements Validated<A> {

        final List<String> msgs;

        private Fail(List<String> msgs) {
            this.msgs = msgs;
        }


        public <T> T fold(Function<List<String>, T> onFail, Function<A, T> onSuccess) {
            return onFail.apply(msgs);
        }

        @Override
        public <B> Validated<B> map(Function<A, B> f) {
            return new Fail<>(msgs);
        }

        @Override
        public <B> Validated<B> flatMap(Function<A, Validated<B>> f) {
            return new Fail<>(msgs);
        }

        @Override
        public <B> Validated<B> apply(Validated<Function<A, B>> vf) {
            return
              vf.fold(
                otherMsgs -> {
                    List<String> newList = new ArrayList<>(msgs);
                    msgs.addAll(otherMsgs);
                    return new Fail<>(newList);
                },
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
