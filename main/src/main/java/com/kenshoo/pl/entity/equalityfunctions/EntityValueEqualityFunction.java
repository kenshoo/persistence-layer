package com.kenshoo.pl.entity.equalityfunctions;

import java.util.function.BiFunction;

/**
 * This function interface allows overriding the default Object.equals behaviour used when comparing an Entity fields' value.
 * Exemplary flows for such a comparison are {@code EntityChangeLogOutputGenerator} and {@code FalseUpdatesFilter}.<br><br>
 *
 * Created by yuvalr on 5/22/16.
 */
@FunctionalInterface
public interface EntityValueEqualityFunction<T> extends BiFunction<T, T, Boolean> {
}
