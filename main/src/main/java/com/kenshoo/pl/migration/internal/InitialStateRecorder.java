package com.kenshoo.pl.migration.internal;

import com.google.common.collect.Maps;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.OutputGenerator;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toMap;


public class InitialStateRecorder<E extends EntityType<E>> implements OutputGenerator<E> {

    private final Collection<EntityField<E, ?>> fields;
    private Map<Identifier<E>, CurrentEntityState> entities = Maps.newHashMap();

    public InitialStateRecorder(Collection<EntityField<E, ?>> fields) {
        this.fields = fields;
    }

    @Override
    public void generate(Collection<? extends EntityChange<E>> commands, ChangeOperation op, ChangeContext ctx) {
        this.entities = commands.stream().collect(toMap(EntityChange::getIdentifier, ctx::getEntity));
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
        return requiredFields();
    }

    public Entity get(EntityChange<E> cmd) {
        return entities.get(cmd);
    }

    private Stream<? extends EntityField<?, ?>> requiredFields() {
        return fields.stream();
    }

}
