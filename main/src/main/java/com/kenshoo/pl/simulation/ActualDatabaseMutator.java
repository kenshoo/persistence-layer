package com.kenshoo.pl.simulation;

import com.kenshoo.pl.entity.EntityType;
import java.util.Collection;


public interface ActualDatabaseMutator<E extends EntityType<E>> {
    Collection<ActualMutatorError<E>> run();
}
