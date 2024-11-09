package org.lulunoel2016.unityMC.utils;

import org.lulunoel2016.unityMC.UnityMC;
import org.lulunoel2016.unityMC.modules.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModuleManager {

    private final UnityMC plugin;
    private final List<IModule> modules;

    public ModuleManager(UnityMC plugin) {
        this.plugin = plugin;
        this.modules = new ArrayList<>();
    }

    public void loadModules() {
        modules.add(new ExampleModule());
        modules.add(new GodModeModule());
        modules.add(new GameModeModule());
        modules.add(new MysticPowersModule());
        modules.add(new TreasureHuntModule());
        modules.add(new EclipseModule(plugin));
        modules.add(new PortalModule());
        modules.add(new SpellModule());
        modules.add(new TotemModule());

        for (IModule module : modules) {
            String moduleName = module.getClass().getSimpleName();
            boolean isEnabled = plugin.getConfig().getBoolean("modules." + moduleName + ".enabled", true);

            if (isEnabled && !module.isEnabled()) {
                module.onEnable(plugin);
                plugin.getLogger().info(moduleName + " activé!");
            } else if (!isEnabled) {
                plugin.getLogger().info(moduleName + " est désactivé dans la configuration et ne sera pas activé.");
            }
        }
    }

    public void unloadModules() {
        for (IModule module : modules) {
            if (module.isEnabled()) {
                module.onDisable();
                plugin.getLogger().info(module.getClass().getSimpleName() + " désactivé!");
            }
        }
    }

    public void registerModule(IModule module) {
        modules.add(module);
    }

    public void enableModule(String moduleName) {
        Optional<IModule> module = getModuleByName(moduleName);
        if (module.isPresent()) {
            if (!module.get().isEnabled()) {
                module.get().onEnable(plugin);
                plugin.getLogger().info(moduleName + " activé !");
                saveModuleState(moduleName, true);
            } else {
                plugin.getLogger().warning(moduleName + " est déjà activé.");
            }
        } else {
            plugin.getLogger().warning("Le module " + moduleName + " n'existe pas.");
        }
    }

    public void disableModule(String moduleName) {
        Optional<IModule> module = getModuleByName(moduleName);
        if (module.isPresent()) {
            if (module.get().isEnabled()) {
                module.get().onDisable();
                plugin.getLogger().info(moduleName + " désactivé !");
                saveModuleState(moduleName, false);
            } else {
                plugin.getLogger().warning(moduleName + " est déjà désactivé.");
            }
        } else {
            plugin.getLogger().warning("Le module " + moduleName + " n'existe pas.");
        }
    }

    public void reloadModule(String moduleName) {
        Optional<IModule> module = getModuleByName(moduleName);
        if (module.isPresent()) {
            if (module.get().isEnabled()) {
                module.get().onDisable();
            }
            module.get().onEnable(plugin);
            plugin.getLogger().info(moduleName + " rechargé !");
        } else {
            plugin.getLogger().warning("Le module " + moduleName + " n'existe pas.");
        }
    }

    private Optional<IModule> getModuleByName(String moduleName) {
        return modules.stream()
                .filter(module -> module.getClass().getSimpleName().equalsIgnoreCase(moduleName))
                .findFirst();
    }

    private void saveModuleState(String moduleName, boolean isEnabled) {
        plugin.getConfig().set("modules." + moduleName + ".enabled", isEnabled);
        plugin.saveConfig();
    }
}
