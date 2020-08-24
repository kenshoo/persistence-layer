package com.kenshoo.pl.entity;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;

public class TriptionalTest {

    @Test
    public void of_NotNull_ShouldBePresent() {
        assertThat(Triptional.of(3).isPresent(), is(true));
    }

    @Test
    public void of_NotNull_ShouldNotBeAbsent() {
        assertThat(Triptional.of(3).isAbsent(), is(false));
    }

    @Test
    public void of_NotNull_ShouldBeNotNull() {
        assertThat(Triptional.of(3).isNotNull(), is(true));
    }

    @Test
    public void of_NotNull_ShouldNotBeNull() {
        assertThat(Triptional.of(3).isNull(), is(false));
    }

    @Test
    public void of_NotNull_ShouldGetValue() {
        assertThat(Triptional.of(3).get(), is(3));
    }

    @Test
    public void of_Null_ShouldBePresent() {
        assertThat(Triptional.of(null).isPresent(), is(true));
    }

    @Test
    public void of_Null_ShouldNotBeAbsent() {
        assertThat(Triptional.of(null).isAbsent(), is(false));
    }

    @Test
    public void of_Null_ShouldNotBeNotNull() {
        assertThat(Triptional.of(null).isNotNull(), is(false));
    }

    @Test
    public void of_Null_ShouldBeNull() {
        assertThat(Triptional.of(null).isNull(), is(true));
    }

    @Test
    public void of_Null_ShouldGetValue() {
        assertThat(Triptional.of(null).get(), nullValue());
    }

    @Test
    public void nullInstance_ShouldBePresent() {
        assertThat(Triptional.nullInstance().isPresent(), is(true));
    }

    @Test
    public void nullInstance_ShouldNotBeAbsent() {
        assertThat(Triptional.nullInstance().isAbsent(), is(false));
    }

    @Test
    public void nullInstance_ShouldNotBeNotNull() {
        assertThat(Triptional.nullInstance().isNotNull(), is(false));
    }

    @Test
    public void nullInstance_ShouldBeNull() {
        assertThat(Triptional.nullInstance().isNull(), is(true));
    }

    @Test
    public void nullInstance_ShouldGetValue() {
        assertThat(Triptional.of(null).get(), nullValue());
    }

    @Test
    public void absent_ShouldNotBePresent() {
        assertThat(Triptional.absent().isPresent(), is(false));
    }

    @Test
    public void absent_ShouldBeAbsent() {
        assertThat(Triptional.absent().isAbsent(), is(true));
    }

    @Test
    public void absent_ShouldNotBeNotNull() {
        assertThat(Triptional.absent().isNotNull(), is(false));
    }

    @Test
    public void absent_ShouldNotBeNull() {
        assertThat(Triptional.absent().isNull(), is(false));
    }

    @Test(expected = NoSuchElementException.class)
    public void absent_ShouldThrowExceptionOnGet() {
        Triptional.absent().get();
    }

    @Test
    public void ifNotNull_WhenNotNull_ShouldCallConsumer() {
        final List<Integer> outputList = new ArrayList<>();
        Triptional.of(3).ifNotNull(outputList::add);
        assertThat(outputList, is(singletonList(3)));
    }

    @Test
    public void ifNotNull_WhenNull_ShouldNotCallConsumer() {
        final List<Object> outputList = new ArrayList<>();
        Triptional.nullInstance().ifNotNull(outputList::add);
        assertThat(outputList, is(empty()));
    }

    @Test
    public void ifNotNull_WhenAbsent_ShouldNotCallConsumer() {
        final List<Object> outputList = new ArrayList<>();
        Triptional.absent().ifNotNull(outputList::add);
        assertThat(outputList, is(empty()));
    }

    @Test
    public void mapOneArg_WhenNotNull_ShouldReturnInstanceWithMappedValue() {
        final Triptional<String> mappedObj = Triptional.of(2).map(String::valueOf);
        assertThat(mappedObj.get(), is("2"));
    }

    @Test
    public void mapOneArg_WhenNull_ShouldReturnNullInstance() {
        final Triptional<String> mappedObj = Triptional.nullInstance().map(String::valueOf);
        assertThat(mappedObj.isNull(), is(true));
    }

    @Test
    public void mapOneArg_WhenAbsent_ShouldReturnAbsentInstance() {
        final Triptional<String> mappedObj = Triptional.absent().map(String::valueOf);
        assertThat(mappedObj.isAbsent(), is(true));
    }

