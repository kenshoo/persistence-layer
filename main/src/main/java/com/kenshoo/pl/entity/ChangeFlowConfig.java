package com.kenshoo.pl.entity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.internal.*;
import com.kenshoo.pl.entity.internal.audit.*;
import com.kenshoo.pl.entity.spi.*;
import com.kenshoo.pl.entity.spi.helpers.EntityChangeCompositeValidator;
import com.kenshoo.pl.entity.spi.helpers.ImmutableFieldValidatorImpl;
import com.kenshoo.pl.entity.spi.helpers.RequiredFieldValidatorImpl;
import org.jooq.lambda.Seq;

import java.util.*;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.spi.PersistenceLayerRetryer.JUST_RUN_WITHOUT_CHECKING_DEADLOCKS;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;


public class ChangeFlowConfig<E extends EntityType<E>> {

    private final static Label NonExcludebale = new Label() {
    };

    private final E entityType;
    private final List<PostFetchCommandEnricher<E>> postFetchCommandEnrichers;
    private final List<OutputGenerator<E>> outputGenerators;
    private final List<ChangesValidator<E>> validators;
    private final Set<EntityField<E, ?>> requiredRelationFields;
    private final Set<EntityField<E, ?>> requiredFields;

    private final List<ChangeFlowConfig<? extends EntityType<?>>> childFlows;
    private final List<ChangesFilter<E>> postFetchFilters;
    private final List<ChangesFilter<E>> postSupplyFilters;
    private final PersistenceLayerRetryer retryer;
    private final AuditRequiredFieldsCalculator<E> auditRequiredFieldsCalculator;
    private final AuditRecordGenerator<E> auditRecordGenerator;
    private final FeatureSet features;


    private ChangeFlowConfig(E entityType,
                             List<PostFetchCommandEnricher<E>> postFetchCommandEnrichers,
                             List<ChangesValidator<E>> validators,
                             List<OutputGenerator<E>> outputGenerators,
                             Set<EntityField<E, ?>> requiredRelationFields,
                             Set<EntityField<E, ?>> requiredFields,
                             List<ChangeFlowConfig<? extends EntityType<?>>> childFlows,
                             PersistenceLayerRetryer retryer,
                             final AuditRequiredFieldsCalculator<E> auditRequiredFieldsCalculator,
                             final AuditRecordGenerator<E> auditRecordGenerator,
                             FeatureSet features) {
        this.entityType = entityType;
        this.postFetchCommandEnrichers = postFetchCommandEnrichers;
        this.outputGenerators = outputGenerators;
        this.validators = validators;
        this.requiredRelationFields = requiredRelationFields;
        this.requiredFields = requiredFields;
        this.childFlows = childFlows;
        this.postFetchFilters = ImmutableList.of(new MissingParentEntitiesFilter<>(entityType.determineForeignKeys(requiredRelationFields).collect(toList())), new MissingEntitiesFilter<>(entityType));
        this.postSupplyFilters = features.isEnabled(Feature.RequiredFieldValidator) ? Collections.emptyList() : ImmutableList.of(new RequiredFieldsChangesFilter<>(requiredFields));
        this.retryer = retryer;
        this.auditRequiredFieldsCalculator = auditRequiredFieldsCalculator;
        this.auditRecordGenerator = auditRecordGenerator;
        this.features = features;
    }

    public E getEntityType() {
        return entityType;
    }

    public PersistenceLayerRetryer retryer() {
        return retryer;
    }

    public Optional<AuditRecordGenerator<E>> auditRecordGenerator() {
        return Optional.ofNullable(auditRecordGenerator);
    }

    public List<PostFetchCommandEnricher<E>> getPostFetchCommandEnrichers() {
        return postFetchCommandEnrichers;
    }

    public List<ChangesValidator<E>> getValidators() {
        return validators;
    }

    public List<OutputGenerator<E>> getOutputGenerators() {
        return outputGenerators;
    }

    public Stream<CurrentStateConsumer<E>> currentStateConsumers() {
        return Seq.concat(postFetchFilters,
                          postSupplyFilters,
                          postFetchCommandEnrichers,
                          validators,
                          outputGenerators)
                  .concat(Optional.ofNullable(auditRequiredFieldsCalculator));
    }

    static <E extends EntityType<E>> Builder<E> builder(E entityType) {
        return new Builder<>(entityType);
    }

    public Set<EntityField<E, ?>> getRequiredRelationFields() {
        return requiredRelationFields;
    }

    public Set<EntityField<E, ?>> getRequiredFields() {
        return requiredFields;
    }

    public List<ChangeFlowConfig<? extends EntityType<?>>> childFlows() {
        return childFlows;
    }

