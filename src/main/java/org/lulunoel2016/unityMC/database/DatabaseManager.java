package org.lulunoel2016.unityMC.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {

    private final HikariDataSource dataSource;
    private final Logger logger;

    public DatabaseManager(String host, int port, String database, String username, String password, Logger logger) throws SQLException {
        this.logger = logger;
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false");
            config.setUsername(username);
            config.setPassword(password);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.setInitializationFailTimeout(0);

            this.dataSource = new HikariDataSource(config);

            // Tester la connexion pour valider la configuration
            try (Connection conn = dataSource.getConnection()) {
                if (conn == null || conn.isClosed()) {
                    throw new SQLException("Échec de la connexion initiale à la base de données.");
                }
            }
        } catch (SQLException e) {
            logger.severe("Erreur lors de la configuration de la base de données : " + e.getMessage());
            close(); // Fermer Hikari si une erreur survient
            throw e; // Relancer l'exception pour désactiver le plugin
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public void executeUpdate(String query) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de l'exécution de la mise à jour SQL : " + query, e);
        }
    }

    public ResultSet executeQuery(String query) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            return stmt.executeQuery();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de l'exécution de la requête SQL : " + query, e);
            return null;
        }
    }
}
