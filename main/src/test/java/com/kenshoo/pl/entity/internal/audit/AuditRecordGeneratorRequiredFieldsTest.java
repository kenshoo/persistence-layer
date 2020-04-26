package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.internal.EntityIdExtractor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Set;

import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static com.kenshoo.pl.matchers.IterableStreamMatcher.eqStreamAsSet;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditRecordGeneratorRequiredFieldsTest {

    @Mock
    private AuditedFieldSet<TestAuditedEntityType> completeFieldSet;

    @Mock
    private EntityIdExtractor entityIdExtractor;

    @InjectMocks
    private AuditRecordGenerator<TestAuditedEntityType> auditRecordGenerator;

    @Test
    public void requiredFields_ShouldReturnResultOfIntersection() {
        final Collection<? extends EntityField<TestAuditedEntityType, ?>> fieldsToUpdate = ImmutableSet.of(TestAuditedEntityType.ID,
                                                                                                           TestAuditedEntityType.NAME,
                                                                                                           TestAuditedEntityType.DESC);

        final AuditedFieldSet<TestAuditedEntityType> expectedIntersectionFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME, TestAuditedEntityType.DESC));

        when(completeFieldSet.intersectWith(eqStreamAsSet(fieldsToUpdate))).thenReturn(expectedIntersectionFieldSet);

        final Set<EntityField<?, ?>> actualRequiredFields =
            auditRecordGenerator.requiredFields(fieldsToUpdate, UPDATE)
                                .collect(toSet());

        assertThat(actualRequiredFields, is(expectedIntersectionFieldSet.getAllFields().collect(toSet())));
    }
}