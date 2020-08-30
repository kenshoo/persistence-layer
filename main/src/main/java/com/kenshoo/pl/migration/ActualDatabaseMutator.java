package com.kenshoo.pl.migration;

import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.Identifier;
import java.util.Collection;


public interface ActualDatabaseMutator<E extends EntityType<E>, ID extends Identifier<E>> {
    Collection<RealMutatorError<E, ID>> run();
}