    public List<ChangesFilter<E>> getPostFetchFilters() {
        return postFetchFilters;
    }

    public List<ChangesFilter<E>> getPostSupplyFilters() {
        return postSupplyFilters;
    }

    public Optional<EntityField<E, Object>> getPrimaryIdentityField() {
        return getEntityType().getPrimaryIdentityField();
    }

    public FeatureSet getFeatures() {
        return this.features;
    }


    public static class Builder<E extends EntityType<E>> {
        private final E entityType;
        private final List<Labeled<? extends PostFetchCommandEnricher<E>>> postFetchCommandEnrichers = new ArrayList<>();
        private final List<Labeled<ChangesValidator<E>>> validators = new ArrayList<>();
        private final List<OutputGenerator<E>> outputGenerators = new ArrayList<>();
        private final Set<EntityField<E, ?>> requiredRelationFields = new HashSet<>();
        private final Set<EntityField<E, ?>> requiredFields = new HashSet<>();
        private Optional<PostFetchCommandEnricher<E>> falseUpdatesPurger = Optional.empty();
        private final List<ChangeFlowConfig.Builder<? extends EntityType<?>>> flowConfigBuilders = new ArrayList<>();
        private PersistenceLayerRetryer retryer = JUST_RUN_WITHOUT_CHECKING_DEADLOCKS;
        private final AuditedFieldsResolver auditedFieldsResolver;
        private FeatureSet features = FeatureSet.EMPTY;

        public Builder(E entityType) {
            this(entityType,
                 AuditedFieldsResolver.INSTANCE);
        }

        @VisibleForTesting
        Builder(final E entityType,
                final AuditedFieldsResolver auditedFieldsResolver) {
            this.entityType = entityType;
            this.auditedFieldsResolver = auditedFieldsResolver;
        }

        public Builder<E> with(FeatureSet features) {
            this.features = features;
            this.flowConfigBuilders.forEach(builder -> builder.with(features));
            return this;
        }

        public Builder<E> withLabeledPostFetchCommandEnricher(PostFetchCommandEnricher<E> enricher, Label label) {
            postFetchCommandEnrichers.add(new Labeled<>(enricher, label));
            return this;
        }

        public Builder<E> withPostFetchCommandEnricher(PostFetchCommandEnricher<E> enricher) {
            postFetchCommandEnrichers.add(new Labeled<>(enricher, NonExcludebale));
            return this;
        }

        public Builder<E> withLabeledPostFetchCommandEnrichers(Collection<? extends PostFetchCommandEnricher<E>> enrichers, Label label) {
            enrichers.forEach(e -> postFetchCommandEnrichers.add(new Labeled<>(e, label)));
            return this;
        }

        public Builder<E> withPostFetchCommandEnrichers(Collection<? extends PostFetchCommandEnricher<E>> enrichers) {
            enrichers.forEach(e -> postFetchCommandEnrichers.add(new Labeled<>(e, NonExcludebale)));
            return this;
        }

        /* not public */ void withFalseUpdatesPurger(FalseUpdatesPurger<E> falseUpdatesPurger) {
            this.falseUpdatesPurger = Optional.of(falseUpdatesPurger);
        }

        public Builder<E> withoutFalseUpdatesPurger() {
            this.falseUpdatesPurger = Optional.empty();
            this.flowConfigBuilders.forEach(Builder::withoutFalseUpdatesPurger);
            return this;
        }

        public Builder<E> withValidator(ChangesValidator<E> validator) {
            this.validators.add(new Labeled<>(validator, NonExcludebale));
            return this;
        }

        public Builder<E> withValidators(Collection<ChangesValidator<E>> validators) {
            validators.forEach(validator -> this.validators.add(new Labeled<>(validator, NonExcludebale)));
            return this;
        }

        public Builder<E> withLabeledValidator(ChangesValidator<E> validator, Label label) {
            this.validators.add(new Labeled<>(validator, label));
            return this;
        }

        public Builder<E> withLabeledValidators(Collection<ChangesValidator<E>> validators, Label label) {
            validators.forEach(validator -> this.validators.add(new Labeled<>(validator, label)));
            return this;
        }

        public Builder<E> withoutValidators() {
            this.validators.clear();
            this.flowConfigBuilders.forEach(Builder::withoutValidators);
            return this;
        }

        public Builder<E> withoutLabeledElements(Label label) {
            this.withoutLabeledElements(ImmutableList.of(label));
            return this;
        }

        public Builder<E> withoutLabeledElements(List<Label> labels) {
            if (!labels.isEmpty()) {
                this.validators.removeIf(validator -> labels.contains(validator.label()));
                this.postFetchCommandEnrichers.removeIf(enricher -> labels.contains(enricher.label()));
                this.flowConfigBuilders.forEach(builder -> builder.withoutLabeledElements(labels));
            }
            return this;
        }

