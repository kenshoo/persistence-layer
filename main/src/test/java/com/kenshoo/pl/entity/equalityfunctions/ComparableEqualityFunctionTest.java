package com.kenshoo.pl.entity.equalityfunctions;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by yuvalr on 5/22/16.
 */
public class ComparableEqualityFunctionTest {

    @Test
    public void notEqual() throws Exception {
        ComparableEqualityFunction<BigDecimal> onTest = ComparableEqualityFunction.getInstance();
        assertFalse(onTest.apply(BigDecimal.ONE, BigDecimal.TEN));
    }

    @Test
    public void equal() throws Exception {
        ComparableEqualityFunction<BigDecimal> onTest = ComparableEqualityFunction.getInstance();
        assertTrue(onTest.apply(BigDecimal.ONE, BigDecimal.ONE));
    }

    @Test
    public void equalButNotAccordingToEquals() throws Exception {
        ComparableEqualityFunction<BigDecimal> onTest = ComparableEqualityFunction.getInstance();

        BigDecimal bigDecimal = new BigDecimal("0");
        BigDecimal bigDecimal2 = new BigDecimal("0.00");

        assertFalse(bigDecimal.equals(bigDecimal2));
        assertTrue(onTest.apply(bigDecimal, bigDecimal2));
    }

    @Test
    public void firstNull() throws Exception {
        ComparableEqualityFunction<BigDecimal> onTest = ComparableEqualityFunction.getInstance();
        assertFalse(onTest.apply(null, BigDecimal.ONE));
    }

    @Test
    public void secondNull() throws Exception {
        ComparableEqualityFunction<BigDecimal> onTest = ComparableEqualityFunction.getInstance();
        assertFalse(onTest.apply(BigDecimal.ONE, null));
    }

    @Test
    public void bothNull() throws Exception {
        ComparableEqualityFunction<BigDecimal> onTest = ComparableEqualityFunction.getInstance();
        assertTrue(onTest.apply(null, null));
    }
}
