package com.kenshoo.pl.entity.equalityfunctions;

import org.apache.commons.lang3.ObjectUtils;

/**
 * This Function overrides the default Bid Field comparison of using {@code equals()} with {@code compare()}.
 * It is needed because BigDecimals' {@code equals()} takes into account the number's scale, which means <br>
 * {@code 0.0 != 0.00} <br>
 * This can cause strange behaviours, especially when comparing UI values against DB values.<br>
 * Using {@code compare()} instead solves this problem as it ignores scale when the values are the same.<br><br>
 *
 * Created by yuvalr on 5/22/16.
 */
public class ComparableEqualityFunction<T extends Comparable<? super T>> implements EntityValueEqualityFunction<T> {

    private static final ComparableEqualityFunction INSTANCE = new ComparableEqualityFunction();

    private ComparableEqualityFunction() {
    }

    @Override
    public Boolean apply(T bigDecimal, T bigDecimal2) {
        return ObjectUtils.compare(bigDecimal, bigDecimal2) == 0;
    }

    public static <T extends Comparable<? super T>> ComparableEqualityFunction<T> getInstance() {
        //noinspection unchecked
        return INSTANCE;
    }
}
