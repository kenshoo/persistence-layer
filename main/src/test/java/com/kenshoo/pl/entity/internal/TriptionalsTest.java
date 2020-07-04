package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.Triptional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.Supplier;

import static com.kenshoo.pl.entity.internal.Triptionals.firstFilled;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TriptionalsTest {

    @Spy
    private StubTriptionalSupplier stubTriptionalSupplier;

    @Test
    public void firstFilled_WhenOneSupplierOfFilled_ShouldReturnIt() {
        assertThat(firstFilled(() -> Triptional.of(2)), is(Triptional.of(2)));
    }

    @Test
    public void firstFilled_WhenOneSupplierOfNull_ShouldReturnAbsent() {
        assertThat(firstFilled(Triptional::nullInstance), is(Triptional.absent()));
    }

    @Test
    public void firstFilled_WhenOneSupplierOfAbsent_ShouldReturnAbsent() {
        assertThat(firstFilled(Triptional::absent), is(Triptional.absent()));
    }

    @Test
    public void firstFilled_WhenTwoSuppliersOfFilled_ShouldReturnFirstValue() {
        assertThat(firstFilled(() -> Triptional.of(2),
                               () -> Triptional.of(3)),
                   is(Triptional.of(2)));
    }

    @Test
    public void firstFilled_WhenTwoSuppliersOfFilled_ShouldNotCalculateSecondValue() {
        firstFilled(() -> Triptional.of(2),
                    stubTriptionalSupplier);

        verify(stubTriptionalSupplier, never()).get();
    }

    @Test
    public void firstFilled_WhenFirstSupplierOfFilled_SecondOfNull_ShouldReturnFirstValue() {
        assertThat(firstFilled(() -> Triptional.of(2),
                               Triptional::nullInstance),
                   is(Triptional.of(2)));
    }

    @Test
    public void firstFilled_WhenFirstSupplierOfFilled_SecondOfAbsent_ShouldReturnFirstValue() {
        assertThat(firstFilled(() -> Triptional.of(2),
                               Triptional::absent),
                   is(Triptional.of(2)));
    }

    @Test
    public void firstFilled_WhenFirstSupplierOfNull_SecondOfFilled_ShouldReturnSecondValue() {
        assertThat(firstFilled(Triptional::absent,
                               () -> Triptional.of(2)),
                   is(Triptional.of(2)));
    }

    @Test
    public void firstFilled_WhenTwoSuppliersOfNull_ShouldReturnAbsent() {
        assertThat(firstFilled(Triptional::nullInstance,
                               Triptional::nullInstance),
                   is(Triptional.absent()));
    }

    @Test
    public void firstFilled_WhenFirstSupplierOfNull_SecondOfAbsent_ShouldReturnAbsent() {
        assertThat(firstFilled(Triptional::nullInstance,
                               Triptional::absent),
                   is(Triptional.absent()));
    }

    @Test
    public void firstFilled_WhenFirstSupplierOfAbsent_SecondOfFilled_ShouldReturnSecondValue() {
        assertThat(firstFilled(Triptional::absent,
                               () -> Triptional.of(2)),
                   is(Triptional.of(2)));
    }

    @Test
    public void firstFilled_WhenFirstSupplierOfAbsent_SecondOfNull_ShouldReturnAbsent() {
        assertThat(firstFilled(Triptional::absent,
                               Triptional::nullInstance),
                   is(Triptional.absent()));
    }

    @Test
    public void firstFilled_WhenTwoSuppliersOfAbsent_ShouldReturnAbsent() {
        assertThat(firstFilled(Triptional::absent,
                               Triptional::absent),
                   is(Triptional.absent()));
    }

    private static class StubTriptionalSupplier implements Supplier<Triptional<Integer>> {

        @Override
        public Triptional<Integer> get() {
            return Triptional.of(9999);
        }
    }
}