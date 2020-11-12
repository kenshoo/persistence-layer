package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.Triptional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.Supplier;

import static com.kenshoo.pl.entity.internal.Triptionals.firstPresent;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TriptionalsTest {

    @Spy
    private StubTriptionalSupplier stubTriptionalSupplier;

    @Test
    public void firstPresent_WhenOneSupplierOfNotNull_ShouldReturnIt() {
        assertThat(firstPresent(() -> Triptional.of(2)), is(Triptional.of(2)));
    }

    @Test
    public void firstPresent_WhenOneSupplierOfNull_ShouldReturnNull() {
        assertThat(firstPresent(Triptional::nullInstance), is(Triptional.nullInstance()));
    }

    @Test
    public void firstPresent_WhenOneSupplierOfAbsent_ShouldReturnAbsent() {
        assertThat(firstPresent(Triptional::absent), is(Triptional.absent()));
    }

    @Test
    public void firstPresent_WhenTwoSuppliersOfNotNull_ShouldReturnFirstValue() {
        assertThat(firstPresent(() -> Triptional.of(2),
                               () -> Triptional.of(3)),
                   is(Triptional.of(2)));
    }

    @Test
    public void firstPresent_WhenTwoSuppliersOfNotNull_ShouldNotCalculateSecondValue() {
        firstPresent(() -> Triptional.of(2),
                    stubTriptionalSupplier);

        verify(stubTriptionalSupplier, never()).get();
    }

    @Test
    public void firstPresent_WhenFirstSupplierOfNotNull_SecondOfNull_ShouldReturnFirstValue() {
        assertThat(firstPresent(() -> Triptional.of(2),
                               Triptional::nullInstance),
                   is(Triptional.of(2)));
    }

    @Test
    public void firstPresent_WhenFirstSupplierOfNotNull_SecondOfAbsent_ShouldReturnFirstValue() {
        assertThat(firstPresent(() -> Triptional.of(2),
                               Triptional::absent),
                   is(Triptional.of(2)));
    }

    @Test
    public void firstPresent_WhenFirstSupplierOfNull_SecondOfNotNull_ShouldReturnNull() {
        assertThat(firstPresent(Triptional::nullInstance,
                               () -> Triptional.of(2)),
                   is(Triptional.nullInstance()));
    }

    @Test
    public void firstPresent_WhenTwoSuppliersOfNull_ShouldReturnNull() {
        assertThat(firstPresent(Triptional::nullInstance,
                               Triptional::nullInstance),
                   is(Triptional.nullInstance()));
    }

    @Test
    public void firstPresent_WhenFirstSupplierOfNull_SecondOfAbsent_ShouldReturnNull() {
        assertThat(firstPresent(Triptional::nullInstance,
                               Triptional::absent),
                   is(Triptional.nullInstance()));
    }

    @Test
    public void firstPresent_WhenFirstSupplierOfAbsent_SecondOfNotNull_ShouldReturnSecondValue() {
        assertThat(firstPresent(Triptional::absent,
                               () -> Triptional.of(2)),
                   is(Triptional.of(2)));
    }

    @Test
    public void firstPresent_WhenFirstSupplierOfAbsent_SecondOfNull_ShouldReturnNull() {
        assertThat(firstPresent(Triptional::absent,
                               Triptional::nullInstance),
                   is(Triptional.nullInstance()));
    }

    @Test
    public void firstPresent_WhenTwoSuppliersOfAbsent_ShouldReturnAbsent() {
        assertThat(firstPresent(Triptional::absent,
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