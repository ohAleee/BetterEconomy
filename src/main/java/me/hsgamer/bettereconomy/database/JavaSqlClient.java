package me.hsgamer.bettereconomy.database;

import it.metamc.metaverse.api.Metaverse;
import me.hsgamer.hscore.database.Setting;
import me.hsgamer.hscore.database.client.sql.SqlClient;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class JavaSqlClient implements SqlClient<Properties> {

    private final DataSource dataSource;

    public JavaSqlClient() {
        this.dataSource = Metaverse.get().getDatabaseProvider().getConnection();
    }

    @Override
    public Setting getSetting() {
        return null;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public Properties getOriginal() {
        return null;
    }
}
