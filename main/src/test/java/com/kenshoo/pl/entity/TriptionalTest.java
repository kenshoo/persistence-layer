package com.kenshoo.pl.entity;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static java.util.Collections.singletonList;
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
    public void of_NotNull_ShouldBeFilled() {
        assertThat(Triptional.of(3).isFilled(), is(true));
    }

    @Test
    public void of_NotNull_ShouldNotBeNotFilled() {
        assertThat(Triptional.of(3).isNotFilled(), is(false));
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
    public void of_Null_ShouldNotBeFilled() {
        assertThat(Triptional.of(null).isFilled(), is(false));
    }

    @Test
    public void of_Null_ShouldBeNotFilled() {
        assertThat(Triptional.of(null).isNotFilled(), is(true));
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
    public void nullInstance_ShouldNotBeFilled() {
        assertThat(Triptional.nullInstance().isFilled(), is(false));
    }

    @Test
    public void nullInstance_ShouldBeNotFilled() {
        assertThat(Triptional.nullInstance().isNotFilled(), is(true));
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
    public void absent_ShouldNotBeFilled() {
        assertThat(Triptional.absent().isFilled(), is(false));
    }

    @Test
    public void absent_ShouldBeNotFilled() {
        assertThat(Triptional.absent().isNotFilled(), is(true));
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
    public void ifFilled_WhenFilled_ShouldCallConsumer() {
        final List<Integer> outputList = new ArrayList<>();
        Triptional.of(3).ifFilled(outputList::add);
        assertThat(outputList, is(singletonList(3)));
    }

    @Test
    public void ifFilled_WhenNull_ShouldNotCallConsumer() {
        final List<Object> outputList = new ArrayList<>();
        Triptional.nullInstance().ifFilled(outputList::add);
        assertThat(outputList, is(empty()));
    }

    @Test
    public void ifFilled_WhenAbsent_ShouldNotCallConsumer() {
        final List<Object> outputList = new ArrayList<>();
        Triptional.absent().ifFilled(outputList::add);
        assertThat(outputList, is(empty()));
    }

    @Test
    public void mapOneArg_WhenFilled_ShouldReturnInstanceWithMappedValue() {
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
    public void mapTwoArgs_WhenFilled_ShouldReturnInstanceWithValueFromMapper() {
        final Triptional<String> mappedObj = Triptional.of(2)
                                                       .map(String::valueOf, () -> "blabla");
        assertThat(mappedObj.get(), is("2"));
    }

    @Test
    public void mapTwoArgs_WhenNull_ShouldReturnInstanceWithValueFromSupplier() {
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
    public void asOptional_WhenFilled_ShouldReturnPresentWithSameValue() {
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
    public void mapToOptionalOneArg_WhenFilled_ShouldReturnPresentWithMappedValue() {
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
    public void mapToOptionalTwoArgs_WhenFilled_ShouldReturnPresentWithMappedValue() {
        assertThat(Triptional.of(2)
                             .mapToOptional(String::valueOf, () -> "bla"),
                   isPresentAndIs("2"));
    }

    @Test
    public void mapToOptionalTwoArgs_WhenNull_ShouldReturnPresentWithSupplierValue() {
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
    public void equals_WhenBothFilledWithSameValue_ShouldReturnTrue() {
        assertThat(Triptional.of(2).equals(Triptional.of(1 + 1)), is(true));
    }

    @Test
    public void equals_WhenBothFilledWithDifferentValues_ShouldReturnFalse() {
        assertThat(Triptional.of(2).equals(Triptional.of(3)), is(false));
    }

    @Test
    public void equals_WhenOneFilledAndOneNull_ShouldReturnFalse() {
        assertThat(Triptional.of(2).equals(Triptional.nullInstance()), is(false));
    }

    @Test
    public void equals_WhenOneFilledAndOneAbsent_ShouldReturnFalse() {
        assertThat(Triptional.of(2).equals(Triptional.absent()), is(false));
    }

    @Test
    public void equals_WhenBothNull_ShouldReturnTrue() {
        assertThat(Triptional.nullInstance().equals(Triptional.of(null)), is(true));
    }

    @Test
    public void equals_WhenOneNullAndOneAbsent_ShouldReturnFalse() {
        assertThat(Triptional.nullInstance().equals(Triptional.absent()), is(false));
    }
}