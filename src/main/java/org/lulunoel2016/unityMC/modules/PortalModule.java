package org.lulunoel2016.unityMC.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.lulunoel2016.unityMC.UnityMC;
import org.lulunoel2016.unityMC.utils.IModule;

import java.util.HashMap;
import java.util.Map;

public class PortalModule implements IModule, CommandExecutor, Listener {

    private JavaPlugin plugin;
    private boolean enabled = false;
    private final Map<String, Location> portals = new HashMap<>();

    @Override
    public void onEnable(UnityMC plugin) {
        this.plugin = plugin;
        enabled = true;

        // Enregistrement des commandes et événements
        plugin.getCommand("createportal").setExecutor(this);
        plugin.getCommand("listportals").setExecutor(this);
        plugin.getCommand("removeportal").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onDisable() {
        enabled = false;
        portals.clear();
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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent exécuter cette commande.");
            return true;
        }

        switch (label.toLowerCase()) {
            case "createportal":
                if (!player.hasPermission("unitymc.portal.create")) {
                    player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission de créer un portail.");
                    return true;
                }
                if (args.length < 1) {
                    player.sendMessage("§cUtilisation: /createportal <type>");
                    return true;
                }
                String type = args[0].toLowerCase();
                createPortal(player, type);
                break;

            case "listportals":
                player.sendMessage(ChatColor.GOLD + "Portails disponibles:");
                for (Map.Entry<String, Location> entry : portals.entrySet()) {
                    player.sendMessage(ChatColor.AQUA + entry.getKey() + " - " + formatLocation(entry.getValue()));
                }
                break;

            case "removeportal":
                if (!player.hasPermission("unitymc.portal.remove")) {
                    player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission de supprimer un portail.");
                    return true;
                }
                if (args.length < 1) {
                    player.sendMessage("§cUtilisation: /removeportal <nom>");
                    return true;
                }
                removePortal(player, args[0]);
                break;

            default:
                player.sendMessage("§cCommande inconnue.");
                return false;
        }
        return true;
    }

    private void createPortal(Player player, String type) {
        String portalName = "Portal-" + (portals.size() + 1);
        Location location = player.getLocation();
        portals.put(portalName, location);

        player.sendMessage(ChatColor.GREEN + "Portail " + portalName + " créé avec succès de type : " + type + " !");
    }

    private void removePortal(Player player, String portalName) {
        if (portals.remove(portalName) != null) {
            player.sendMessage(ChatColor.GREEN + "Le portail " + portalName + " a été supprimé.");
        } else {
            player.sendMessage(ChatColor.RED + "Portail introuvable.");
        }
    }

    private String formatLocation(Location location) {
        return "X: " + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Activation du portail avec un œil d'Ender
        if (item.getType() == Material.ENDER_EYE) {
            Location playerLocation = player.getLocation();
            Location nearestPortal = getNearestPortal(playerLocation);

            if (nearestPortal != null) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Activation du portail dimensionnel...");
                teleportToPortal(player, nearestPortal);
            } else {
                player.sendMessage(ChatColor.RED + "Aucun portail à proximité.");
            }
        }
    }

    private Location getNearestPortal(Location location) {
        Location nearestPortal = null;
        double minDistance = Double.MAX_VALUE;

        for (Location portalLocation : portals.values()) {
            double distance = location.distance(portalLocation);
            if (distance < minDistance) {
                minDistance = distance;
                nearestPortal = portalLocation;
            }
        }
        return nearestPortal;
    }

    private void teleportToPortal(Player player, Location portalLocation) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(portalLocation);
                player.sendMessage(ChatColor.GREEN + "Vous avez été téléporté à travers le portail !");
            }
        }.runTaskLater(plugin, 40); // Téléportation avec un délai de 2 secondes
    }
}
