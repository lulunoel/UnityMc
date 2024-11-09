package org.lulunoel2016.unityMC.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.lulunoel2016.unityMC.UnityMC;

public class ModuleCommand implements CommandExecutor {

    private final UnityMC plugin;

    public ModuleCommand(UnityMC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) || sender.hasPermission("unitymc.module")) {
            if (args.length < 2) {
                sender.sendMessage("§cUtilisation : /module <enable|disable|reload> <nomModule>");
                return true;
            }

            String action = args[0];
            String moduleName = args[1];

            switch (action.toLowerCase()) {
                case "enable":
                    plugin.getModuleManager().enableModule(moduleName);
                    sender.sendMessage("§aModule " + moduleName + " activé.");
                    break;
                case "disable":
                    plugin.getModuleManager().disableModule(moduleName);
                    sender.sendMessage("§cModule " + moduleName + " désactivé.");
                    break;
                case "reload":
                    plugin.getModuleManager().reloadModule(moduleName);
                    sender.sendMessage("§eModule " + moduleName + " rechargé.");
                    break;
                default:
                    sender.sendMessage("§cAction inconnue. Utilisation : /module <enable|disable|reload> <nomModule>");
                    break;
            }
        } else {
            sender.sendMessage("§cVous n'avez pas la permission d'exécuter cette commande.");
        }
        return true;
    }
}
