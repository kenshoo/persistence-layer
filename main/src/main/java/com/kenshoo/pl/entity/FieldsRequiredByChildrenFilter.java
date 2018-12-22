package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.internal.ChangesFilter;

import java.util.Collection;
import java.util.stream.Stream;


public class FieldsRequiredByChildrenFilter <PARENT extends EntityType<PARENT>> implements ChangesFilter<PARENT> {
    
    @Override
    public <T extends EntityChange<PARENT>> Collection<T> filter(Collection<T> changes, ChangeOperation changeOperation, ChangeContext changeContext) {
        return changes;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<PARENT>> cmds, ChangeOperation changeOperation) {

        if (hasAnyChildCommand(cmds)) {
            EntityType<PARENT> entityType = cmds.iterator().next().getEntityType();
            return entityType.findFields(entityType.getPrimaryTable().getPrimaryKey().getFields()).stream();
        } else {
            return Stream.empty();
        }
    }

    private boolean hasAnyChildCommand(Collection<? extends ChangeEntityCommand<PARENT>> cmds) {
        return cmds.stream().flatMap(cmd -> cmd.getChildren()).findAny().isPresent();
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.UPDATE_AND_DELETE;
    }
}