    @Test
    public void mapTwoArgs_WhenNotNull_ShouldReturnInstanceWithValueFromMapper() {
        final Triptional<String> mappedObj = Triptional.of(2)
                                                       .map(String::valueOf, () -> "blabla");
        assertThat(mappedObj.get(), is("2"));
    }

    @Test
    public void mapTwoArgs_WhenNull_ShouldReturnInstanceWithReplacingValue() {
        final Triptional<String> mappedObj = Triptional.nullInstance()
                                                       .map(String::valueOf, () -> "blabla");
        assertThat(mappedObj.get(), is("blabla"));
    }


    @Test
    public void mapTwoArgs_WhenAbsent_ShouldReturnAbsentInstance() {
        final Triptional<String> mappedObj = Triptional.absent()
                                                       .map(String::valueOf, () -> "blabla");
        assertThat(mappedObj.isAbsent(), is(true));
    }

    @Test
    public void flatMapOneArg_WhenNotNull_AndMappedToNotNull_ShouldReturnPresentWithMappedValue() {
        final Triptional<String> mappedObj = Triptional.of(2).flatMap(this::toTriptionalString);
        assertThat(mappedObj.get(), is("2"));
    }

    @Test
    public void flatMapOneArg_WhenNotNull_AndMappedToNullInstance_ShouldReturnNullInstance() {
        final Triptional<String> mappedObj = Triptional.of(2).flatMap(any -> Triptional.nullInstance());
        assertThat(mappedObj.isNull(), is(true));
    }

    @Test
    public void flatMapOneArg_WhenNotNull_AndMappedToAbsent_ShouldReturnAbsent() {
        final Triptional<String> mappedObj = Triptional.of(2).flatMap(any -> Triptional.absent());
        assertThat(mappedObj.isAbsent(), is(true));
    }

    @Test
    public void flatMapOneArg_WhenNull_ShouldReturnNullInstance() {
        final Triptional<String> mappedObj = Triptional.nullInstance().flatMap(this::toTriptionalString);
        assertThat(mappedObj.isNull(), is(true));
    }

    @Test
    public void flatMapOneArg_WhenAbsent_ShouldReturnAbsentInstance() {
        final Triptional<String> mappedObj = Triptional.absent().flatMap(this::toTriptionalString);
        assertThat(mappedObj.isAbsent(), is(true));
    }

    @Test
    public void flatMapTwoArgs_WhenNotNull_AndMappedToNotNull_ShouldReturnPresentWithMappedValue() {
        final Triptional<String> mappedObj = Triptional.of(2).flatMap(this::toTriptionalString,
                                                                      () -> Triptional.of("blabla"));
        assertThat(mappedObj.get(), is("2"));
    }

    @Test
    public void flatMapTwoArgs_WhenNotNull_AndMappedToNullInstance_ShouldReturnNullInstance() {
        final Triptional<String> mappedObj = Triptional.of(2).flatMap(any -> Triptional.nullInstance(),
                                                                      () -> Triptional.of("blabla"));
        assertThat(mappedObj.isNull(), is(true));
    }

    @Test
    public void flatMapTwoArgs_WhenNotNull_AndMappedToAbsent_ShouldReturnAbsent() {
        final Triptional<String> mappedObj = Triptional.of(2).flatMap(any -> Triptional.absent(),
                                                                      () -> Triptional.of("blabla"));
        assertThat(mappedObj.isAbsent(), is(true));
    }

    @Test
    public void flatMapTwoArgs_WhenNull_AndReplacerReturnsNotNull_ShouldReturnReplacingInstance() {
        final Triptional<String> mappedObj = Triptional.nullInstance().flatMap(this::toTriptionalString,
                                                                               () -> Triptional.of("blabla"));
        assertThat(mappedObj.get(), is("blabla"));
    }

    @Test
    public void flatMapTwoArgs_WhenNull_AndReplacerReturnsNull_ShouldReturnNullInstance() {
        final Triptional<String> mappedObj = Triptional.nullInstance().flatMap(this::toTriptionalString,
                                                                               Triptional::nullInstance);
        assertThat(mappedObj.isNull(), is(true));
    }

    @Test
    public void flatMapTwoArgs_WhenNull_AndReplacerReturnsAbsent_ShouldReturnAbsent() {
        final Triptional<String> mappedObj = Triptional.nullInstance().flatMap(this::toTriptionalString,
                                                                               Triptional::absent);
        assertThat(mappedObj.isAbsent(), is(true));
    }

    @Test
    public void flatMapTwoArgs_WhenAbsent_ShouldReturnAbsent() {
        final Triptional<String> mappedObj = Triptional.absent().flatMap(this::toTriptionalString,
                                                                         () -> Triptional.of("blabla"));
        assertThat(mappedObj.isAbsent(), is(true));
    }

