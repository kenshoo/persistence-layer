package com.kenshoo.pl.entity.equalityfunctions;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by yuvalr on 5/22/16.
 */
public class EmptyAsNullStringEqualityFunctionTest {

    @Test
    public void notEqual() throws Exception {
        EmptyAsNullStringEqualityFunction onTest = new EmptyAsNullStringEqualityFunction();
        assertFalse(onTest.apply("hudson", "moses"));
    }

    @Test
    public void equal() throws Exception {
        EmptyAsNullStringEqualityFunction onTest = new EmptyAsNullStringEqualityFunction();
        assertTrue(onTest.apply("tiger lili", "tiger lili"));
    }

    @Test
    public void firstNull() throws Exception {
        EmptyAsNullStringEqualityFunction onTest = new EmptyAsNullStringEqualityFunction();
        assertFalse(onTest.apply(null, "goocha"));
    }

    @Test
    public void secondNull() throws Exception {
        EmptyAsNullStringEqualityFunction onTest = new EmptyAsNullStringEqualityFunction();
        assertFalse(onTest.apply("giraffe", null));
    }

    @Test
    public void bothEmpty() throws Exception {
        EmptyAsNullStringEqualityFunction onTest = new EmptyAsNullStringEqualityFunction();
        assertTrue(onTest.apply("", ""));
    }

    @Test
    public void bothNull() throws Exception {
        EmptyAsNullStringEqualityFunction onTest = new EmptyAsNullStringEqualityFunction();
        assertTrue(onTest.apply(null, null));
    }

    @Test
    public void firstNullSecondEmpty() throws Exception {
        EmptyAsNullStringEqualityFunction onTest = new EmptyAsNullStringEqualityFunction();
        assertTrue(onTest.apply(null, ""));
    }

    @Test
    public void firstEmptySecondNull() throws Exception {
        EmptyAsNullStringEqualityFunction onTest = new EmptyAsNullStringEqualityFunction();
        assertTrue(onTest.apply("", null));
    }
}