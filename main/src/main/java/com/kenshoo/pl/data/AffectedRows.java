package com.kenshoo.pl.data;

import org.jooq.lambda.Seq;

import java.util.List;

import static java.util.Collections.emptyList;

public class AffectedRows {

    private static final AffectedRows EMPTY = new AffectedRows(0, 0, 0, emptyList());

    private final int inserted;
    private final int updated;
    private final int deleted;

    public List<Long> getGeneratedIds() {
        return generatedIds;
    }

    private final List<Long> generatedIds;

    private AffectedRows(int inserted, int updated, int deleted, List<Long> generatedIds) {
        this.inserted = inserted;
        this.updated = updated;
        this.deleted = deleted;
        this.generatedIds = generatedIds;
    }

    public int getInserted() {
        return inserted;
    }

    public int getUpdated() {
        return updated;
    }

    public int getDeleted() {
        return deleted;
    }

    public AffectedRows plus(AffectedRows another) {
        return new AffectedRows(inserted + another.inserted, updated + another.updated, deleted + another.deleted, append(generatedIds, another.generatedIds));
    }

    public AffectedRows withGeneratedIds(List<Long> generatedIds) {
        return this.plus(new AffectedRows(0, 0, 0, generatedIds));
    }

    private static <T> List<T> append(List<T> list1, List<T> list2) {
        if (list1 == null || list1.isEmpty()) {
            return list2;
        }
        if (list2 == null || list2.isEmpty()) {
            return list1;
        }
        return Seq.seq(list1).append(list2).toList();
    }

    public static AffectedRows empty() {
        return EMPTY;
    }

    public static AffectedRows updated(int updated) {
        return new AffectedRows(0, updated, 0, emptyList());
    }

    public static AffectedRows deleted(int deleted) {
        return new AffectedRows(0, 0, deleted, emptyList());
    }

    public static AffectedRows insertedAndUpdated(int inserted, int updated) {
        return new AffectedRows(inserted, updated, 0, emptyList());
    }
}