        public Builder<E> withOutputGenerator(OutputGenerator<E> outputGenerator) {
            outputGenerators.add(outputGenerator);
            return this;
        }

        public Builder<E> withOutputGenerators(Collection<? extends OutputGenerator<E>> outputGenerators) {
            this.outputGenerators.addAll(outputGenerators);
            return this;
        }

        public Builder<E> withoutOutputGenerators() {
            this.outputGenerators.clear();
            this.flowConfigBuilders.forEach(Builder::withoutOutputGenerators);
            return this;
        }

        public Builder<E> withChildFlowBuilder(ChangeFlowConfig.Builder<? extends EntityType<?>> flowConfigBuilder) {
            this.flowConfigBuilders.add(flowConfigBuilder);
            return this;
        }

        /* not public */ void withRequiredRelationFields(Stream<EntityField<E, ?>> requiredRelationFields) {
            requiredRelationFields.collect(toCollection(() -> this.requiredRelationFields));
        }

        /* not public */ void withDeprecatedRequiredFields(Stream<EntityField<E, ?>> requiredFields) {
            requiredFields.collect(toCollection(() -> this.requiredFields));
        }

        /* not public */ void withRequiredFields(Stream<EntityField<E, ?>> requiredFields) {
            EntityChangeCompositeValidator<E> compositeValidator = new EntityChangeCompositeValidator<>();
            requiredFields.forEach(requiredField -> compositeValidator.register(new RequiredFieldValidatorImpl<>(requiredField, Errors.FIELD_IS_REQUIRED)));
            this.withValidator(compositeValidator);
        }

        /* not public */ void withImmutableFields(Stream<EntityField<E, ?>> immutableFields) {
            EntityChangeCompositeValidator<E> compositeValidator = new EntityChangeCompositeValidator<>();
            immutableFields.forEach(immutableField -> compositeValidator.register(new ImmutableFieldValidatorImpl<>(immutableField, Errors.FIELD_IS_IMMUTABLE)));
            this.withValidator(compositeValidator);
        }

        public Builder<E> withRetryer(PersistenceLayerRetryer retryer) {
            this.retryer = retryer;
            return this;
        }

        public ChangeFlowConfig<E> build() {
            ImmutableList.Builder<PostFetchCommandEnricher<E>> enrichers = ImmutableList.builder();
            postFetchCommandEnrichers.forEach(excludableElement -> enrichers.add(excludableElement.element()));
            ImmutableList.Builder<ChangesValidator<E>> validatorList = ImmutableList.builder();
            validators.forEach(validator -> validatorList.add(validator.element()));
            falseUpdatesPurger.ifPresent(enrichers::add);

            final Optional<AuditedFieldSet<E>> optionalAuditedFieldSet = auditedFieldsResolver.resolve(entityType);
            final AuditRequiredFieldsCalculator<E> auditRequiredFieldsCalculator =
                optionalAuditedFieldSet.map(AuditRequiredFieldsCalculator::new).orElse(null);
            final AuditRecordGenerator<E> auditRecordGenerator =
                optionalAuditedFieldSet.map(this::createAuditRecordGenerator).orElse(null);

            return new ChangeFlowConfig<>(entityType,
                                          enrichers.build(),
                                          validatorList.build(),
                                          ImmutableList.copyOf(outputGenerators),
                                          ImmutableSet.copyOf(requiredRelationFields),
                                          ImmutableSet.copyOf(requiredFields),
                                          flowConfigBuilders.stream().map(Builder::build).collect(toList()),
                                          retryer,
                                          auditRequiredFieldsCalculator,
                                          auditRecordGenerator,
                                          features
            );
        }

        private AuditRecordGenerator<E> createAuditRecordGenerator(final AuditedFieldSet<E> auditedFieldSet) {
            final AuditMandatoryFieldValuesGenerator mandatoryFieldValuesGenerator =
                new AuditMandatoryFieldValuesGenerator(auditedFieldSet.getMandatoryFields());

            final AuditFieldChangesGenerator<E> fieldChangesGenerator = new AuditFieldChangesGenerator<>(auditedFieldSet.getInternalFields());

            return new AuditRecordGeneratorImpl<>(mandatoryFieldValuesGenerator, fieldChangesGenerator);
        }

        static private class Labeled<Element> {

            private final Element element;
            private final Label label;

            Labeled(Element element, Label label) {
                this.element = element;
                this.label = label;
            }

            Element element() {
                return element;
            }

            Label label() {
                return label;
            }
        }
    }

}