    @Test
    public void asOptional_WhenNotNull_ShouldReturnPresentWithSameValue() {
        assertThat(Triptional.of(2).asOptional(), isPresentAndIs(2));
    }

    @Test
    public void asOptional_WhenNull_ShouldReturnEmpty() {
        assertThat(Triptional.nullInstance().asOptional(), isEmpty());
    }

    @Test
    public void asOptional_WhenAbsent_ShouldReturnEmpty() {
        assertThat(Triptional.absent().asOptional(), isEmpty());
    }

    @Test
    public void mapToOptionalOneArg_WhenNotNull_ShouldReturnPresentWithMappedValue() {
        assertThat(Triptional.of(2).mapToOptional(String::valueOf), isPresentAndIs("2"));
    }

    @Test
    public void mapToOptionalOneArg_WhenNull_ShouldReturnEmpty() {
        assertThat(Triptional.nullInstance().mapToOptional(String::valueOf), isEmpty());
    }

    @Test
    public void mapToOptionalOneArg_WhenAbsent_ShouldReturnEmpty() {
        assertThat(Triptional.absent().mapToOptional(String::valueOf), isEmpty());
    }

    @Test
    public void mapToOptionalTwoArgs_WhenNotNull_ShouldReturnPresentWithMappedValue() {
        assertThat(Triptional.of(2)
                             .mapToOptional(String::valueOf, () -> "bla"),
                   isPresentAndIs("2"));
    }

    @Test
    public void mapToOptionalTwoArgs_WhenNull_ShouldReturnPresentWithReplacingValue() {
        assertThat(Triptional.nullInstance()
                             .mapToOptional(String::valueOf, () -> "bla"),
                   isPresentAndIs("bla"));
    }

    @Test
    public void mapToOptionalTwoArgs_WhenAbsent_ShouldReturnEmpty() {
        assertThat(Triptional.absent()
                             .mapToOptional(String::valueOf, () -> "bla"),
                   isEmpty());
    }

    @Test
    public void filter_WhenNotNullAndMatchesPredicate_ShouldReturnThis_Scenario1() {
        assertThat(Triptional.of(2).filter(num -> num < 3), is(Triptional.of(2)));
    }

    @Test
    public void filter_WhenNotNullAndMatchesPredicate_ShouldReturnThis_Scenario2() {
        assertThat(Triptional.of(2).filter(num -> num > 1), is(Triptional.of(2)));
    }

    @Test
    public void filter_WhenNotNullAndDoesntMatchPredicate_ShouldReturnAbsent() {
        assertThat(Triptional.of(2).filter(num -> num < 2), is(Triptional.absent()));
    }

    @Test
    public void filter_WhenNullAndMatchesPredicate_ShouldReturnThis() {
        assertThat(Triptional.nullInstance().filter(Objects::isNull), is(Triptional.nullInstance()));
    }

    @Test
    public void filter_WhenNullAndDoesntMatchPredicate_ShouldReturnAbsent() {
        assertThat(Triptional.nullInstance().filter("abc"::equals), is(Triptional.absent()));
    }

    @Test
    public void filter_WhenAbsent_ShouldReturnAbsent() {
        assertThat(Triptional.absent().filter("abc"::equals), is(Triptional.absent()));
    }

    @Test
    public void matches_WhenNotNullAndMatchesPredicate_ShouldReturnTrue_Scenario1() {
        assertThat(Triptional.of(2).matches(num -> num < 3), is(true));
    }

    @Test
    public void matches_WhenNotNullAndMatchesPredicate_ShouldReturnTrue_Scenario2() {
        assertThat(Triptional.of(2).matches(num -> num > 1), is(true));
    }

    @Test
    public void matches_WhenNotNullAndDoesntMatchPredicate_ShouldReturnFalse() {
        assertThat(Triptional.of(2).matches(num -> num < 2), is(false));
    }

    @Test
    public void matches_WhenNullAndMatchesPredicate_ShouldReturnTrue() {
        assertThat(Triptional.nullInstance().matches(Objects::isNull), is(true));
    }

    @Test
    public void matches_WhenNullAndDoesntMatchPredicate_ShouldReturnFalse() {
        assertThat(Triptional.nullInstance().matches("abc"::equals), is(false));
    }

    @Test
    public void matches_WhenAbsent_ShouldReturnFalse() {
        assertThat(Triptional.absent().matches("abc"::equals), is(false));
    }

