package com.kenshoo.pl.entity;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;

import static org.jooq.lambda.Seq.seq;


@Service
public class InsertOnDuplicateUpdatePersistenceLayer<E extends EntityType<E>> {

    @Resource
    private PersistenceLayer<E> persistenceLayer;

    public <ID extends Identifier<E>> InsertOnDuplicateUpdateResult<E, ID> makeChanges(Collection<? extends InsertOnDuplicateUpdateCommand<E, ID>> commands, ChangeFlowConfig<E> flowConfig) {
        ChangeContext changeContext = new ChangeContext();
        persistenceLayer.makeChanges(commands, changeContext, flowConfig);
        return new InsertOnDuplicateUpdateResult<>(seq(commands).map(cmd -> toResult(changeContext, cmd)).toList(), changeContext.getStats());
    }

    private <ID extends Identifier<E>> EntityInsertOnDuplicateUpdateResult<E, ID> toResult(ChangeContext changeContext, InsertOnDuplicateUpdateCommand<E, ID> command) {
        Collection<ValidationError> commandErrors = changeContext.getValidationErrors(command);
        if (commandErrors.isEmpty()) {
            return new EntityInsertOnDuplicateUpdateResult<>(command);
        } else {
            return new EntityInsertOnDuplicateUpdateResult<>(command, commandErrors);
        }
    }
}
