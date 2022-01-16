package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithFieldLevelOnlyValueFormatterType;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class UpdateAuditedWithFieldLevelOnlyValueFormatterCommand
    extends UpdateEntityCommand<AuditedWithFieldLevelOnlyValueFormatterType, Identifier<AuditedWithFieldLevelOnlyValueFormatterType>>
    implements EntityCommandExt<AuditedWithFieldLevelOnlyValueFormatterType, UpdateAuditedWithFieldLevelOnlyValueFormatterCommand> {

    public UpdateAuditedWithFieldLevelOnlyValueFormatterCommand(final long id) {
        super(AuditedWithFieldLevelOnlyValueFormatterType.INSTANCE, uniqueKey(AuditedWithFieldLevelOnlyValueFormatterType.ID).createIdentifier(id));
    }
}
