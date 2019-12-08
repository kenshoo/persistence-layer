package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.UniqueKey;

import java.util.Optional;

/**
 * Created by libbyfr on 12/4/2019.
 */
public interface MissingChildrenSupplier<CHILD extends EntityType<CHILD>> {

    Optional<ChangeEntityCommand<CHILD>> supplyNewCommand(Identifier<CHILD> missingChild);
    CHILD getChildType();
}
