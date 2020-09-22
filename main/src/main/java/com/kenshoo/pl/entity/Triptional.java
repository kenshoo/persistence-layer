package com.kenshoo.pl.entity;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.*;

import static com.kenshoo.pl.entity.Triptional.State.*;
import static java.util.Objects.requireNonNull;

/**
 * A container object which has three possible distinct states:
 * <ul>
 * <li>Present with a non-{@code null} value</li>
 * <li>Present with a {@code null} value</li>
 * <li>Absent</li>
 * </ul>
 *
 * This three-state object can be understood as an extension or generalization of {@code Optional},
 * which allows one to distinguish between a "present and {@code null}" value and an "absent" value.
 *
 * <p>Similar to {@code Optional}, this is a <a href="../lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * ({@code ==}), identity hash code, or synchronization) on instances of
 * {@code Triptional} may have unpredictable results and should be avoided.
 *
 * The motivation for creating this type is as follows:
 * <p>
 * In the persistence-layer we manage objects representing DB values, and since the DB supports {@code NULL} as a valid field value,
 * this resulted in two types that require a three-state representation of fields:
 * <ol>
 * <li>In {@link Entity} which holds fields fetched from the DB, a field can be either:
 * <ul>
 *   <li>Fetched with a non-{@code null} value</li>
 *   <li>Fetched with a {@code null} value</li>
 *   <li>Not fetched</li>
 * </ul>
 * </li>
 * <li>In {@link EntityChange} (command) a field can be either:
 * <ul>
 * <li>Changed by the command to a non-{@code null} value</li>
 * <li>Changed by the command to a {@code null} value</li>
 * <li>Not in the command (unchanged)</li>
 * </ul>
 * </ol>
 *
 * @param <T> the type of value in the {@code Triptional}
 */
public class Triptional<T> {

    enum State {
        NOT_NULL,
        NULL,
        ABSENT
    }

    private static final Triptional<?> NULL_INSTANCE = new Triptional<>(NULL);
    private static final Triptional<?> ABSENT_INSTANCE = new Triptional<>(ABSENT);

    private final T value;
    private final State state;

    private Triptional(final State state) {
        this(null, state);
    }

    private Triptional(final T value, final State state) {
        this.value = value;
        this.state = state;
    }

    /**
     * Returns a {@code Triptional} with the specified present value, can be {@code null}.
     *
     * @param <T> the class of the value
     * @param value the value to be present, can be {@code null}
     * @return a {@code Triptional} with the value present
     */
    public static <T> Triptional<T> of(final T value) {
        return value == null ? nullInstance() : new Triptional<>(value, NOT_NULL);
    }

    /**
     * Returns an {@code null} {@code Triptional} instance - meaning that it is present but with a {@code null} value.<br>
     *
     * <i>Note</i> Though it may be tempting to do so, avoid testing if an object
     * is {@code null} by comparing with {@code ==} against instances returned by
     * {@code Triptional.nullInstance()}. There is no guarantee that it is a singleton.
     * Instead, use {@link #isNull()}.
     *
     * @param <T> Type of the {@code null} value
     * @return a {@code null} {@code Triptional}
     */
    @SuppressWarnings("unchecked")
    public static <T> Triptional<T> nullInstance() {
        return (Triptional<T>) NULL_INSTANCE;
    }

    /**
     * Returns an absent {@code Triptional} instance.  No value is present for this
     * Triptional.
     *
     * <i>Note</i> Though it may be tempting to do so, avoid testing if an object
     * is empty by comparing with {@code ==} against instances returned by
     * {@code Triptional.absent()}. There is no guarantee that it is a singleton.
     * Instead, use either {@link #isPresent()} or {@link #isAbsent()}.
     *
     * @param <T> Type of the absent value
     * @return an absent {@code Triptional}
     */
    @SuppressWarnings("unchecked")
    public static <T> Triptional<T> absent() {
        return (Triptional<T>) ABSENT_INSTANCE;
    }

