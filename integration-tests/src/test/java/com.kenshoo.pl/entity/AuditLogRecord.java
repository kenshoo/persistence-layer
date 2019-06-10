package com.kenshoo.pl.entity;

public class AuditLogRecord {

    private final String entity;
    private final long id;
    private final String operation;
    private final String field;
    private final String oldValue;
    private final String newValue;

    public AuditLogRecord(String entity, long id, String operation, String field, String oldValue, String newValue) {
        this.entity = entity;
        this.id = id;
        this.operation = operation;
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuditLogRecord that = (AuditLogRecord) o;

        if (id != that.id) return false;
        if (!entity.equals(that.entity)) return false;
        if (!operation.equals(that.operation)) return false;
        if (!field.equals(that.field)) return false;
        if (oldValue != null ? !oldValue.equals(that.oldValue) : that.oldValue != null) return false;
        return !(newValue != null ? !newValue.equals(that.newValue) : that.newValue != null);

    }

    @Override
    public int hashCode() {
        int result = entity.hashCode();
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + operation.hashCode();
        result = 31 * result + field.hashCode();
        result = 31 * result + (oldValue != null ? oldValue.hashCode() : 0);
        result = 31 * result + (newValue != null ? newValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AuditLogRecord{" +
                "entity='" + entity + '\'' +
                ", id=" + id +
                ", operation=" + operation +
                ", field='" + field + '\'' +
                ", oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
                '}';
    }
}
