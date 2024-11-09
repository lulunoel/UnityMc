package org.lulunoel2016.unityMC.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.lulunoel2016.unityMC.UnityMC;
import org.lulunoel2016.unityMC.utils.IModule;

import java.util.*;

public class TreasureHuntModule implements IModule, CommandExecutor, Listener {

    private JavaPlugin plugin;
    private boolean enabled = false;
    private final Map<UUID, Integer> playerPoints = new HashMap<>(); // Points par joueur
    private Location currentTreasureLocation = null;
    private String currentHint = "";

    @Override
    public void onEnable(UnityMC plugin) {
        this.plugin = plugin;
        enabled = true;

        // Enregistrement des commandes
        plugin.getCommand("starttreasurehunt").setExecutor(this);
        plugin.getCommand("hint").setExecutor(this);
        plugin.getCommand("leaderboard").setExecutor(this);
    }

    @Override
    public void onDisable() {
        enabled = false;
        playerPoints.clear();
        currentTreasureLocation = null;
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent exécuter cette commande.");
            return true;
        }

        switch (label.toLowerCase()) {
            case "starttreasurehunt":
                if (!player.hasPermission("unitymc.treasurehunt.start")) {
                    player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission de lancer une chasse aux trésors.");
                    return true;
                }
                startTreasureHunt(player);
                break;

            case "hint":
                if (!player.hasPermission("unitymc.treasurehunt.hint")) {
                    player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission de demander un indice.");
                    return true;
                }
                player.sendMessage(ChatColor.GOLD + "Indice : " + currentHint);
                break;

            case "leaderboard":
                showLeaderboard(player);
                break;

            default:
                player.sendMessage("§cCommande inconnue.");
                return false;
        }
        return true;
    }

    private void startTreasureHunt(Player player) {
        // Génération d'une position aléatoire pour le trésor
        Location randomLocation = generateRandomLocation(player.getWorld().getSpawnLocation());
        currentTreasureLocation = randomLocation;
        currentHint = "Cherchez près de X: " + randomLocation.getBlockX() + ", Z: " + randomLocation.getBlockZ();

        player.sendMessage(ChatColor.GREEN + "Une chasse aux trésors a commencé ! Utilisez /hint pour obtenir des indices.");
        Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + " a lancé une chasse aux trésors !");

        // Planification pour créer le trésor après quelques secondes
        new BukkitRunnable() {
            @Override
            public void run() {
                spawnTreasure(randomLocation);
            }
        }.runTaskLater(plugin, 100); // 5 secondes de délai
    }

    private Location generateRandomLocation(Location spawnLocation) {
        Random random = new Random();
        int x = spawnLocation.getBlockX() + random.nextInt(1000) - 500;
        int z = spawnLocation.getBlockZ() + random.nextInt(1000) - 500;
        int y = spawnLocation.getWorld().getHighestBlockYAt(x, z);
        return new Location(spawnLocation.getWorld(), x, y, z);
    }

    private void spawnTreasure(Location location) {
        location.getWorld().getBlockAt(location).setType(Material.CHEST);
        Bukkit.broadcastMessage(ChatColor.GOLD + "Un trésor mystique est apparu ! Utilisez /hint pour obtenir des indices.");
    }

    private void showLeaderboard(Player player) {
        player.sendMessage(ChatColor.DARK_PURPLE + "Classement des Chasseurs de Trésors :");
        playerPoints.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    String playerName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                    player.sendMessage(ChatColor.LIGHT_PURPLE + playerName + " : " + entry.getValue() + " points");
                });
    }

    public void playerFoundTreasure(Player player) {
        // Récompense le joueur
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
        player.sendMessage(ChatColor.GREEN + "Félicitations ! Vous avez trouvé le trésor et gagné un diamant !");
        Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + " a trouvé le trésor mystique !");

        // Augmente les points du joueur
        playerPoints.put(player.getUniqueId(), playerPoints.getOrDefault(player.getUniqueId(), 0) + 1);

        // Lance un nouveau trésor
        startTreasureHunt(player);
    }
}