    /**
     * If a value is present in this {@code Triptional} (including possibly {@code null}), returns the value,
     * otherwise throws {@code NoSuchElementException}.
     *
     * @return the value held by this {@code Triptional}, may be {@code null}
     * @throws NoSuchElementException if there is no value present
     *
     * @see Triptional#isPresent()
     */
    public T get() {
        if (isAbsent()) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * If a value is present and not-{@code null}, invoke the specified consumer with the value,
     * otherwise do nothing.
     *
     * @param consumer block to be executed if a value is present and not-{@code null}
     */
    public void ifNotNull(final Consumer<? super T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }

    /**
     * If a value is present and not-{@code null}, apply the <i>mapper()</i> function to it,
     * and return a {@code Triptional} describing the result.<br>
     * If a value is present and {@code null}, return a {@code null} Triptional instance.<br>
     * Otherwise - return an absent {@code Triptional}.<br>
     * This method is similar to {@link Optional#map} except that it will preserve a present {@code null} value as well
     * (will not turn it into an absent Triptional}
     *
     * @param <U> The type of the result of the mapping function
     * @param mapper a mapping function to apply to the value, if present and not-{@code null}
     * @return a {@code Triptional} describing the result of applying the <i>mapper</i>, if it is present and not-{@code null};
     * or a null {@code Triptional}, if it is present and {@code null};
     * otherwise - an absent {@code Triptional}
     * @throws NullPointerException if the <i>mapper</i> function is {@code null}
     */
    public <U> Triptional<U> map(final Function<? super T, ? extends U> mapper) {
        return map(mapper, () -> null);
    }

    /**
     * If a value is present and not-{@code null}, apply the <i>notNullMapper()</i> function to it,
     * and return a {@code Triptional} describing the result.<br>
     * If a value is present and {@code null}, call the <i>nullReplacer()</i> function,
     * and return a {@code Triptional} holding the result.<br>
     * Otherwise - return an absent {@code Triptional}.
     *
     * <i>Note</i> This method is similar to {@link Optional#map} except that it also allows for a separately-defined replacement operation
     * in case of a present {@code null} value. This is convenient in case some default value is required to replace the {@code null}.<br>
     * For example - the following code will convert numbers into their string representations, unless the number is {@code null},
     * in which case it will be replaced with an empty string:
     *
     * <pre>{@code
     *     final Integer number = readNumber();
     *     final Triptional<String> triptional = Triptional.of(number).map(String::valueOf, () -> "");
     * }</pre>
     *
     * @param <U> The type of the result of the mapping function
     * @param notNullMapper a mapping function to apply to the value, if present and not-{@code null}
     * @param nullReplacer a function to calculate a replacement value, if present and {@code null}
     * @return a {@code Triptional} describing the result of applying the <i>notNullMapper</i>
     * function to the value of this {@code Triptional}, if it is present and not-{@code null};
     * or a {@code Triptional} holding the result of the <i>nullReplacer</i> function, if present and {@code null};
     * otherwise - an absent {@code Triptional}
     * @throws NullPointerException if the <i>notNullMapper</i> function is {@code null} or the <i>nullReplacer </i> function is {@code null}
     */
    public <U> Triptional<U> map(final Function<? super T, ? extends U> notNullMapper,
                                 final Supplier<? extends U> nullReplacer) {
        requireNonNull(notNullMapper, "notNullMapper is required");
        requireNonNull(nullReplacer, "nullReplacer is required");

        switch (state) {
            case NOT_NULL:
                return of(notNullMapper.apply(value));
            case NULL:
                return of(nullReplacer.get());
            default:
                return absent();
        }
    }

    /**
     * If a value is present and not-{@code null}, apply the Triptional-bearing <i>notNullMapper()</i> function to it,
     * and return that result.<br>
     * If a value is present and {@code null}, return a {@code null} {@code Triptional} .<br>
     * Otherwise - return an absent {@code Triptional}.<br>
     *
     * This method is similar to {@link Optional#flatMap} except that it will preserve a present {@code null} value as well
     * (will not turn it into an absent {@code Triptional}}
     *
     * @param <U> The type of the result of the mapping function
     * @param mapper a Triptional-bearing mapping function to apply to the value, if present and value not-{@code null}
     * @return the result of applying the <i>notNullMapper</i> function, if present and value not-{@code null};
     * or a {@code null} {@code Triptional}, if present and value is {@code null};
     * otherwise - an absent {@code Triptional}
     * @throws NullPointerException if the <i>mapper</i> function is {@code null}, or if it returns a {@code null} result
     */
    public <U> Triptional<U> flatMap(final Function<? super T, Triptional<U>> mapper) {
        return flatMap(mapper, Triptional::nullInstance);
    }

    /**
     * If a value is present and not-{@code null}, apply the Triptional-bearing <i>notNullMapper()</i> function to it,
     * and return that result.<br>
     * If a value is present and {@code null}, call the <i>nullReplacer()</i> Triptional-bearing function,
     * and return that result.<br>
     * Otherwise - return an absent {@code Triptional}.
     *
     * <i>Note</i> This method is similar to {@link Optional#flatMap} except that it also allows for a separately-defined replacement
     * for a {@code null} value. This is convenient in case some operation requires a default value to replace the {@code null}.<br>
     * For example - the following code converts a nested Triptional holding a number into a single one, unless the inner object is a {@code null} Triptional,
     * in which case it will be replaced by a zero:
     *
     * <pre>{@code
     *     final Triptional<Triptional<Integer>> nested = readNestedTriptional();
     *     final Triptional<Integer> flattened = nested.flatMap(Function.identity(), () -> 0);
     * }</pre>
     *
     * @param <U> The type of the result of the mapping function
     * @param notNullMapper a Triptional-bearing mapping function to apply to the value, if present and value not-{@code null}
     * @param nullReplacer a Triptional-bearing function to calculate a replacement instance, if present and value is {@code null}
     * @return the result of applying the <i>notNullMapper</i> function, if present and value not-{@code null};
     * or the result of calling the <i>nullReplacer</i> function, if present and value is {@code null};
     * otherwise - an absent {@code Triptional}
     * @throws NullPointerException if either of the input functions are {@code null}, or if either one of them returns a {@code null} result
     */
    public <U> Triptional<U> flatMap(final Function<? super T, Triptional<U>> notNullMapper,
                                     final Supplier<Triptional<U>> nullReplacer) {
        requireNonNull(notNullMapper, "notNullMapper is required");
        requireNonNull(nullReplacer, "nullReplacer is required");

        switch (state) {
            case NOT_NULL:
                return requireNonNull(notNullMapper.apply(value));
            case NULL:
                return requireNonNull(nullReplacer.get());
            default:
                return absent();
        }
    }

    /**
     * Returns an {code Optional} with the value, if present and not-{@code null}; otherwise (whether present with a {@code null} value or absent) -
     * return an empty {@code Optional}.<br>
     * This method is in effect a "reduction" operation where two distinct states of {@code Triptional}, {@code null} and absent, are mapped to the same empty {@code Optional}.
     *
     * @return an {code Optional} with a value, if present and not-{@code null}, otherwise - an empty {@code Optional}
     */
    public Optional<T> asOptional() {
        return Optional.ofNullable(value);
    }

    /**
     * If a value is present and not-{@code null}, apply the <i>notNullMapper()</i> function to it,
     * and return a {@code Optional} holding the result, or an empty {@code Optional} if the result is {@code null}.<br>
     * Otherwise - return an empty {@code Optional}.
     *
     * @param <U> The type of the result of the mapping function
     * @param mapper a mapping function to apply to the value, if present and not-{@code null}
     * @return an {@code Optional} holding the result of applying the <i>mapper</i>
     * function to the value of this {@code Triptional}, if it is present and not-{@code null};
     * otherwise - an empty {@code Optional}
     * @throws NullPointerException if the <i>mapper</i> function is {@code null}
     */
    public <U> Optional<U> mapToOptional(final Function<? super T, ? extends U> mapper) {
        return mapToOptional(mapper, () -> null);
    }

    /**
     * If a value is present and not-{@code null}, apply the <i>notNullMapper()</i> function to it,
     * and return a {@code Optional} holding the result, or an empty {@code Optional} if the result is {@code null}.<br>
     * If a value is present and {@code null}, call the <i>nullReplacer()</i> function,
     * and return an {@code Optional} holding the result, or an empty {@code Optional} if the result is {@code null}.<br>
     * Otherwise - return an empty {@code Optional}.
     *
     * @param <U> The type of the result of the mapping function
     * @param notNullMapper a mapping function to apply to the value, if present and not-{@code null}
     * @param nullReplacer a function to calculate a replacement value, if present and {@code null}
     * @return an {@code Optional} holding the result of applying the <i>notNullMapper</i>
     * function to the value of this {@code Triptional}, if it is present and not-{@code null};
     * or an {@code Optional} holding the result of the <i>nullReplacer</i> function, if present and {@code null};
     * otherwise - an empty {@code Optional}
     * @throws NullPointerException if the <i>notNullMapper</i> function is {@code null} or the <i>nullReplacer </i> function is {@code null}
     */
    public <U> Optional<U> mapToOptional(final Function<? super T, ? extends U> notNullMapper,
                                         final Supplier<? extends U> nullReplacer) {
        return map(notNullMapper, nullReplacer).asOptional();
    }

    /**
     * If a value is present, and the value matches the given predicate,
     * returns an {@code Triptional} describing the value, otherwise returns an
     * absent {@code Triptional}.
     *
     * @param predicate the predicate to apply to a value, if present
     * @return an {@code Triptional} describing the value of this
     *         {@code Triptional}, if a value is present and the value matches the
     *         given predicate; otherwise an absent {@code Triptional}
     * @throws NullPointerException if the predicate is {@code null}
     */
    public Triptional<T> filter(final Predicate<? super T> predicate) {
        requireNonNull(predicate, "a predicate must be provided");
        if (matches(predicate)) {
            return this;
        }
        return absent();
    }

    /**
     * Return {@code true} if there is a value is present, and it matches the given predicate;
     * otherwise return {@code false}
     *
     * @param predicate the predicate to apply to a value, if present
     * @return {@code true} if there is a value is present, and it matches the given predicate;
     *         otherwise {@code false}
     * @throws NullPointerException if the predicate is {@code null}
     */
    public boolean matches(final Predicate<? super T> predicate) {
        requireNonNull(predicate, "a predicate must be provided");
        return isPresent() && predicate.test(value);
    }

    /**
     * Return {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    public boolean isPresent() {
        return !isAbsent();
    }

    /**
     * Return {@code true} if there is no value present, otherwise {@code false}.
     *
     * @return {@code true} if there is no value present, otherwise {@code false}.
     */
    public boolean isAbsent() {
        return state == ABSENT;
    }

    /**
     * Return {@code true} if the value is present and not {@code null}, otherwise {@code false}.
     *
     * @return {@code true} if the value is present and not {@code null}, otherwise {@code false}.
     */
    public boolean isNotNull() {
        return state == NOT_NULL;
    }

    /**
     * Return {@code true} if the value is present and {@code null} or no value, otherwise {@code false}.
     *
     * @return {@code true} if the value is present and {@code null} or no value, otherwise {@code false}.
     */
    public boolean isNullOrAbsent() {
        return isAbsent() || isNull();
    }

    /**
     * Return {@code true} if the value is present and {@code null}, otherwise {@code false}.
     *
     * @return {@code true} if the value is present and {@code null}, otherwise {@code false}.
     */
    public boolean isNull() {
        return state == NULL;
    }

    /**
     * Return {@code true} if there is a value present (possibly {@code null}), and it equals the input;
     * otherwise {@code false}
     *
     * @param value a value to compare to this value
     * @return {@code true} if there is a value present, and it equals the input; otherwise {@code false}
     */
    public boolean equalsValue(final T value) {
        return isPresent() && Objects.equals(this.value, value);
    }

    /**
     * Indicates whether some other object is "equal to" this {@code Triptional}. The
     * other object is considered equal if:
     * <ul>
     * <li>it is also a {@code Triptional} and;</li>
     * <li>both instances have no value present or;</li>
     * <li>both instances have a present and {@code null} value or;</li>
     * <li>the present values are "equal to" each other via {@code equals()}.</li>
     * </ul>
     *
     * @param obj an object to be tested for equality
     * @return {@code true} if the other object is "equal to" this object
     * otherwise {@code false}
     */
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(final Object obj) {
        return equals(obj, Objects::equals);
    }

    /**
     * Indicates whether some other object is "equal to" this {@code Triptional} using the input function to test equality.<br>
     * The other object is considered equal if:
     * <ul>
     * <li>it is also a {@code Triptional} and;
     * <li>both instances have no value present or;
     * <li>both instances have a value present (possibly {@code null}),
     * and when the <i>valueEqualityFunction</i> is applied to both values it returns {@code true}
     * </ul>
     *
     * @param obj an object to be tested for equality
     * @param valueEqualityFunction the function to use for testing equality of values, when present
     * @return {@code true} if the other object is "equal to" this object
     * otherwise {@code false}
     * @throws NullPointerException if <i>valueEqualityFunction</i> is {@code null}
     */
    public boolean equals(final Object obj, final BiFunction<T, T, Boolean> valueEqualityFunction) {
        requireNonNull(valueEqualityFunction, "A value equality function must be provided");

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Triptional)) {
            return false;
        }

