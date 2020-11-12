package com.kenshoo.jooq;

import org.jooq.exception.DataAccessException;
import org.jooq.lambda.Unchecked;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;


public class AlwaysAllocateNewConnection implements ClosableConnectionProvider {

    private final List<Connection> allocatedConnections = new LinkedList<>();
    private final Supplier<Connection> connectionFactory;

    public AlwaysAllocateNewConnection(Supplier<Connection> connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void close() throws Exception {
        allocatedConnections.forEach(c -> Unchecked.runnable(c::close).run());
    }

    @Override
    public Connection acquire() throws DataAccessException {
        Connection newConnection = connectionFactory.get();
        allocatedConnections.add(newConnection);
        return newConnection;
    }

    @Override
    public void release(Connection connection) throws DataAccessException {
        Unchecked.runnable(connection::close).run();
    }
}
