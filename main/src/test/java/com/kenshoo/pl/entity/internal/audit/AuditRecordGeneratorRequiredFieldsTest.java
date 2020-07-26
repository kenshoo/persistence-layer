package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.internal.EntityIdExtractor;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Set;

import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static com.kenshoo.pl.entity.audit.AuditTrigger.ON_CREATE_OR_UPDATE;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class AuditRecordGeneratorRequiredFieldsTest {

    @Mock
    private EntityIdExtractor entityIdExtractor;

    @Mock
    private AuditedFieldsToFetchResolver fieldsToFetchResolver;

    @Test
    public void requiredFields_ShouldReturnResultOfResolver() {
        final AuditedFieldSet<AuditedType> initialFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC2)
                           .build();

        final Set<? extends EntityField<AuditedType, ?>> intersectionFields = ImmutableSet.of(AuditedType.ID,
                                                                                              AuditedType.NAME);

        final Collection<? extends EntityField<AuditedType, ?>> fieldsToUpdate = ImmutableSet.of(AuditedType.ID,
                                                                                                 AuditedType.NAME,
                                                                                                 AuditedType.DESC);

        doReturn(intersectionFields.stream()).when(fieldsToFetchResolver).resolve(initialFieldSet, fieldsToUpdate);

        final Set<EntityField<?, ?>> actualRequiredFields =
            newAuditRecordGenerator(initialFieldSet).requiredFields(fieldsToUpdate, UPDATE)
                                                    .collect(toSet());

        assertThat(actualRequiredFields, is(intersectionFields));
    }

    private AuditRecordGenerator<AuditedType> newAuditRecordGenerator(final AuditedFieldSet<AuditedType> fieldSet) {
        return new AuditRecordGenerator<>(fieldSet, entityIdExtractor, fieldsToFetchResolver);
    }
}