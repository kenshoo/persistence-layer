package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.internal.ChangesFilter;
import com.kenshoo.pl.entity.internal.Errors;
import com.kenshoo.pl.entity.internal.FalseUpdatesPurger;
import com.kenshoo.pl.entity.internal.MissingEntitiesFilter;
import com.kenshoo.pl.entity.internal.MissingParentEntitiesFilter;
import com.kenshoo.pl.entity.internal.RequiredFieldsChangesFilter;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import com.kenshoo.pl.entity.spi.CurrentStateConsumer;
import com.kenshoo.pl.entity.spi.OutputGenerator;
import com.kenshoo.pl.entity.spi.PersistenceLayerRetryer;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;
import com.kenshoo.pl.entity.spi.helpers.EntityChangeCompositeValidator;
import com.kenshoo.pl.entity.spi.helpers.ImmutableFieldValidatorImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.spi.PersistenceLayerRetryer.JUST_RUN_WITHOUT_CHECKING_DEADLOCKS;
import static java.util.stream.Collectors.toCollection;

public class ChangeFlowConfig<E extends EntityType<E>> {

    private final static Label NonExcludebale = new Label() {};

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

    private ChangeFlowConfig(E entityType,
                             List<PostFetchCommandEnricher<E>> postFetchCommandEnrichers,
                             List<ChangesValidator<E>> validators,
                             List<OutputGenerator<E>> outputGenerators,
                             Set<EntityField<E, ?>> requiredRelationFields,
                             Set<EntityField<E, ?>> requiredFields,
                             List<ChangeFlowConfig<? extends EntityType<?>>> childFlows,
                             PersistenceLayerRetryer retryer) {
        this.entityType = entityType;
        this.postFetchCommandEnrichers = postFetchCommandEnrichers;
        this.outputGenerators = outputGenerators;
        this.validators = validators;
        this.requiredRelationFields = requiredRelationFields;
        this.requiredFields = requiredFields;
        this.childFlows = childFlows;
        this.postFetchFilters = ImmutableList.of(new MissingParentEntitiesFilter<>(entityType.determineForeignKeys(requiredRelationFields)), new MissingEntitiesFilter<>(), new FieldsRequiredByChildrenFilter<>());
        this.postSupplyFilters = ImmutableList.of(new RequiredFieldsChangesFilter<>(requiredFields));
        this.retryer = retryer;
    }

    public E getEntityType() {
        return entityType;
    }

    public PersistenceLayerRetryer retryer() {
        return retryer;
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
        return Stream.concat(postFetchFilters.stream(), Stream.concat(postSupplyFilters.stream(), Stream.concat(Stream.concat(postFetchCommandEnrichers.stream(), validators.stream()), outputGenerators.stream())));
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

    public static class Builder<E extends EntityType<E>> {
        private final E entityType;
        private final List<Labeled<? extends PostFetchCommandEnricher<E>>> postFetchCommandEnrichers = new ArrayList<>();
        private final List<ChangesValidator<E>> validators = new ArrayList<>();
        private final List<OutputGenerator<E>> outputGenerators = new ArrayList<>();
        private final Set<EntityField<E, ?>> requiredRelationFields = new HashSet<>();
        private final Set<EntityField<E, ?>> requiredFields = new HashSet<>();
        private Optional<PostFetchCommandEnricher<E>> falseUpdatesPurger = Optional.empty();
        private final List<ChangeFlowConfig.Builder<? extends EntityType<?>>> flowConfigBuilders = new ArrayList<>();
        private PersistenceLayerRetryer retryer = JUST_RUN_WITHOUT_CHECKING_DEADLOCKS;


        public Builder(E entityType) {
            this.entityType = entityType;
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
            validators.add(validator);
            return this;
        }

        public Builder<E> withValidators(Collection<? extends ChangesValidator<E>> validators) {
            this.validators.addAll(validators);
            return this;
        }

        public Builder<E> withoutValidators() {
            this.validators.clear();
            this.flowConfigBuilders.forEach(Builder::withoutValidators);
            return this;
        }

        public Builder<E> withoutPostFetchCommandEnrichers(Label label){
            this.postFetchCommandEnrichers.removeIf(enricher -> enricher.lablel().equals(label));
            this.flowConfigBuilders.forEach(builder -> builder.withoutPostFetchCommandEnrichers(label));
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

        /* not public */ void withRequiredFields(Stream<EntityField<E, ?>> requiredFields) {
            requiredFields.collect(toCollection(() -> this.requiredFields));
        }

        /* not public */ void withImmutableFields(Stream<EntityField<E, ?>> immutableFields) {
            EntityChangeCompositeValidator<E> compositeValidator = new EntityChangeCompositeValidator<>();
            immutableFields.forEach(immutableField -> compositeValidator.register(entityType, new ImmutableFieldValidatorImpl<>(immutableField, Errors.FIELD_IS_IMMUTABLE)));
            this.validators.add(compositeValidator);
        }

        public Builder<E> withRetryer(PersistenceLayerRetryer retryer) {
            this.retryer = retryer;
            return this;
        }

        public ChangeFlowConfig<E> build() {
            ImmutableList.Builder<PostFetchCommandEnricher<E>> enrichers = ImmutableList.builder();
            postFetchCommandEnrichers.forEach(excludableElement -> enrichers.add(excludableElement.element()));
            falseUpdatesPurger.ifPresent(enrichers::add);
            return new ChangeFlowConfig<>(entityType,
                    enrichers.build(),
                    ImmutableList.copyOf(validators),
                    ImmutableList.copyOf(outputGenerators),
                    ImmutableSet.copyOf(requiredRelationFields),
                    ImmutableSet.copyOf(requiredFields),
                    flowConfigBuilders.stream().map(Builder::build).collect(Collectors.toList()),
                    retryer
            );
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

            Label lablel() {
                return label;
            }
        }
    }

}
