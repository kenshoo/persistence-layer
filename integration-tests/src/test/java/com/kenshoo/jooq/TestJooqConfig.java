package com.kenshoo.jooq;

import com.kenshoo.pl.BetaTesting;
import com.mysql.jdbc.ConnectionImpl;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.lambda.Seq;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import static java.lang.Integer.parseInt;

public class TestJooqConfig {

    static {
        Seq.of(BetaTesting.Feature.values()).forEach(BetaTesting::enable);
    }

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
        return new ConnectionImpl(props.getProperty("server"), parseInt(props.getProperty("port")), props, props.getProperty("database"), null);
    }

    private static Properties readProperties(String resourceName) throws IOException {
        Properties props = new Properties();
        props.load(TestJooqConfig.class.getResourceAsStream(resourceName));
        return props;
    }
}
