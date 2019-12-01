package com.kenshoo.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import java.util.Collection;

import static org.jooq.lambda.Seq.seq;

public class AllItemsAreDifferent<T> extends TypeSafeMatcher<Collection<T>> {

    @Override
    protected boolean matchesSafely(Collection<T> items) {
        return ((int)seq(items).distinct().count()) == items.size();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("There are duplicate items in the collection");
    }

    public static <T> AllItemsAreDifferent<T> allItemsAreDifferent() {
        return new AllItemsAreDifferent<>();
    }
}
