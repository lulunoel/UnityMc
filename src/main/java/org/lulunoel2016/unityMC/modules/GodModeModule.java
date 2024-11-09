package org.lulunoel2016.unityMC.modules;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.lulunoel2016.unityMC.UnityMC;
import org.lulunoel2016.unityMC.utils.IModule;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GodModeModule implements IModule, CommandExecutor, Listener {

    private final Set<UUID> godPlayers = new HashSet<>();
    private boolean enabled = false;

    @Override
    public void onEnable(UnityMC plugin) {
        enabled = true;

        // Enregistrement des commandes
        plugin.getCommand("god").setExecutor(this);
        plugin.getCommand("ungod").setExecutor(this);

        // Enregistrement de l'événement pour gérer le mode god
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onDisable() {
        godPlayers.clear(); // Réinitialiser l'état des joueurs en mode god
    }

    @Override
    public void setupDatabase() {
        // Pas de configuration de base de données nécessaire pour ce module
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && godPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true); // Annule les dégâts si le joueur est en mode god
            player.setHealth(player.getMaxHealth()); // Restaure la santé au maximum
            player.setSaturation(20.0f); // Restaure la saturation au maximum
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && godPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true); // Annule la perte de nourriture pour les joueurs en mode god
            player.setSaturation(20.0f); // Assure une saturation maximale
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        godPlayers.remove(player.getUniqueId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isEnabled()) {
            sender.sendMessage("§cLe module GodModeModule n'est pas activé.");
            return true;
        }

        if (args.length > 1) {
            sender.sendMessage("§cUtilisation incorrecte. Utilisez /god [joueur] ou /ungod [joueur].");
            return true;
        }

        Player target;
        if (args.length == 1) {
            // Gestion d'une cible spécifiée
            if (!sender.hasPermission("unitymc.god.others")) {
                sender.sendMessage("§cVous n'avez pas la permission d'activer le mode God pour les autres joueurs.");
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cLe joueur spécifié est introuvable.");
                return true;
            }
        } else {
            // Gestion du mode God pour soi-même
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cSeuls les joueurs peuvent exécuter cette commande.");
                return true;
            }
            target = player;
        }

        // Gestion des commandes /god et /ungod
        if (command.getName().equalsIgnoreCase("god")) {
            if (godPlayers.contains(target.getUniqueId())) {
                sender.sendMessage("§e" + target.getName() + " est déjà en mode God.");
                return true;
            } else {
                godPlayers.add(target.getUniqueId());
                sender.sendMessage("§a" + target.getName() + " est maintenant en mode God!");
                target.sendMessage("§aVous êtes maintenant en mode God!");
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("ungod")) {
            if (!godPlayers.contains(target.getUniqueId())) {
                sender.sendMessage("§e" + target.getName() + " n'est pas en mode God.");
                return true;
            } else {
                godPlayers.remove(target.getUniqueId());
                sender.sendMessage("§c" + target.getName() + " n'est plus en mode God.");
                target.sendMessage("§cVous n'êtes plus en mode God.");
                return true;
            }
        }
        return true;
    }
}
