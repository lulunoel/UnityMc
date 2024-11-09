package org.lulunoel2016.unityMC.modules;

import org.lulunoel2016.unityMC.UnityMC;
import org.lulunoel2016.unityMC.utils.IModule;

public class ExampleModule implements IModule {

    private UnityMC plugin;
    private boolean enabled = false;

    @Override
    public void onEnable(UnityMC plugin) {
        this.plugin = plugin;
        if (enabled) return; // Empêche l'activation si le module est déjà activé
        enabled = true;

        // Vérifier et créer la table si nécessaire
        setupDatabase();
        insertExampleData();
    }

    public void setupDatabase() {
        // Requête SQL pour créer la table avec des accents graves autour du nom de la colonne
        String createTableQuery = "CREATE TABLE IF NOT EXISTS example_table (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "`column` VARCHAR(255) NOT NULL" +
                ");";
        plugin.getDatabaseManager().executeUpdate(createTableQuery);
        plugin.getLogger().info("Table example_table vérifiée/créée.");
    }

    // Utilisez également les accents graves dans l'insertion
    public void insertExampleData() {
        String insertQuery = "INSERT INTO example_table (`column`) VALUES ('value');";
        plugin.getDatabaseManager().executeUpdate(insertQuery);
    }


    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void onDisable() {
        if (!enabled) return; // Empêche la désactivation si le module est déjà désactivé
        enabled = false;
    }

}
