package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.internal.*;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import com.kenshoo.pl.entity.spi.CurrentStateConsumer;
import com.kenshoo.pl.entity.spi.OutputGenerator;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;
import com.kenshoo.pl.entity.spi.helpers.EntityChangeCompositeValidator;
import com.kenshoo.pl.entity.spi.helpers.ImmutableFieldValidatorImpl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class ChangeFlowConfig<E extends EntityType<E>> {

    private final E entityType;
    private final List<PostFetchCommandEnricher<E>> postFetchCommandEnrichers;
    private final List<OutputGenerator<E>> outputGenerators;
    private final List<ChangesValidator<E>> validators;
    private final Set<EntityField<E, ?>> requiredRelationFields;
    private final Set<EntityField<E, ?>> requiredFields;
    private final List<ChangeFlowConfig<? extends EntityType<?>>> childFlows;
    private final List<ChangesFilter<E>> postFetchFilters;
    private final List<ChangesFilter<E>> postSupplyFilters;

    private ChangeFlowConfig(E entityType,
                             List<PostFetchCommandEnricher<E>> postFetchCommandEnrichers,
                             List<ChangesValidator<E>> validators,
                             List<OutputGenerator<E>> outputGenerators,
                             Set<EntityField<E, ?>> requiredRelationFields,
                             Set<EntityField<E, ?>> requiredFields,
                             List<ChangeFlowConfig<? extends EntityType<?>>> childFlows) {
        this.entityType = entityType;
        this.postFetchCommandEnrichers = postFetchCommandEnrichers;
        this.outputGenerators = outputGenerators;
        this.validators = validators;
        this.requiredRelationFields = requiredRelationFields;
        this.requiredFields = requiredFields;
        this.childFlows = childFlows;
        this.postFetchFilters = ImmutableList.of(new MissingParentEntitiesFilter<>(entityType.determineForeignKeys(requiredRelationFields)), new MissingEntitiesFilter<>(), new FieldsRequiredByChildrenFilter<>());
        this.postSupplyFilters = ImmutableList.of(new RequiredFieldsChangesFilter<>(requiredFields));
    }

    public E getEntityType() {
        return entityType;
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

    public static class Builder<E extends EntityType<E>> {
        private final E entityType;
        private final List<PostFetchCommandEnricher<E>> postFetchCommandEnrichers = new ArrayList<>();
        private final List<ChangesValidator<E>> validators = new ArrayList<>();
        private final List<OutputGenerator<E>> outputGenerators = new ArrayList<>();
        private final Set<EntityField<E, ?>> requiredRelationFields = new HashSet<>();
        private final Set<EntityField<E, ?>> requiredFields = new HashSet<>();
        private Optional<PostFetchCommandEnricher<E>> falseUpdatesPurger = Optional.empty();
        private final List<Builder<? extends EntityType<?>>> flowConfigBuilders = new ArrayList<>();


        public Builder(E entityType) {
            this.entityType = entityType;
        }

        public Builder<E> withPostFetchCommandEnricher(PostFetchCommandEnricher<E> enricher) {
            postFetchCommandEnrichers.add(enricher);
            return this;
        }

        public Builder<E> withPostFetchCommandEnrichers(Collection<? extends PostFetchCommandEnricher<E>> enrichers) {
            postFetchCommandEnrichers.addAll(enrichers);
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


        public ChangeFlowConfig<E> build() {
            ImmutableList.Builder<PostFetchCommandEnricher<E>> enrichers = ImmutableList.<PostFetchCommandEnricher<E>>builder().addAll(postFetchCommandEnrichers);
            falseUpdatesPurger.ifPresent(enrichers::add);
            return new ChangeFlowConfig<>(entityType,
                    enrichers.build(),
                    ImmutableList.copyOf(validators),
                    ImmutableList.copyOf(outputGenerators),
                    ImmutableSet.copyOf(requiredRelationFields),
                    ImmutableSet.copyOf(requiredFields),
                    flowConfigBuilders.stream().map(Builder::build).collect(Collectors.toList()));
        }
    }

}
