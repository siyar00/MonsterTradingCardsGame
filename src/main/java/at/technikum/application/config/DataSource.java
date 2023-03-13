package at.technikum.application.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DataSource implements DbConnector {

    private static HikariConfig config;
    private static HikariDataSource ds;

    static {
        config.setJdbcUrl( "jdbc_url" );
        config.setUsername( "database_username" );
        config.setPassword( "database_password" );
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        ds = new HikariDataSource( config );
    }
    private DataSource() {
        config = new HikariConfig("src/main/resources/hikari.properties");
        ds = new HikariDataSource(config);
    }
    private static DataSource dataSource;

    public static DataSource getInstance() {
        if (dataSource == null) {
            dataSource = new DataSource();
        }
        return dataSource;
    }

    public Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("Database not available!", e);
        }
    }

}
