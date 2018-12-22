package com.kenshoo.pl.entity;


import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;

@Service
public class DeletePersistenceLayer<E extends EntityType<E>> {

    @Resource
    private PersistenceLayer<E> persistenceLayer;

    public <ID extends Identifier<E>> DeleteResult<E, ID> makeChanges(Collection<? extends DeleteEntityCommand<E, ID>> commands, ChangeFlowConfig<E> flowConfig) {
        ChangeContext changeContext = new ChangeContext();
        persistenceLayer.makeChanges(commands, changeContext, flowConfig);
        Collection<EntityDeleteResult<E, ID>> results = new ArrayList<>(commands.size());
        for (DeleteEntityCommand<E, ID> command : commands) {
            Collection<ValidationError> commandErrors = changeContext.getValidationErrors(command);
            if (commandErrors.isEmpty()) {
                results.add(new EntityDeleteResult<>(command));
            } else {
                results.add(new EntityDeleteResult<>(command, commandErrors));
            }
        }
        return new DeleteResult<>(results, changeContext.getStats());
    }
}
