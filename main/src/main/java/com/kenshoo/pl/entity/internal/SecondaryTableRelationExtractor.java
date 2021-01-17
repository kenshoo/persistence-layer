package com.kenshoo.pl.entity.internal;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityFieldDbAdapter;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.ValueConverter;
import com.kenshoo.pl.entity.converters.IdentityValueConverter;
import org.jooq.Record;
import org.jooq.TableField;
import java.util.Objects;
import java.util.stream.Stream;

import static org.jooq.lambda.Seq.seq;


class SecondaryTableRelationExtractor {

    /**
     * This usually results with creating "fake" temporary EntityField because it is not likely that the Entity
     * defines such an entity field.
     * For example:
     *
     * ----- Table fields -----
     *
     * table campaign {
     *     int id;
     * }
     *
     * table campaign_secondary {
     *     int campaign_id;  // FK to campaign
     * }
     *
     * ----- Entity fields -----
     *
     * class CampaignEntity {
     *     //
     *     // This is obvious:
     *     //
     *     static final EntityField ID = field(campaign.id);
     *     //
     *     // This is very unlikely:
     *     //
     *     static final EntityField ID_OF_PRIMARY_LOCATED_ON_SECONDARY_TABLE = field(campaign_secondary.campaign_id);
     * }
     *
     * This method will create the unlikely EntityField assuming we can use it for fetching.
     */
    static <E extends EntityType<E>> Stream<EntityField<E, ?>> relationUsingTableFieldsOfSecondary(DataTable secondaryTable, E entityType) {
        var foreignKey = secondaryTable.getForeignKey(entityType.getPrimaryTable());
        var primaryAndSecondaryFieldPairs = seq(foreignKey.getKey().getFields()).zip(foreignKey.getFields());
        return primaryAndSecondaryFieldPairs
                .map(ps -> entityType.findField(ps.v2).orElseGet(() -> createTemporaryEntityField(entityType, ps.v2, ps.v1)))
                .map(KeyFieldFromSecondary::new);
    }

    private static <T, E extends EntityType<E>> EntityField<E, ?> createTemporaryEntityField(E entityType, TableField<Record, T> secondaryField, TableField<?, ?> primaryTableField) {
        var primaryField = entityType.findField(primaryTableField).get();
        var converter = IdentityValueConverter.getInstance(secondaryField.getType());
        return new EntityFieldImpl(entityType, new SimpleEntityFieldDbAdapter<>(secondaryField, converter), primaryField.getStringValueConverter(), Objects::equals);
    }

    static <E extends EntityType<E>> Stream<? extends EntityField<E, ?>> relationUsingTableFieldsOfPrimary(DataTable secondaryTable, E entityType) {
        var foreignKey = secondaryTable.getForeignKey(entityType.getPrimaryTable());
        var primaryFields = foreignKey.getKey().getFields();
        return seq(primaryFields)
                .map(field -> entityType.findField(field)
                        .orElseThrow(() -> new IllegalStateException(String.format("field %s is a FK from table %s to %s but is not defined on entity type %s.", field, secondaryTable.getName(), entityType.getPrimaryTable().getName(), entityType.getName()))));
    }

    /**
     * We use this type as a marker
     */
    public static class KeyFieldFromSecondary<E extends EntityType<E>, T> implements EntityField<E, T> {
        private final EntityField<E, T> delegate;

        private KeyFieldFromSecondary(EntityField<E, T> delegate) { this.delegate = delegate; }
        @Override public EntityFieldDbAdapter<T> getDbAdapter() { return delegate.getDbAdapter(); }
        @Override public ValueConverter<T, String> getStringValueConverter() { return delegate.getStringValueConverter(); }
        @Override public boolean valuesEqual(T v1, T v2) { return delegate.valuesEqual(v1, v2); }
        @Override public EntityType<E> getEntityType() { return delegate.getEntityType(); }
    }

}
