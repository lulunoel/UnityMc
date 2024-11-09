package org.lulunoel2016.unityMC;

import org.bukkit.plugin.java.JavaPlugin;
import org.lulunoel2016.unityMC.modules.ModuleManager;

public final class UnityMC extends JavaPlugin {

    private ModuleManager moduleManager;

    @Override
    public void onEnable() {
        // Initialisation du gestionnaire de modules
        this.moduleManager = new ModuleManager(this);
        
        // Charger les modules
        moduleManager.loadModules();
        
        getLogger().info("UnityMC a démarré avec succès!");
    }

    @Override
    public void onDisable() {
        // Décharger les modules
        moduleManager.unloadModules();
        
        getLogger().info("UnityMC s'est arrêté proprement.");
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }
}
