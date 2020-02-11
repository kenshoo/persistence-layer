package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.junit.Test;

import java.util.Iterator;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EntityFieldDbAdapterTest {

    private EntityFieldDbAdapterForIdentity adapterForIdentity = new EntityFieldDbAdapterForIdentity();
    private EntityFieldDbAdapterForNonIdentity adapterForNonIdentity = new EntityFieldDbAdapterForNonIdentity();
    private EntityFieldDbAdapterWithoutFields adapterWithoutFields = new EntityFieldDbAdapterWithoutFields();

    @Test
    public void isIdentityFieldReturnsTrueWhenMatches() {
        assertThat(adapterForIdentity.isIdentityField(), is(true));
    }

    @Test
    public void isIdentityFieldReturnsTrueWhenDoesntMatch() {
        assertThat(adapterForNonIdentity.isIdentityField(), is(false));
    }

    @Test
    public void getFirstTableFieldWhenExistsShouldReturnCorrectField() {
        assertThat(adapterForIdentity.getFirstTableField(), is(TestEntityAutoIncTable.TABLE.id));
    }

    @Test
    public void getFirstDbValueWhenExistsShouldReturnCorrectValue() {
        assertThat(adapterForIdentity.getFirstDbValue(1), is(1));
    }

    @Test(expected = IllegalStateException.class)
    public void getFirstTableFieldWhenDoesntExistShouldThrowException() {
        adapterWithoutFields.getFirstTableField();
    }

    @Test(expected = IllegalStateException.class)
    public void getFirstDbValueWhenDoesntExistShouldThrowException() {
        adapterWithoutFields.getFirstDbValue(1);
    }

    private static class EntityFieldDbAdapterForIdentity implements EntityFieldDbAdapter<Object> {

        @Override
        public DataTable getTable() {
            return TestEntityAutoIncTable.TABLE;
        }

        @Override
        public Stream<TableField<Record, ?>> getTableFields() {
            return Stream.of(TestEntityAutoIncTable.TABLE.id);
        }

        @Override
        public Stream<Object> getDbValues(final Object value) {
            return Stream.of(value);
        }

        @Override
        public Object getFromRecord(final Iterator<Object> valuesIterator) {
            return valuesIterator.next();
        }
    }

    private static class EntityFieldDbAdapterForNonIdentity implements EntityFieldDbAdapter<Object> {

        @Override
        public DataTable getTable() {
            return TestEntityTable.TABLE;
        }

        @Override
        public Stream<TableField<Record, ?>> getTableFields() {
            return Stream.of(TestEntityTable.TABLE.id);
        }

        @Override
        public Stream<Object> getDbValues(final Object value) {
            return Stream.of(value);
        }

        @Override
        public Object getFromRecord(final Iterator<Object> valuesIterator) {
            return valuesIterator.next();
        }
    }

    private static class EntityFieldDbAdapterWithoutFields implements EntityFieldDbAdapter<Object> {

        @Override
        public DataTable getTable() {
            return TestEntityTable.TABLE;
        }

        @Override
        public Stream<TableField<Record, ?>> getTableFields() {
            return Stream.empty();
        }

        @Override
        public Stream<Object> getDbValues(final Object value) {
            return Stream.empty();
        }

        @Override
        public Object getFromRecord(final Iterator<Object> valuesIterator) {
            return null;
        }
    }
}