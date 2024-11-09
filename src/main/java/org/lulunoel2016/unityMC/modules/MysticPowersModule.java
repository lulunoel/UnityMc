package org.lulunoel2016.unityMC.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.lulunoel2016.unityMC.UnityMC;
import org.lulunoel2016.unityMC.utils.IModule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MysticPowersModule implements IModule, CommandExecutor, Listener {

    private boolean enabled = false;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public void onEnable(UnityMC plugin) {
        enabled = true;

        // Enregistrement des commandes et événements
        plugin.getCommand("power").setExecutor(this);
        plugin.getCommand("powers").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onDisable() {
        enabled = false;
        cooldowns.clear();
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

        if (label.equalsIgnoreCase("powers")) {
            player.sendMessage(ChatColor.GOLD + "Pouvoirs Mystiques disponibles :");
            player.sendMessage(ChatColor.AQUA + "- Lévitation");
            player.sendMessage(ChatColor.AQUA + "- Invisibilité");
            player.sendMessage(ChatColor.AQUA + "- Vitesse Supersonique");
            player.sendMessage(ChatColor.AQUA + "- Bouclier de Régénération");
            player.sendMessage(ChatColor.AQUA + "- Poings enflammés");
            return true;
        }

        if (label.equalsIgnoreCase("power")) {
            if (args.length == 0) {
                player.sendMessage("§cUtilisation : /power <pouvoir>");
                return true;
            }

            String power = args[0].toLowerCase();
            activatePower(player, power);
            return true;
        }

        return false;
    }

    private void activatePower(Player player, String power) {
        if (isInCooldown(player)) {
            player.sendMessage(ChatColor.RED + "Votre pouvoir est en recharge !");
            return;
        }

        switch (power) {
            case "levitation":
                if (!player.hasPermission("unitymc.power.levitation")) {
                    player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser ce pouvoir.");
                    return;
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 1));
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Vous flottez dans les airs !");
                break;

            case "invisibilite":
                if (!player.hasPermission("unitymc.power.invisibility")) {
                    player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser ce pouvoir.");
                    return;
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 1));
                player.sendMessage(ChatColor.GRAY + "Vous êtes maintenant invisible !");
                break;

            case "vitesse":
                if (!player.hasPermission("unitymc.power.speed")) {
                    player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser ce pouvoir.");
                    return;
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 3));
                player.sendMessage(ChatColor.YELLOW + "Vitesse supersonique activée !");
                break;

            case "bouclier":
                if (!player.hasPermission("unitymc.power.shield")) {
                    player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser ce pouvoir.");
                    return;
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 2));
                player.sendMessage(ChatColor.GREEN + "Bouclier de régénération activé !");
                break;

            case "feu":
                if (!player.hasPermission("unitymc.power.firepunch")) {
                    player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser ce pouvoir.");
                    return;
                }
                player.sendMessage(ChatColor.RED + "Vos poings sont maintenant enflammés !");
                break;

            default:
                player.sendMessage("§cPouvoir inconnu. Utilisez /powers pour la liste.");
                return;
        }

        // Mise en place du temps de recharge
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private boolean isInCooldown(Player player) {
        Long lastUsed = cooldowns.get(player.getUniqueId());
        return lastUsed != null && (System.currentTimeMillis() - lastUsed) < 10000; // 10 secondes de cooldown
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (cooldowns.containsKey(player.getUniqueId())) {
                event.getEntity().setFireTicks(100); // Enflamme l'entité frappée pendant 5 secondes
            }
        }
    }
}
