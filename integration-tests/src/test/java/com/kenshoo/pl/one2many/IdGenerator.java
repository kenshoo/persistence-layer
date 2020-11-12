package com.kenshoo.pl.one2many;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class IdGenerator implements Supplier<Integer> {

    private int nextId = 1;
    public final List<Integer> generatedIds = new LinkedList<>();

    @Override
    public Integer get() {
        generatedIds.add(nextId);
        return nextId++;
    }
}
