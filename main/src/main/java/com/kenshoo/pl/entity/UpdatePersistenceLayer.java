package com.kenshoo.pl.entity;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;

@Service
public class UpdatePersistenceLayer<E extends EntityType<E>> {

    @Resource
    private PersistenceLayer<E> persistenceLayer;

    public <ID extends Identifier<E>> UpdateResult<E, ID> makeChanges(Collection<? extends UpdateEntityCommand<E, ID>> commands, ChangeFlowConfig<E> flowConfig) {
        ChangeContext changeContext = new ChangeContext();
        persistenceLayer.makeChanges(commands, changeContext, flowConfig);
        Collection<EntityUpdateResult<E, ID>> results = new ArrayList<>(commands.size());
        for (UpdateEntityCommand<E, ID> command : commands) {
            Collection<ValidationError> commandErrors = changeContext.getValidationErrors(command);
            if (commandErrors.isEmpty()) {
                results.add(new EntityUpdateResult<>(command));
            } else {
                results.add(new EntityUpdateResult<>(command, commandErrors));
            }
        }
        return new UpdateResult<>(results, changeContext.getStats());
    }
}
