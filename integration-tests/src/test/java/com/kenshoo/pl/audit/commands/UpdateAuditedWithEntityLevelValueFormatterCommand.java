package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithEntityLevelValueFormatterType;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class UpdateAuditedWithEntityLevelValueFormatterCommand
    extends UpdateEntityCommand<AuditedWithEntityLevelValueFormatterType, Identifier<AuditedWithEntityLevelValueFormatterType>>
    implements EntityCommandExt<AuditedWithEntityLevelValueFormatterType, UpdateAuditedWithEntityLevelValueFormatterCommand> {

    public UpdateAuditedWithEntityLevelValueFormatterCommand(final long id) {
        super(AuditedWithEntityLevelValueFormatterType.INSTANCE, uniqueKey(AuditedWithEntityLevelValueFormatterType.ID).createIdentifier(id));
    }
}
