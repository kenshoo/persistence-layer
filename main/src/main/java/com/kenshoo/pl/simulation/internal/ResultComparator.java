package com.kenshoo.pl.simulation.internal;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.simulation.ComparisonMismatch;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;


public class ResultComparator<E extends EntityType<E>, ID extends Identifier<E>> {

    private final Collection<EntityField<E, ?>> inspectedFields;

    public ResultComparator(Collection<EntityField<E, ?>> inspectedFields) {
        this.inspectedFields = inspectedFields;
    }

    public List<ComparisonMismatch<E, ID>> findMismatches(
            Iterable<SimulatedResult<E, ID>> simulatedResults,
            Iterable<ActualResult> actualDbResults) {

        return seq(simulatedResults).zip(actualDbResults)
                .map(pair -> findMismatch(pair.v1, pair.v2))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private Optional<ComparisonMismatch<E, ID>> findMismatch(SimulatedResult<E, ID> simulatedResult, ActualResult actualResult) {

        if (simulatedResult.isError() && actualResult.isError()) {
            return Optional.empty();
        }

        if (simulatedResult.isSuccess() && actualResult.isError()) {
            return Optional.of(new ComparisonMismatch<>(simulatedResult.getId(), "Simulated mutation was successful but real mutation finished with the following error: " + actualResult.getErrorDescription()));
        }

        if (simulatedResult.isError() && actualResult.isSuccess()) {
            return Optional.of(new ComparisonMismatch<>(simulatedResult.getId(), "Real mutation was successful but simulated mutation finished with the following errors: " + simulatedResult.getErrors()));
        }

        final var mismatchingFields = inspectedFields.stream()
                .map(field -> getFieldMismatch(field, simulatedResult.getCommand(), actualResult))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        return mismatchingFields.isEmpty()
                ? Optional.empty()
                : Optional.of(new ComparisonMismatch<>(simulatedResult.getId(), "Found field mismatch: " + mismatchingFields));
    }

    private Optional<String> getFieldMismatch(EntityField<E, ?> field, EntityChange<E> simulated, ActualResult actualResult) {

        if (!simulated.isFieldChanged(field) && !actualResult.isReallyChanged(field)) {
            return Optional.empty();
        }

        if (!simulated.isFieldChanged(field) && actualResult.isReallyChanged(field)) {
            return Optional.of("Field \"" + field + "\" is not populated in the simulated command although it was changed in DB");
        }

        return Objects.equals(simulated.get(field), actualResult.getFinalValue(field))
                ? Optional.empty()
                : Optional.of("Field \"" + field + "\" has mismatch values. Simulated: \"" + simulated.get(field) + "\"" + ", Actual: \"" + actualResult.getFinalValue(field) + "\"");
    }


}
