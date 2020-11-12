package com.kenshoo.pl.entity.equalityfunctions;

import com.google.common.base.Strings;

/**
 * Created by yuvalr on 5/22/16.
 */
public class EmptyAsNullStringEqualityFunction implements EntityValueEqualityFunction<String> {

    public static final EmptyAsNullStringEqualityFunction INSTANCE = new EmptyAsNullStringEqualityFunction();

    @Override
    public Boolean apply(String s, String s2) {
        return Strings.nullToEmpty(s).equals(Strings.nullToEmpty(s2));
    }
}
