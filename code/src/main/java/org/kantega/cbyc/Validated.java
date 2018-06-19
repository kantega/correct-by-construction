package org.kantega.cbyc;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A Validated represents a value that has been validated. It can have one of two states. Either it is a Invalid or it is a Valid.
 * The Invalid state contains a nonempty list of messages. The Valid state contains the validated value. To use the Validated object
 * one can use several methods to introspect its state.
 * <br/>
 * <code>fold()</code> will give access to both states, calling the correct provided function depending on its state (like a visitor).
 * <br/>
 * <code>map()</code> will transform the contents of the Validated iff it is Valid.
 * <br/>
 * <code>flatMap()</code> lets you validate a value that depends on another Validated
 * <br/>
 * <code>apply()</code> is like map(), but "inside" a Validated.
 *
 * @param <A> the type of the value that is validated
 */
public interface Validated<A> {

    /**
     * Lets the user introspect the state of the validated object.
     *
     * @param onInvalid    The function that is called if this is a Invalid
     * @param onSuccess The function that is called if this i a Valid
     * @param <T>       The returned type
     * @return The result of the corresponding function that has been applied
     */
    <T> T fold(Function<List<String>, T> onInvalid, Function<A, T> onSuccess);

    /**
     * If the Validated is Valid, then this method return a new Validated with the function applied to its contents. If the Validated is
     * Invalided, then it has no effect.
     *
     * @param f   The transformation function
     * @param <B> the type of the value that the function returns
     * @return a new Validated that contains the transformed value, or the original failure.
     */
    <B> Validated<B> map(Function<A, B> f);

    /**
     * Applies the given function to the value if this is a Valid and returns the result. If this is a Invalid, the failure message is
     * returned.
     *
     * @param f   the function that applies a new validation
     * @param <B> the type of the value the next validation validates
     * @return either a new validation based on this one, or this.
     */
    <B> Validated<B> flatMap(Function<A, Validated<B>> f);

    /**
     * Applies the function of the supplied validation if both are Valid. Accumulates the failure messages if either or both are
     * a Invalid.
     *
     * @param vf  The Validated function to apply
     * @param <B> the type of the return value of the function.
     * @return a new Validated.
     */
    <B> Validated<B> apply(Validated<Function<A, B>> vf);


    /**
     * If the Validated is Valid, then returns the contained value. If not it return the provided default value.
     * @param defaultValue The value to return of the Validated was a Invalid
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
     * Creates a Validated that is Invalid and contains the message.
     *
     * @param msg The reason message
     * @param <A> the type of the validated value
     * @return a Validated that is in the Invalid state
     */
    static <A> Validated<A> invalid(String msg) {
        List<String> list = new ArrayList<>();
        list.add(msg);
        return new Invalid<>(list);
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
        return optional.map(Validated::valid).orElseGet(() -> invalid(msg));
    }



    /**
     * Validates an object by applying it to the supplied predicate.
     * If the predicate holds, the object is valid.
     * If the predicate fails, a Invalid is returned with the supplied msg.
     *
     * @param value     The object to validate
     * @param predicate The predicate that must hold
     * @param msg       The message to use if the predicate does ot hold
     * @param <A>       the type of the object
     * @return a Validated
     */
    static <A> Validated<A> validate(A value, Predicate<A> predicate, String msg) {
        return predicate.test(value) ? valid(value) : invalid(msg);
    }

    /**
     * Accumulates the values of two Validated values. If both are Valid, the values are applied to the provided function, returning
     * a Valid with the result of the application.
     * If either Validated is Invalid, the Invalid is returned. If both are Invalid, their messages are append into a new Invalid.
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

    /**
     * Validates a value based on two other Validated values. If both are Valid, then the provided function is called. If not, the failed Validated is returned.
     * If both are Invalid, the failures are accumulated.
     * @param va The first Validated
     * @param vb The second Validated
     * @param f The function that first takes in the result of the first Validated, then the second value, and the validates those two vales.
     * @param <A> The type of the first validated value
     * @param <B> The type of the second validated value
     * @param <T> The type of the final validated value
     * @return a new Validation.
     */
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

        public <T> T fold(Function<List<String>, T> onInvalid, Function<A, T> onSuccess) {
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
                Invalid::new,
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
     * The class the represents the Invalid state of a validated.
     *
     * @param <A>
     */
    class Invalid<A> implements Validated<A> {

        final List<String> msgs;

        private Invalid(List<String> msgs) {
            this.msgs = msgs;
        }


        public <T> T fold(Function<List<String>, T> onInvalid, Function<A, T> onSuccess) {
            return onInvalid.apply(msgs);
        }

        @Override
        public <B> Validated<B> map(Function<A, B> f) {
            return new Invalid<>(msgs);
        }

        @Override
        public <B> Validated<B> flatMap(Function<A, Validated<B>> f) {
            return new Invalid<>(msgs);
        }

        @Override
        public <B> Validated<B> apply(Validated<Function<A, B>> vf) {
            return
              vf.fold(
                otherMsgs -> {
                    List<String> newList = new ArrayList<>(msgs);
                    msgs.addAll(otherMsgs);
                    return new Invalid<>(newList);
                },
                s -> new Invalid<>(msgs)
              );
        }

        @Override
        public String toString() {
            return "Invalid{" +
              "msgs=" + msgs +
              '}';
        }
    }

}
