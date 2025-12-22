package me.hsgamer.bettereconomy.database;

import me.hsgamer.hscore.database.client.sql.SqlClient;

import java.util.Collections;
import java.util.List;

public class MySqlDataStorageSupplier extends SqlDataStorageSupplier {

    public MySqlDataStorageSupplier(SqlClient<?> client) {
        super(client);
    }

    @Override
    protected String getIncrementalKeyDefinition() {
        return "INTEGER PRIMARY KEY AUTO_INCREMENT";
    }

    @Override
    protected List<String> toSaveStatement(String name, String[] keyColumns, String[] valueColumns) {
        StringBuilder statement = new StringBuilder("INSERT INTO `")
                .append(name)
                .append("` (");
        for (int i = 0; i < keyColumns.length + valueColumns.length; i++) {
            statement.append("`")
                    .append(i < keyColumns.length ? keyColumns[i] : valueColumns[i - keyColumns.length])
                    .append("`");
            if (i != keyColumns.length + valueColumns.length - 1) {
                statement.append(", ");
            }
        }
        statement.append(") VALUES (");
        for (int i = 0; i < keyColumns.length + valueColumns.length; i++) {
            statement.append("?");
            if (i != keyColumns.length + valueColumns.length - 1) {
                statement.append(", ");
            }
        }
        statement.append(") ON DUPLICATE KEY UPDATE ");
        for (int i = 0; i < valueColumns.length; i++) {
            statement.append("`")
                    .append(valueColumns[i])
                    .append("` = VALUES(`")
                    .append(valueColumns[i])
                    .append("`)");
            if (i != valueColumns.length - 1) {
                statement.append(", ");
            }
        }
        statement.append(";");
        return Collections.singletonList(statement.toString());
    }

    @Override
    protected List<Object[]> toSaveValues(Object[] keys, Object[] values) {
        Object[] queryValues = new Object[keys.length + values.length];
        System.arraycopy(keys, 0, queryValues, 0, keys.length);
        System.arraycopy(values, 0, queryValues, keys.length, values.length);
        return Collections.singletonList(queryValues);
    }
}
