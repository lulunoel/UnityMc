package org.lulunoel2016.unityMC.modules;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.lulunoel2016.unityMC.UnityMC;
import org.lulunoel2016.unityMC.utils.IModule;

public class GameModeModule implements IModule, CommandExecutor {

    private JavaPlugin plugin;
    private boolean enabled = false;

    @Override
    public void onEnable(UnityMC plugin) {
        this.plugin = plugin;
        enabled = true;

        // Enregistrer les commandes sur le thread principal
        Bukkit.getScheduler().runTask(plugin, () -> {
            registerCommand("gamemode");
            registerCommand("gmc");
            registerCommand("gms");
            registerCommand("gmsp");
            registerCommand("gma");
            registerCommand("gm");
        });
    }

    private void registerCommand(String command) {
        plugin.getCommand(command).setExecutor(this);
    }

    @Override
    public void onDisable() {
        enabled = false;
    }

    @Override
    public void setupDatabase() {
        // Pas de configuration de base de données pour ce module
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isEnabled()) {
            sender.sendMessage("§cLe module GameModeModule n'est pas activé.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent exécuter cette commande.");
            return true;
        }

        Player player = (Player) sender;
        Player targetPlayer = player;
        GameMode targetGameMode = null;

        if (label.equalsIgnoreCase("gamemode") || label.equalsIgnoreCase("gm")) {
            if (args.length == 0) {
                player.sendMessage("§cUtilisation: /" + label + " <creative|survival|spectator|adventure|0|1|2|3> [joueur]");
                return true;
            }
            targetGameMode = parseGameMode(args[0]);

            if (targetGameMode == null) {
                player.sendMessage("§cMode de jeu invalide. Utilisez creative, survival, spectator, adventure ou un chiffre entre 0 et 3.");
                return true;
            }

            if (args.length > 1) {
                if (!player.hasPermission("unitymc.gamemode.others")) {
                    player.sendMessage("§cVous n'avez pas la permission de changer le mode de jeu d'autres joueurs.");
                    return true;
                }
                targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null) {
                    player.sendMessage("§cLe joueur spécifié est introuvable.");
                    return true;
                }
            } else if (!player.hasPermission("unitymc.gamemode.self")) {
                player.sendMessage("§cVous n'avez pas la permission de changer votre propre mode de jeu.");
                return true;
            }
        } else {
            switch (label.toLowerCase()) {
                case "gmc":
                    targetGameMode = GameMode.CREATIVE;
                    break;
                case "gms":
                    targetGameMode = GameMode.SURVIVAL;
                    break;
                case "gmsp":
                    targetGameMode = GameMode.SPECTATOR;
                    break;
                case "gma":
                    targetGameMode = GameMode.ADVENTURE;
                    break;
                default:
                    player.sendMessage("§cCommande non reconnue.");
                    return true;
            }

            if (args.length > 0) {
                if (!player.hasPermission("unitymc.gamemode.others")) {
                    player.sendMessage("§cVous n'avez pas la permission de changer le mode de jeu d'autres joueurs.");
                    return true;
                }
                targetPlayer = Bukkit.getPlayer(args[0]);
                if (targetPlayer == null) {
                    player.sendMessage("§cLe joueur spécifié est introuvable.");
                    return true;
                }
            } else if (!player.hasPermission("unitymc.gamemode.self")) {
                player.sendMessage("§cVous n'avez pas la permission de changer votre propre mode de jeu.");
                return true;
            }
        }

        targetPlayer.setGameMode(targetGameMode);
        String modeMessage = "§aMode de jeu changé en " + targetGameMode.name().toLowerCase() + ".";
        if (targetPlayer.equals(player)) {
            player.sendMessage(modeMessage);
        } else {
            player.sendMessage("§aLe mode de jeu de " + targetPlayer.getName() + " a été changé en " + targetGameMode.name().toLowerCase() + ".");
            targetPlayer.sendMessage(modeMessage);
        }
        return true;
    }

    private GameMode parseGameMode(String mode) {
        switch (mode.toLowerCase()) {
            case "creative":
            case "1":
                return GameMode.CREATIVE;
            case "survival":
            case "0":
                return GameMode.SURVIVAL;
            case "spectator":
            case "3":
                return GameMode.SPECTATOR;
            case "adventure":
            case "2":
                return GameMode.ADVENTURE;
            default:
                return null;
        }
    }
}