    @Test
    public void equalsValue_WhenBothNotNullAndEqual_ShouldReturnTrue() {
        assertThat(Triptional.of(2).equalsValue(2), is(true));
    }

    @Test
    public void equalsValue_WhenBothNotNullAndUnequal_ShouldReturnFalse() {
        assertThat(Triptional.of(2).equalsValue(3), is(false));
    }

    @Test
    public void equalsValue_WhenThisNullAndOtherNotNull_ShouldReturnFalse() {
        assertThat(Triptional.nullInstance().equalsValue(3), is(false));
    }

    @Test
    public void equalsValue_WhenThisNotNullAndOtherNull_ShouldReturnFalse() {
        assertThat(Triptional.of(2).equalsValue(null), is(false));
    }

    @Test
    public void equalsValue_WhenBothNull_ShouldReturnTrue() {
        assertThat(Triptional.nullInstance().equalsValue(null), is(true));
    }

    @Test
    public void equalsValue_WhenThisAbsentAndOtherNotNull_ShouldReturnFalse() {
        assertThat(Triptional.absent().equalsValue(3), is(false));
    }

    @Test
    public void equalsValue_WhenThisAbsentAndOtherNull_ShouldReturnFalse() {
        assertThat(Triptional.absent().equalsValue(null), is(false));
    }

    @Test
    public void equalsOneArg_WhenBothNotNullWithSameValue_ShouldReturnTrue() {
        assertThat(Triptional.of(2).equals(Triptional.of(1 + 1)), is(true));
    }

    @Test
    public void equalsOneArg_WhenBothNotNullWithDifferentValues_ShouldReturnFalse() {
        assertThat(Triptional.of(2).equals(Triptional.of(3)), is(false));
    }

    @Test
    public void equalsOneArg_WhenOneNotNullAndOneNull_ShouldReturnFalse() {
        assertThat(Triptional.of(2).equals(Triptional.nullInstance()), is(false));
    }

    @Test
    public void equalsOneArg_WhenOneNotNullAndOneAbsent_ShouldReturnFalse() {
        assertThat(Triptional.of(2).equals(Triptional.absent()), is(false));
    }

    @Test
    public void equalsOneArg_WhenBothNull_ShouldReturnTrue() {
        assertThat(Triptional.nullInstance().equals(Triptional.of(null)), is(true));
    }

    @Test
    public void equalsOneArg_WhenOneNullAndOneAbsent_ShouldReturnFalse() {
        assertThat(Triptional.nullInstance().equals(Triptional.absent()), is(false));
    }

    @Test
    public void equalsTwoArgs_ForTwoDoublesCloseEnough_ShouldReturnTrue() {
        assertThat(Triptional.of(2.001).equals(Triptional.of(2.0), (x, y) -> Math.abs(x - y) < 0.01),
                   is(true));
    }

    @Test
    public void equalsTwoArgs_ForTwoDoublesNotCloseEnough_ShouldReturnFalse() {
        assertThat(Triptional.of(2.1).equals(Triptional.of(2.0), (x, y) -> Math.abs(x - y) < 0.01),
                   is(false));
    }

    @Test
    public void equalsTwoArgs_WhenOneNotNullAndOneNull_AndEqualityFunctionTrue_ShouldReturnTrue() {
        assertThat(Triptional.of(EMPTY).equals(Triptional.nullInstance(),
                                               (s1, s2) -> defaultIfEmpty(s1, "bla").equals(defaultIfEmpty(s2, "bla"))),
                   is(true));
    }

    @Test
    public void equalsTwoArgs_WhenOneNotNullAndOneNull_AndEqualityFunctionFalse_ShouldReturnFalse() {
        assertThat(Triptional.of(2).equals(Triptional.nullInstance(), Objects::equals),
                   is(false));
    }

    @Test
    public void equalsTwoArgs_WhenOneNotNullAndOneAbsent_ShouldReturnFalse() {
        assertThat(Triptional.of(2).equals(Triptional.absent(), Objects::equals),
                   is(false));
    }

    @Test
    public void equalsTwoArgs_WhenBothNull_ShouldReturnTrue() {
        assertThat(Triptional.nullInstance().equals(Triptional.of(null), Objects::equals),
                   is(true));
    }

    @Test
    public void equalsTwoArgs_WhenOneNullAndOneAbsent_ShouldReturnFalse() {
        assertThat(Triptional.nullInstance().equals(Triptional.absent(), Objects::equals),
                   is(false));
    }
    private Triptional<String> toTriptionalString(final Object val) {
        return Triptional.of(String.valueOf(val));
    }
}