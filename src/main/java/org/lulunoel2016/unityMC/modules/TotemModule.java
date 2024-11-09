package org.lulunoel2016.unityMC.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.lulunoel2016.unityMC.UnityMC;
import org.lulunoel2016.unityMC.utils.IModule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TotemModule implements IModule, CommandExecutor, Listener {

    private JavaPlugin plugin;
    private boolean enabled = false;
    private final Map<Location, TotemType> totems = new HashMap<>();

    private enum TotemType {
        POWER, REGENERATION, CURSE, SPEED, RESISTANCE
    }

    @Override
    public void onEnable(UnityMC plugin) {
        this.plugin = plugin;
        enabled = true;

        // Enregistrement des commandes
        plugin.getCommand("placetotem").setExecutor(this);
        plugin.getCommand("removetotem").setExecutor(this);
        plugin.getCommand("totemlist").setExecutor(this);

        // Lancement de la tâche pour appliquer les effets des totems
        startTotemEffectTask();
    }

    @Override
    public void onDisable() {
        enabled = false;
        totems.clear();
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
            case "placetotem":
                if (!player.hasPermission("unitymc.totem.place")) {
                    player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission de placer un totem.");
                    return true;
                }
                if (args.length == 0) {
                    player.sendMessage("§cUtilisation: /placetotem <power|regeneration|curse|speed|resistance>");
                    return true;
                }
                TotemType totemType;
                try {
                    totemType = TotemType.valueOf(args[0].toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ChatColor.RED + "Type de totem invalide. Utilisez /placetotem <power|regeneration|curse|speed|resistance>.");
                    return true;
                }
                placeTotem(player.getLocation(), totemType);
                player.sendMessage(ChatColor.GREEN + "Totem de type " + totemType + " placé !");
                break;

            case "removetotem":
                if (!player.hasPermission("unitymc.totem.remove")) {
                    player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission de supprimer un totem.");
                    return true;
                }
                removeNearestTotem(player);
                break;

            case "totemlist":
                player.sendMessage(ChatColor.GOLD + "Liste des Totems Actifs :");
                for (Map.Entry<Location, TotemType> entry : totems.entrySet()) {
                    player.sendMessage(ChatColor.AQUA + "- " + entry.getValue() + " à " + formatLocation(entry.getKey()));
                }
                break;

            default:
                player.sendMessage("§cCommande inconnue.");
                return false;
        }
        return true;
    }

    private void placeTotem(Location location, TotemType totemType) {
        totems.put(location, totemType);
        location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
    }

    private void removeNearestTotem(Player player) {
        Location playerLocation = player.getLocation();
        Location nearestTotem = null;
        double minDistance = Double.MAX_VALUE;

        for (Location location : totems.keySet()) {
            double distance = playerLocation.distance(location);
            if (distance < minDistance) {
                minDistance = distance;
                nearestTotem = location;
            }
        }

        if (nearestTotem != null) {
            totems.remove(nearestTotem);
            player.sendMessage(ChatColor.GREEN + "Le totem le plus proche a été supprimé.");
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f);
        } else {
            player.sendMessage(ChatColor.RED + "Aucun totem trouvé à proximité.");
        }
    }

    private void startTotemEffectTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Location, TotemType> entry : totems.entrySet()) {
                    applyTotemEffect(entry.getKey(), entry.getValue());
                }
            }
        }.runTaskTimer(plugin, 0, 100); // Exécute toutes les 5 secondes
    }

    private void applyTotemEffect(Location location, TotemType totemType) {
        location.getWorld().spawnParticle(Particle.SPIT, location, 10, 0.5, 1, 0.5, 0.1);

        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) <= 10) { // Rayon de 10 blocs
                switch (totemType) {
                    case POWER:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 0, false, false));
                        break;
                    case REGENERATION:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, false, false));
                        break;
                    case CURSE:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1, false, false));
                        break;
                    case SPEED:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, false, false));
                        break;
                    case RESISTANCE:
                        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 1, false, false));
                        break;
                }
            }
        }
    }

    private String formatLocation(Location location) {
        return "X: " + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ();
    }
}
