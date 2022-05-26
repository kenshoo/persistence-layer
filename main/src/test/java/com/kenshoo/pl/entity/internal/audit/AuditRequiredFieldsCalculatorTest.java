package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithoutIdType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import org.jooq.lambda.Seq;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.audit.AuditTrigger.*;
import static com.kenshoo.pl.entity.internal.audit.AuditedEntityType.builder;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AuditRequiredFieldsCalculatorTest {

    private static final ChangeOperation OPERATOR = ChangeOperation.UPDATE;

    @Test
    public void requiredFields_TypeHasIdOnly_ToChangeAreDifferent_ShouldReturnIdOnly() {
        final var auditedEntityType = builder(AuditedAutoIncIdType.INSTANCE).build();

        final var fieldsToChange = Set.of(AuditedAutoIncIdType.NAME,
                AuditedAutoIncIdType.DESC,
                AuditedAutoIncIdType.DESC2);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(toUnmodifiableSet()),
                is(Collections.<EntityField<?, ?>>singleton(AuditedAutoIncIdType.ID)));
    }

    @Test
    public void requiredFields_TypeHasIdOnly_ToChangeIncludeIdAndOthers_ShouldReturnIdOnly() {
        final var auditedEntityType = builder(AuditedAutoIncIdType.INSTANCE).build();

        final var fieldsToChange = Set.of(AuditedAutoIncIdType.ID,
                AuditedAutoIncIdType.NAME,
                AuditedAutoIncIdType.DESC,
                AuditedAutoIncIdType.DESC2);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(toUnmodifiableSet()),
                is(Collections.<EntityField<?, ?>>singleton(AuditedAutoIncIdType.ID)));
    }

    @Test
    public void requiredFields_TypeHasIdAndExternalMandatoryOnly_ToChangeNotEmpty_ShouldReturnFieldsOfType() {
        final var auditedEntityType = builder(AuditedAutoIncIdType.INSTANCE)
                .withUnderlyingExternalFields(NotAuditedAncestorType.NAME,
                        NotAuditedAncestorType.DESC)
                .build();

        final var fieldsToChange = singleton(AuditedAutoIncIdType.NAME);

        final var expectedRequiredFields = toEntityFields(auditedEntityType);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasIdAndSelfMandatoryOnly_ToChangeAreDifferent_ShouldReturnFieldsOfType() {
        final var auditedEntityType = builder(AuditedAutoIncIdType.INSTANCE)
                .withUnderlyingInternalFields(ALWAYS, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                .build();

        final var fieldsToChange = singleton(AuditedAutoIncIdType.DESC2);

        final var expectedRequiredFields = toEntityFields(auditedEntityType);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasIdAndSelfMandatoryOnly_ToChangeAreTheSame_ShouldReturnFieldsToChange() {
        final var auditedEntityType =
                builder(AuditedAutoIncIdType.INSTANCE)
                        .withUnderlyingInternalFields(ALWAYS, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                        .build();

        final var fieldsToChange = Set.of(AuditedAutoIncIdType.ID,
                AuditedAutoIncIdType.NAME,
                AuditedAutoIncIdType.DESC);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(fieldsToChange));
    }

    @Test
    public void requiredFields_TypeHasIdAndSelfMandatoryOnly_ToChangePartiallyIntersect_ShouldReturnFieldsOfType() {
        final var auditedEntityType =
                builder(AuditedAutoIncIdType.INSTANCE)
                        .withUnderlyingInternalFields(ALWAYS, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                        .build();

        final var fieldsToChange = Set.of(AuditedAutoIncIdType.DESC,
                AuditedAutoIncIdType.DESC2);

        final var expectedRequiredFields = toEntityFields(auditedEntityType);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasIdAndOnCreateOrUpdateOnly_ToChangeAreDifferent_ShouldReturnIdOnly() {
        final var auditedEntityType =
                builder(AuditedAutoIncIdType.INSTANCE)
                        .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                        .build();

        final var fieldsToChange = singleton(AuditedAutoIncIdType.DESC2);

        final var expectedRequiredFields = singleton(AuditedAutoIncIdType.ID);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasIdAndOnCreateOrUpdateOnly_ToChangeAreSame_ShouldReturnFieldsToChange() {
        final var auditedEntityType =
                builder(AuditedAutoIncIdType.INSTANCE)
                        .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                        .build();

        final var fieldsToChange = Set.of(AuditedAutoIncIdType.ID,
                AuditedAutoIncIdType.NAME,
                AuditedAutoIncIdType.DESC);

        final var expectedRequiredFields = toEntityFields(auditedEntityType);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasIdAndOnCreateOrUpdateOnly_ToChangeIncludedInOnCreateOrUpdate_ShouldReturnIdAndFieldsToChange() {
        final var auditedEntityType =
                builder(AuditedAutoIncIdType.INSTANCE)
                        .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE,
                                AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC, AuditedAutoIncIdType.DESC2)
                        .build();

        final var fieldsToChange = Set.of(AuditedAutoIncIdType.DESC,
                AuditedAutoIncIdType.DESC2);

        final var expectedRequiredFields = Set.of(AuditedAutoIncIdType.ID,
                AuditedAutoIncIdType.DESC,
                AuditedAutoIncIdType.DESC2);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasIdAndOnCreateOrUpdateOnly_ToChangeContainOnCreateOrUpdate_ShouldReturnFieldsOfType() {
        final var auditedEntityType =
                builder(AuditedAutoIncIdType.INSTANCE)
                        .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE,
                                AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                        .build();

        final var fieldsToChange = Set.of(AuditedAutoIncIdType.NAME,
                AuditedAutoIncIdType.DESC,
                AuditedAutoIncIdType.DESC2);

        final var expectedRequiredFields = toEntityFields(auditedEntityType);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasIdAndOnCreateOrUpdateOnly_ToChangePartiallyIntersectOnCreateOrUpdate_ShouldReturnIdAndIntersection() {
        final var auditedEntityType =
                builder(AuditedAutoIncIdType.INSTANCE)
                        .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                        .build();

        final var fieldsToChange = Set.of(AuditedAutoIncIdType.DESC,
                AuditedAutoIncIdType.DESC2);

        final var expectedRequiredFields = Set.of(AuditedAutoIncIdType.ID,
                AuditedAutoIncIdType.DESC);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasIdAndOnUpdateOnly_ToChangeAreDifferent_ShouldReturnIdOnly() {
        final var auditedEntityType =
                builder(AuditedAutoIncIdType.INSTANCE)
                        .withUnderlyingInternalFields(ON_UPDATE, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                        .build();

        final var fieldsToChange = singleton(AuditedAutoIncIdType.DESC2);

        final var expectedRequiredFields = singleton(AuditedAutoIncIdType.ID);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasIdAndOnUpdateOnly_ToChangeAreSame_ShouldReturnFieldsToChange() {
        final var auditedEntityType =
                builder(AuditedAutoIncIdType.INSTANCE)
                        .withUnderlyingInternalFields(ON_UPDATE, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                        .build();

        final var fieldsToChange = Set.of(AuditedAutoIncIdType.ID,
                AuditedAutoIncIdType.NAME,
                AuditedAutoIncIdType.DESC);

        final var expectedRequiredFields = toEntityFields(auditedEntityType);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasIdAndOnUpdateOnly_ToChangeIncludedInOnCreateOrUpdate_ShouldReturnIdAndFieldsToChange() {
        final var auditedEntityType =
                builder(AuditedAutoIncIdType.INSTANCE)
                        .withUnderlyingInternalFields(ON_UPDATE,
                                AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC, AuditedAutoIncIdType.DESC2)
                        .build();

        final var fieldsToChange = Set.of(AuditedAutoIncIdType.DESC,
                AuditedAutoIncIdType.DESC2);

        final var expectedRequiredFields = Set.of(AuditedAutoIncIdType.ID,
                AuditedAutoIncIdType.DESC,
                AuditedAutoIncIdType.DESC2);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasIdAndOnUpdateOnly_ToChangeContainOnCreateOrUpdate_ShouldReturnType() {
        final var auditedEntityType =
                builder(AuditedAutoIncIdType.INSTANCE)
                        .withUnderlyingInternalFields(ON_UPDATE, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                        .build();

        final var fieldsToChange = Set.of(AuditedAutoIncIdType.NAME,
                AuditedAutoIncIdType.DESC,
                AuditedAutoIncIdType.DESC2);

        final var expectedRequiredFields = toEntityFields(auditedEntityType);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasIdAndOnUpdateOnly_ToChangePartiallyIntersectOnCreateOrUpdate_ShouldReturnIdAndIntersection() {
        final var auditedEntityType =
                builder(AuditedAutoIncIdType.INSTANCE)
                        .withUnderlyingInternalFields(ON_UPDATE, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                        .build();

        final var fieldsToChange = Set.of(AuditedAutoIncIdType.DESC,
                AuditedAutoIncIdType.DESC2);

        final var expectedRequiredFields = Set.of(AuditedAutoIncIdType.ID,
                AuditedAutoIncIdType.DESC);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasEverything_ToChangePartiallyIntersectOnCreateOrUpdate_ShouldReturnIdAndMandatoriesAndIntersection() {
        final var auditedEntityType =
                builder(AuditedAutoIncIdType.INSTANCE)
                        .withUnderlyingExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                        .withUnderlyingInternalFields(ALWAYS, AuditedAutoIncIdType.NAME)
                        .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedAutoIncIdType.DESC)
                        .withUnderlyingInternalFields(ON_UPDATE, AuditedAutoIncIdType.DESC2)
                        .build();

        final var fieldsToChange = singleton(AuditedAutoIncIdType.DESC);

        final var expectedRequiredFields = Set.of(AuditedAutoIncIdType.ID,
                NotAuditedAncestorType.NAME,
                NotAuditedAncestorType.DESC,
                AuditedAutoIncIdType.NAME,
                AuditedAutoIncIdType.DESC);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasEverything_ToChangePartiallyIntersectOnUpdate_ShouldReturnIdAndMandatoriesAndIntersection() {
        final var auditedEntityType =
                builder(AuditedAutoIncIdType.INSTANCE)
                        .withUnderlyingExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                        .withUnderlyingInternalFields(ALWAYS, AuditedAutoIncIdType.NAME)
                        .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedAutoIncIdType.DESC)
                        .withUnderlyingInternalFields(ON_UPDATE, AuditedAutoIncIdType.DESC2)
                        .build();

        final var fieldsToChange = singleton(AuditedAutoIncIdType.DESC);

        final var expectedRequiredFields = Set.of(AuditedAutoIncIdType.ID,
                NotAuditedAncestorType.NAME,
                NotAuditedAncestorType.DESC,
                AuditedAutoIncIdType.NAME,
                AuditedAutoIncIdType.DESC);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeIsEmpty_ToChangeNotEmpty_ShouldReturnEmpty() {
        final var auditedEntityType = builder(AuditedWithoutIdType.INSTANCE).build();

        final var fieldsToChange = Set.of(AuditedWithoutIdType.NAME, AuditedWithoutIdType.DESC);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(toUnmodifiableSet()), is(emptySet()));
    }

    @Test
    public void requiredFields_TypeHasExternalMandatoryOnly_ToChangeNotEmpty_ShouldReturnFieldsOfType() {
        final var auditedEntityType = builder(AuditedWithoutIdType.INSTANCE)
                .withUnderlyingExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                .build();

        final var fieldsToChange = singleton(AuditedWithoutIdType.NAME);

        final var expectedRequiredFields = toEntityFields(auditedEntityType);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasSelfMandatoryOnly_ToChangeAreDifferent_ShouldReturnFieldsOfType() {
        final var auditedEntityType = builder(AuditedWithoutIdType.INSTANCE)
                .withUnderlyingInternalFields(ALWAYS, AuditedWithoutIdType.NAME, AuditedWithoutIdType.DESC)
                .build();

        final var fieldsToChange = singleton(AuditedWithoutIdType.DESC2);

        final var expectedRequiredFields = toEntityFields(auditedEntityType);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasSelfMandatoryOnly_ToChangeAreTheSame_ShouldReturnFieldsToChange() {
        final var auditedEntityType = builder(AuditedWithoutIdType.INSTANCE)
                .withUnderlyingInternalFields(ALWAYS, AuditedWithoutIdType.NAME, AuditedWithoutIdType.DESC)
                .build();

        final var fieldsToChange = Set.of(AuditedWithoutIdType.NAME, AuditedWithoutIdType.DESC);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(fieldsToChange));
    }

    @Test
    public void requiredFields_TypeHasSelfMandatoryOnly_ToChangePartiallyIntersect_ShouldReturnFieldsOfType() {
        final var auditedEntityType = builder(AuditedWithoutIdType.INSTANCE)
                .withUnderlyingInternalFields(ALWAYS, AuditedWithoutIdType.NAME, AuditedWithoutIdType.DESC)
                .build();

        final var fieldsToChange = Set.of(AuditedWithoutIdType.DESC, AuditedWithoutIdType.DESC2);

        final var expectedRequiredFields = toEntityFields(auditedEntityType);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasOnCreateOrUpdateOnly_ToChangeAreDifferent_ShouldReturnEmpty() {
        final var auditedEntityType = builder(AuditedWithoutIdType.INSTANCE)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedWithoutIdType.NAME, AuditedWithoutIdType.DESC)
                .build();

        final var fieldsToChange = singleton(AuditedWithoutIdType.DESC2);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(emptySet()));
    }

    @Test
    public void requiredFields_TypeHasOnCreateOrUpdateOnly_ToChangeAreSame_ShouldReturnFieldsToChange() {
        final var auditedEntityType = builder(AuditedWithoutIdType.INSTANCE)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedWithoutIdType.NAME, AuditedWithoutIdType.DESC)
                .build();

        final var fieldsToChange = Set.of(AuditedWithoutIdType.NAME, AuditedWithoutIdType.DESC);

        final var expectedRequiredFields = toEntityFields(auditedEntityType);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasOnCreateOrUpdateOnly_ToChangeIncludedInOnCreateOrUpdate_ShouldReturnFieldsToChange() {
        final var auditedEntityType =
                builder(AuditedWithoutIdType.INSTANCE)
                        .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE,
                                AuditedWithoutIdType.NAME, AuditedWithoutIdType.DESC, AuditedWithoutIdType.DESC2)
                        .build();

        final var fieldsToChange = Set.of(AuditedWithoutIdType.DESC, AuditedWithoutIdType.DESC2);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(fieldsToChange));
    }

    @Test
    public void requiredFields_TypeHasOnCreateOrUpdateOnly_ToChangeContainOnCreateOrUpdate_ShouldReturnFieldsOfType() {
        final var auditedEntityType =
                builder(AuditedWithoutIdType.INSTANCE)
                        .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE,
                                AuditedWithoutIdType.NAME, AuditedWithoutIdType.DESC)
                        .build();

        final var fieldsToChange = Set.of(AuditedWithoutIdType.NAME,
                AuditedWithoutIdType.DESC,
                AuditedWithoutIdType.DESC2);

        final var expectedRequiredFields = toEntityFields(auditedEntityType);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    @Test
    public void requiredFields_TypeHasOnCreateOrUpdateOnly_ToChangePartiallyIntersectOnCreateOrUpdate_ShouldReturnIntersection() {
        final var auditedEntityType =
                builder(AuditedWithoutIdType.INSTANCE)
                        .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedWithoutIdType.NAME, AuditedWithoutIdType.DESC)
                        .build();

        final var fieldsToChange = Set.of(AuditedWithoutIdType.DESC, AuditedWithoutIdType.DESC2);

        final var expectedRequiredFields = Set.of(AuditedWithoutIdType.DESC);

        final var actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toUnmodifiableSet()), is(expectedRequiredFields));
    }

    private <E extends EntityType<E>> Stream<? extends EntityField<?, ?>> calculate(final AuditedEntityType<E> auditedEntityType,
                                                                                    final Set<? extends EntityField<E, ?>> fieldsToChange) {
        return new AuditRequiredFieldsCalculator<>(auditedEntityType).requiredFields(fieldsToChange, OPERATOR);
    }

    private Set<? extends EntityField<?, ?>> toEntityFields(final AuditedEntityType<?> auditedEntityType) {
        return Seq.<EntityField<?, ?>>seq(auditedEntityType.getExternalFields().stream().map(AuditedField::getField))
                .append(auditedEntityType.getInternalFields().map(AuditedField::getField))
                .append(auditedEntityType.getIdField())
                .collect(toUnmodifiableSet());
    }
}