        //noinspection unchecked
        final Triptional<T> other = (Triptional<T>) obj;

        if (!(isPresent() && other.isPresent())) {
            return false;
        }

        return valueEqualityFunction.apply(value, other.value);
    }

    /**
     * Returns a hash code value which is the combination of:
     * <ul>
     *     <li>The present value, if any, or 0 (zero) if no value is present.</li>
     *     <li>An internal indicator to differentiate between a present and an absent value
     *     (without this, a present value of {@code null} and an absent value would produce the same hashcode)</li>
     * </ul>
     *
     * @return hash code value of this instance
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(state)
            .append(value)
            .toHashCode();
    }

    /**
     * Returns a non-empty string representation of this {@code Triptional} suitable for
     * debugging. The exact presentation format is unspecified and may vary
     * between implementations and versions.<br>
     *
     * <i>Implementation Note:</i> If a value is present the result must include its string
     * representation in the result. Absent, {@code null} and not-{@code null} Triptionals must all be
     * unambiguously differentiable.
     *
     * @return the string representation of this instance
     */
    @Override
    public String toString() {
        switch (state) {
            case NOT_NULL:
                return String.format("Triptional[%s]", value);
            case NULL:
                return "Triptional.null";
            default:
                return "Triptional.absent";
        }
    }
}
