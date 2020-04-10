package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.annotation.*;
import com.kenshoo.pl.entity.internal.*;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;
import org.jooq.lambda.tuple.Tuple2;
import java.time.Instant;
import java.util.Optional;

import static com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil.annotatedWith;
import static com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil.getFieldAnnotation;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class ChangeFlowConfigBuilderFactory {

     public static <E extends EntityType<E>> ChangeFlowConfig.Builder<E> newInstance(PLContext plContext, E entityType) {
        //noinspection unchecked
        ChangeFlowConfig.Builder<E> builder = ChangeFlowConfig.builder(entityType)
                .withOutputGenerator(new DbCommandsOutputGenerator<>(entityType, plContext))
                .with(plContext.generateFeatureSet());

        builder.withRetryer(plContext.persistenceLayerRetryer());
        builder.withFalseUpdatesPurger(new FalseUpdatesPurger.Builder<E>()
                .setFieldUnsetter(ChangeEntityCommand::unset)
                .setDeleteIfSetAloneFields(entityType.getFields().filter(annotatedWith(entityType, IgnoredIfSetAlone.class)))
                .setFieldsToRetain(entityType.getFields().filter(annotatedWith(entityType, DontPurge.class)))
                .build());
        builder.withRequiredRelationFields(entityType.getFields()
                .filter(entityField -> {
                    Required requiredAnnotation = getFieldAnnotation(entityType, entityField, Required.class);
                    return requiredAnnotation != null && requiredAnnotation.value() == RequiredFieldType.RELATION;
                }));
        builder.withRequiredFields(entityType.getFields().filter(annotatedWith(entityType, Required.class)));
        builder.withImmutableFields(entityType.getFields().filter(annotatedWith(entityType, Immutable.class)));
        Optional<EntityField<E, ?>> creationDateField = entityType.getFields()
                .filter(annotatedWith(entityType, CreationDate.class))
                .findFirst();
        if (creationDateField.isPresent()) {
            if (creationDateField.get().getStringValueConverter().getValueClass() != Instant.class) {
                throw new IllegalArgumentException("Field annotated with @" + CreationDate.class.getSimpleName() +
                        " should be of type " + Instant.class.getName() + ". Field " + entityType.toFieldName(creationDateField.get()) +
                        " has type " + creationDateField.get().getStringValueConverter().getValueClass().getName());
            }
            //noinspection unchecked
            EntityField<E, Instant> entityField = (EntityField<E, Instant>) creationDateField.get();
            builder.withPostFetchCommandEnricher(new CreationDateEnricher<>(entityField));
        }

        entityType.getFields()
                .<Tuple2<EntityField<E, ?>, DefaultValue>>map(entityField -> tuple(entityField, EntityTypeReflectionUtil.getFieldAnnotation(entityType, entityField, DefaultValue.class)))
                .filter(tuple -> tuple.v2 != null)
                .map(tuple -> defaultFieldValueEnricher(tuple.v1, tuple.v2.value()))
                .forEach(builder::withPostFetchCommandEnricher);


        return builder;
    }

    private static <E extends EntityType<E>, T> PostFetchCommandEnricher<E> defaultFieldValueEnricher(EntityField<E, T> field, String value) {
        return new DefaultFieldValueEnricher<>(field, field.getStringValueConverter().convertFrom(value));
    }

}
