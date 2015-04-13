package io.ll.warden.storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Storage {

    public abstract Connection getConnection() throws SQLException;

    public abstract StorageType getStorageType();

    public boolean checkForWorkingConnection() {
        Connection connection = null;

        try {
            connection = this.getConnection();
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.cleanConnections(connection);
        }

        return false;
    }

    public List<Map<String, Object>> doQuery(String query, String... labels) {
        List<Map<String, Object>> queryResults = new ArrayList<Map<String, Object>>();

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = this.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            if (resultSet.isBeforeFirst()) {
                while (resultSet.next()) {
                    Map<String, Object> setResult = new HashMap<String, Object>();

                    for (String label : labels) {
                        setResult.put(label, resultSet.getObject(label));
                    }

                    queryResults.add(setResult);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.cleanConnections(connection, statement, resultSet);
        }

        return queryResults;
    }

    private void cleanConnections(Connection c, Statement s, ResultSet r) {
        try {
            if (s != null) s.close();
            if (r != null) r.close();
            if (c != null) c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cleanConnections(Connection c) {
        this.cleanConnections(c, null, null);
    }
}
