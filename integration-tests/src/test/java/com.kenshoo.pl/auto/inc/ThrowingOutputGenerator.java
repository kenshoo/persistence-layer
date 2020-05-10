package com.kenshoo.pl.auto.inc;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.OutputGenerator;
import java.util.Collection;


public class ThrowingOutputGenerator<E extends EntityType<E>> implements OutputGenerator<E> {

    int numOfTimesToFail;

    public ThrowingOutputGenerator(int numOfTimesToFail) {
        this.numOfTimesToFail = numOfTimesToFail;
    }

    @Override
    public void generate(Collection<? extends EntityChange<E>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext) {
        if (numOfTimesToFail-- > 0) {
            System.out.println("ThrowingOutputGenerator is failing. " + numOfTimesToFail + " more failures left");
            throw new RuntimeException("ThrowingOutputGenerator is failing on purpose");
        }
    }


}
