package com.kenshoo.jooq;

import com.mysql.jdbc.ConnectionImpl;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class TestJooqConfig {

    public static DSLContext create() {
        try {
            DefaultConfiguration conf = new DefaultConfiguration();
            conf.setConnection(connection(readProperties("/database.properties")));
            conf.setSQLDialect(SQLDialect.MYSQL);
            return DSL.using(conf);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ConnectionImpl connection(Properties props) throws SQLException {
        return new ConnectionImpl(props.getProperty("server"), 3306, props, props.getProperty("database"), null);
    }

    private static Properties readProperties(String resourceName) throws IOException {
        Properties props = new Properties();
        props.load(TestJooqConfig.class.getResourceAsStream(resourceName));
        return props;
    }
}
