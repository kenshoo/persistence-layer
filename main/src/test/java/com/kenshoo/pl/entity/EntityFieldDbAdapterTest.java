package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.jooq.DataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.junit.Test;

import java.util.Iterator;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class EntityFieldDbAdapterTest {

    @Test
    public void isIdentityFieldReturnsTrueWhenMatches() {
        assertThat(new EntityFieldDbAdapterForIdentity().isIdentityField(), is(true));
    }

    @Test
    public void isIdentityFieldReturnsTrueWhenDoesntMatch() {
        assertThat(new EntityFieldDbAdapterForNonIdentity().isIdentityField(), is(false));
    }

    private static class EntityFieldDbAdapterForIdentity implements EntityFieldDbAdapter<Object> {

        @Override
        public DataTable getTable() {
            return TestEntityAutoIncTable.TABLE;
        }

        @Override
        public Stream<TableField<Record, ?>> getTableFields() {
            return ImmutableSet.<TableField<Record, ?>>of(TestEntityAutoIncTable.TABLE.id).stream();
        }

        @Override
        public Stream<Object> getDbValues(final Object value) {
            return null;
        }

        @Override
        public Object getFromRecord(final Iterator<Object> valuesIterator) {
            return null;
        }
    }

    private static class EntityFieldDbAdapterForNonIdentity implements EntityFieldDbAdapter<Object> {

        @Override
        public DataTable getTable() {
            return TestEntityTable.TABLE;
        }

        @Override
        public Stream<TableField<Record, ?>> getTableFields() {
            return ImmutableSet.<TableField<Record, ?>>of(TestEntityTable.TABLE.id).stream();
        }

        @Override
        public Stream<Object> getDbValues(final Object value) {
            return null;
        }

        @Override
        public Object getFromRecord(final Iterator<Object> valuesIterator) {
            return null;
        }
    }
}