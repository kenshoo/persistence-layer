package com.kenshoo.pl.data;

public class AffectedRows {

    private static final AffectedRows EMPTY = new AffectedRows(0, 0, 0);

    private final int inserted;
    private final int updated;
    private final int deleted;

    private AffectedRows(int inserted, int updated, int deleted) {
        this.inserted = inserted;
        this.updated = updated;
        this.deleted = deleted;
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
        return new AffectedRows(inserted + another.inserted, updated + another.updated, deleted + another.deleted);
    }

    public static AffectedRows empty() {
        return EMPTY;
    }

    public static AffectedRows updated(int updated) {
        return new AffectedRows(0, updated, 0);
    }

    public static AffectedRows deleted(int deleted) {
        return new AffectedRows(0, 0, deleted);
    }

    public static AffectedRows insertedAndUpdated(int inserted, int updated) {
        return new AffectedRows(inserted, updated, 0);
    }
}
