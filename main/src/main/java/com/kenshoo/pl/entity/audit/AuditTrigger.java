package com.kenshoo.pl.entity.audit;

/**
 * Indicates the rule by which to trigger the auditing of a field, following execution of a command.
 * @see com.kenshoo.pl.entity.annotation.audit.Audited
 * @see AuditRecord
 */
public enum AuditTrigger {

    /**
     * Indicates that a field should be audited always, regardless of whether its value has changed - whenever there is some other change in the  currentState.<br>
     * This means that the <b>current</b> value of the field will always be included in {@link AuditRecord#getMandatoryFieldValues()}.
     */
    ALWAYS,
    /**
     * Indicates that a field should be audited only if its value has changed.<br>
     * This means that whenever the value changes, the old and new values will be included in {@link AuditRecord#getFieldRecords()}.
     */
    ON_CHANGE
}
