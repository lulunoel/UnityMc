package org.lulunoel2016.unityMC;

import org.bukkit.plugin.java.JavaPlugin;
import org.lulunoel2016.unityMC.commands.ModuleCommand;
import org.lulunoel2016.unityMC.database.DatabaseManager;
import org.lulunoel2016.unityMC.utils.ModuleManager;
import org.lulunoel2016.unityMC.modules.ExampleModule;

import java.util.Objects;

public final class UnityMC extends JavaPlugin {

    private ModuleManager moduleManager;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        // Charger la configuration
        saveDefaultConfig();

        // Initialiser la base de données
        String host = Objects.requireNonNull(getConfig().getString("database.host", "127.0.0.1"), "Host non défini dans config.yml");
        int port = getConfig().getInt("database.port", 3306); // Valeur par défaut pour éviter les erreurs
        String name = Objects.requireNonNull(getConfig().getString("database.name", "minecraft"), "Nom de la base non défini dans config.yml");
        String username = Objects.requireNonNull(getConfig().getString("database.username", "root"), "Nom d'utilisateur non défini dans config.yml");
        String password = Objects.requireNonNull(getConfig().getString("database.password", "password"), "Mot de passe non défini dans config.yml");

        try {
            this.databaseManager = new DatabaseManager( host, port, name, username, password, getLogger());
        } catch (Exception e) {
            getLogger().severe("Impossible de se connecter à la base de données. Vérifiez les paramètres de configuration.");
            getPluginLoader().disablePlugin(this);
            return;
        }

        // Initialiser le gestionnaire de modules
        this.moduleManager = new ModuleManager(this);

        // Charger les modules
        moduleManager.loadModules();

        // Enregistrer la commande
        this.getCommand("module").setExecutor(new ModuleCommand(this));

        getLogger().info("UnityMC a démarré avec succès!");
    }


    @Override
    public void onDisable() {
        if (moduleManager != null) {
            moduleManager.unloadModules();
            getLogger().info("Modules déchargés avec succès.");
        }
        if (databaseManager != null) {
            databaseManager.close();
            getLogger().info("Connexion à la base de données fermée.");
        }
        getLogger().info("UnityMC s'est arrêté proprement.");
    }


    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }
}
