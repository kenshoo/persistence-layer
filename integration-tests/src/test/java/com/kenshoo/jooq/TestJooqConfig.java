package com.kenshoo.jooq;

import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.jdbc.ConnectionImpl;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.ThreadLocalTransactionProvider;
import org.jooq.lambda.Unchecked;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import static java.lang.Integer.parseInt;

public class TestJooqConfig {

    private static ClosableConnectionProvider previousConnectionProvider;

    public static DSLContext create() {
        return create(alwaysAllocatingNewConnections());
    }

    public static DSLContext create(ClosableConnectionProvider connectionProvider) {
        closePreviousConnectionProvider();
        previousConnectionProvider = connectionProvider;
        DefaultConfiguration conf = new DefaultConfiguration();
        conf.setSQLDialect(SQLDialect.MYSQL);
        // Don't need to do this: conf.setConnectionProvider(connectionProvider);
        conf.setTransactionProvider(new ThreadLocalTransactionProvider(connectionProvider));
        return DSL.using(conf);
    }

    public static AlwaysAllocateNewConnection alwaysAllocatingNewConnections() {
        Properties props = readProperties("/database.properties");
        return new AlwaysAllocateNewConnection(() -> connection(props));
    }

    private static void closePreviousConnectionProvider() {
        if (previousConnectionProvider != null) {
            Unchecked.runnable(previousConnectionProvider::close).run();
        }
    }

    private static ConnectionImpl connection(Properties props) {
        try {
            String host = props.getProperty("server");
            int port = parseInt(props.getProperty("port"));
            String user = props.getProperty("user");
            String password = props.getProperty("password");
            Map<String, String> properties = Map.of(PropertyKey.DBNAME.getKeyName(), props.getProperty("database"));
            HostInfo hostInfo = new HostInfo(null, host, port, user, password, properties);
            return new ConnectionImpl(hostInfo);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Properties readProperties(String resourceName) {
        try {
            Properties props = new Properties();
            props.load(TestJooqConfig.class.getResourceAsStream(resourceName));
            return props;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
