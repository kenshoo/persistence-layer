package com.kenshoo.pl.entity.internal;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class CollectionViewTest {


    private static final Predicate<Object> TRUE_PREDICATE = object -> true;
    private static final Predicate<Object> FALSE_PREDICATE = object -> false;

    @Test
    public void test_count_true_predicate() {
        List<Object> objects = List.of(new Object());
        CollectionView<Object> collectionView = new CollectionView<>(objects, TRUE_PREDICATE);
        assertThat(collectionView.size(), is(1));
    }

    @Test
    public void test_count_false_predicate() {
        List<Object> objects = List.of(new Object());
        CollectionView<Object> collectionView = new CollectionView<>(objects, FALSE_PREDICATE);
        assertThat(collectionView.size(), is(0));
    }

    @Test
    public void test_isEmpty_true_predicate() {
        List<Object> objects = List.of(new Object());
        CollectionView<Object> collectionView = new CollectionView<>(objects, TRUE_PREDICATE);
        assertThat(collectionView.isEmpty(), is(false));
    }

    @Test
    public void test_isEmpty_false_predicate() {
        List<Object> objects = List.of(new Object());
        CollectionView<Object> collectionView = new CollectionView<>(objects, FALSE_PREDICATE);
        assertThat(collectionView.isEmpty(), is(true));
    }

    @Test
    public void test_iterator_true_predicate() {
        Object element = new Object();
        List<Object> objects = List.of(element);
        CollectionView<Object> collectionView = new CollectionView<>(objects, TRUE_PREDICATE);
        Iterator<Object> iterator = collectionView.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(element));
    }

    @Test
    public void test_contains_true_predicate() {
        Object element = new Object();
        List<Object> objects = List.of(element);
        CollectionView<Object> collectionView = new CollectionView<>(objects, TRUE_PREDICATE);
        assertThat(collectionView.contains(element), is(true));
        assertThat(collectionView.containsAll(objects), is(true));
    }

    @Test
    public void test_contains_false_predicate() {
        Object element = new Object();
        List<Object> objects = List.of(element);
        CollectionView<Object> collectionView = new CollectionView<>(objects, FALSE_PREDICATE);
        assertThat(collectionView.contains(element), is(false));
        assertThat(collectionView.containsAll(objects), is(false));
    }

    @Test
    public void test_iterator_false_predicate() {
        List<Object> objects = List.of(new Object());
        CollectionView<Object> collectionView = new CollectionView<>(objects, FALSE_PREDICATE);
        Iterator<Object> iterator = collectionView.iterator();
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void test_toArray_true_predicate() {
        List<Object> objects = List.of(new Object(), new Object());
        CollectionView<Object> collectionView = new CollectionView<>(objects, TRUE_PREDICATE);
        Object[] array = collectionView.toArray();
        Assert.assertThat(objects, contains(array));
    }

    @Test
    public void test_toArray_false_predicate() {
        List<Object> objects = List.of(new Object(), new Object());
        CollectionView<Object> collectionView = new CollectionView<>(objects, FALSE_PREDICATE);
        Object[] array = collectionView.toArray();
        assertThat(array.length, is(0));
    }

    @Test
    public void test_generic_toArray_true_predicate() {
        List<Integer> objects = List.of(1, 2);
        CollectionView<Integer> collectionView = new CollectionView<>(objects, e -> true);
        Object[] array = collectionView.toArray();
        Assert.assertThat(objects, contains(array));
    }

    @Test
    public void test_generic_toArray_false_predicate() {
        List<Integer> objects = List.of(1, 2);
        CollectionView<Integer> collectionView = new CollectionView<>(objects, e -> false);
        Object[] array = collectionView.toArray();
        assertThat(array.length, is(0));
    }
}