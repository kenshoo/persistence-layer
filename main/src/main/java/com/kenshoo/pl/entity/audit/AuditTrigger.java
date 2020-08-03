package com.kenshoo.pl.entity.audit;

/**
 * Indicates the rule by which to trigger the auditing of a field, following execution of a command.
 * @see com.kenshoo.pl.entity.annotation.audit.Audited
 * @see AuditRecord
 */
public enum AuditTrigger {

    /**
     * Indicates that a field should be audited always, regardless of whether its value has changed - whenever there is some change in the entity.<br>
     * This means that the <b>current</b> value of the field will always be included in {@link AuditRecord#getMandatoryFieldValues()}.<br>
     * In addition it will also be included in {@link AuditRecord#getFieldRecords()} according to the rule of {@link #ON_CREATE_OR_UPDATE} (see below).
     */
    ALWAYS,

    /**
     * Indicates that a field should be audited only if it is either being created, or its value has been changed by an update.<br>
     * This means that:<br>
     * Upon create, the new value will be included in {@link AuditRecord#getFieldRecords()}.<br>
     * Upon update if the value has changed - the old and new values will be included in {@link AuditRecord#getFieldRecords()}.
     */
    ON_CREATE_OR_UPDATE,

    /**
     * Indicates that a field should be audited only if its value has been changed by an update.<br>
     * This means that upon update if the value has changed - the old and new values will be included in {@link AuditRecord#getFieldRecords()}.
     */
    ON_UPDATE
}
