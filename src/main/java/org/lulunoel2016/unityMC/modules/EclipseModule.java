package org.lulunoel2016.unityMC.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.lulunoel2016.unityMC.UnityMC;
import org.lulunoel2016.unityMC.utils.IModule;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class EclipseModule implements IModule, CommandExecutor, Listener {

    private final UnityMC plugin;
    private boolean enabled = false;
    private boolean isEclipseActive = false;
    private long nextEclipseTime = System.currentTimeMillis() + 7200000; // Prochaine éclipse dans 2 heures
    private final Set<UUID> empoweredMonsters = new HashSet<>();

    public EclipseModule(UnityMC plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable(UnityMC plugin) {
        enabled = true;

        // Enregistrement des commandes et événements
        plugin.getCommand("starteclipse").setExecutor(this);
        plugin.getCommand("eclipsetime").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Démarrer la tâche de vérification d'éclipse
        startEclipseCheckTask();
    }

    @Override
    public void onDisable() {
        enabled = false;
        isEclipseActive = false;
        empoweredMonsters.clear();
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

    private void startEclipseCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!enabled) return;

                // Vérifier si une éclipse doit commencer
                if (!isEclipseActive && System.currentTimeMillis() >= nextEclipseTime) {
                    startEclipse();
                }
            }
        }.runTaskTimer(plugin, 0, 1200); // Vérifie toutes les 60 secondes
    }

    private void startEclipse() {
        isEclipseActive = true;
        nextEclipseTime = System.currentTimeMillis() + 7200000; // Prochaine éclipse dans 2 heures

        // Effets d'éclipse pour tous les mondes et joueurs
        for (World world : Bukkit.getWorlds()) {
            world.setTime(18000); // Change le temps pour la nuit
            world.setStorm(true); // Active la pluie
            world.playSound(world.getSpawnLocation(), Sound.AMBIENT_CAVE, 1.0f, 0.5f);
            for (Player player : world.getPlayers()) {
                player.sendTitle(ChatColor.DARK_PURPLE + "Éclipse Mystique", ChatColor.GRAY + "Survivez aux ténèbres !", 10, 70, 20);
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 6000, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 6000, 1));
            }
        }

        // Planifier la fin de l'éclipse
        new BukkitRunnable() {
            @Override
            public void run() {
                endEclipse();
            }
        }.runTaskLater(plugin, 6000); // Éclipse dure 5 minutes
    }

    private void endEclipse() {
        isEclipseActive = false;

        for (World world : Bukkit.getWorlds()) {
            world.setStorm(false); // Arrête la pluie
            for (Player player : world.getPlayers()) {
                player.sendMessage(ChatColor.GREEN + "L'éclipse est terminée ! Vous avez survécu aux ténèbres.");
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                player.removePotionEffect(PotionEffectType.RESISTANCE);
            }
        }

        // Récompenses pour les joueurs présents
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.giveExp(100);
            player.sendMessage(ChatColor.GOLD + "Vous recevez 100 points d'expérience pour avoir survécu à l'éclipse !");
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (isEclipseActive && event.getEntity() instanceof org.bukkit.entity.Monster monster) {
            empoweredMonsters.add(monster.getUniqueId());
            monster.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 6000, 1));
            monster.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000, 1));
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (isEclipseActive && empoweredMonsters.contains(event.getEntity().getUniqueId())) {
            event.setDamage(event.getDamage() * 1.5); // Augmente les dégâts des monstres pendant l'éclipse
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("starteclipse")) {
            if (!(sender.hasPermission("unitymc.eclipse.start"))) {
                sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission de lancer une éclipse.");
                return true;
            }
            startEclipse();
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Éclipse mystique lancée manuellement.");
            return true;
        } else if (label.equalsIgnoreCase("eclipsetime")) {
            long timeRemaining = nextEclipseTime - System.currentTimeMillis();
            long minutesRemaining = timeRemaining / 60000;
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Temps restant avant la prochaine éclipse : " + minutesRemaining + " minutes.");
            return true;
        }
        return false;
    }
}

