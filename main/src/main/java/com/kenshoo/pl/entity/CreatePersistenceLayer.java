package com.kenshoo.pl.entity;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;

@Service
public class CreatePersistenceLayer<E extends EntityType<E>, ID extends Identifier<E>> {

    @Resource
    private PersistenceLayer<E> persistenceLayer;

    public CreateResult<E, ID> makeChanges(Collection<? extends CreateEntityCommand<E>> commands, ChangeFlowConfig<E> flowConfig, UniqueKey<E> primaryKey) {
        ChangeContext changeContext = new ChangeContext();
        persistenceLayer.makeChanges(commands, changeContext, flowConfig);
        Collection<EntityCreateResult<E, ID>> results = new ArrayList<>(commands.size());
        for (CreateEntityCommand<E> command : commands) {
            Collection<ValidationError> commandErrors = changeContext.getValidationErrors(command);
            if (commandErrors.isEmpty()) {
                //noinspection unchecked
                ID identifier = (ID) primaryKey.createValue(command);
                command.setIdentifier(identifier);
                results.add(new EntityCreateResult<>(command));
            } else {
                results.add(new EntityCreateResult<>(command, commandErrors));
            }
        }
        return new CreateResult<>(results, changeContext.getStats());
    }
}
