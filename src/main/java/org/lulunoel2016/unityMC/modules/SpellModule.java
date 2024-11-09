package org.lulunoel2016.unityMC.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.lulunoel2016.unityMC.UnityMC;
import org.lulunoel2016.unityMC.utils.IModule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpellModule implements IModule, CommandExecutor, Listener {

    private JavaPlugin plugin;
    private boolean enabled = false;
    private final Map<UUID, Long> cooldowns = new HashMap<>(); // Temps de recharge pour chaque joueur

    @Override
    public void onEnable(UnityMC plugin) {
        this.plugin = plugin;
        enabled = true;

        // Enregistrement des commandes et événements
        plugin.getCommand("cast").setExecutor(this);
        plugin.getCommand("spellbook").setExecutor(this);
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

        switch (label.toLowerCase()) {
            case "cast":
                if (!player.hasPermission("unitymc.spell.cast")) {
                    player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission de lancer des sorts.");
                    return true;
                }
                if (args.length == 0) {
                    player.sendMessage("§cUtilisation: /cast <sort>");
                    return true;
                }
                String spellName = args[0].toLowerCase();
                castSpell(player, spellName);
                break;

            case "spellbook":
                if (!player.hasPermission("unitymc.spell.list")) {
                    player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission de voir le livre des sorts.");
                    return true;
                }
                showSpellBook(player);
                break;

            default:
                player.sendMessage("§cCommande inconnue.");
                return false;
        }
        return true;
    }

    private void castSpell(Player player, String spellName) {
        if (isInCooldown(player)) {
            player.sendMessage(ChatColor.RED + "Votre sort est en recharge !");
            return;
        }

        switch (spellName) {
            case "bouledefeu":
                if (hasSpellComponents(player, Material.BLAZE_POWDER, Material.FIRE_CHARGE)) {
                    player.launchProjectile(Fireball.class);
                    player.sendMessage(ChatColor.RED + "Vous avez lancé une boule de feu !");
                    player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
                } else {
                    player.sendMessage(ChatColor.RED + "Vous avez besoin de Blaze Powder et Fire Charge pour lancer ce sort.");
                }
                break;

            case "guerison":
                if (hasSpellComponents(player, Material.GOLDEN_APPLE)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 2));
                    player.sendMessage(ChatColor.GREEN + "Vous vous êtes soigné !");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                } else {
                    player.sendMessage(ChatColor.RED + "Vous avez besoin d'une Golden Apple pour lancer ce sort.");
                }
                break;

            case "bouclier":
                if (hasSpellComponents(player, Material.SHIELD, Material.IRON_INGOT)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1));
                    player.sendMessage(ChatColor.BLUE + "Vous avez activé un bouclier magique !");
                } else {
                    player.sendMessage(ChatColor.RED + "Vous avez besoin d'un Shield et Iron Ingot pour lancer ce sort.");
                }
                break;

            case "invocation":
                if (hasSpellComponents(player, Material.BONE)) {
                    Wolf wolf = player.getWorld().spawn(player.getLocation(), Wolf.class);
                    wolf.setOwner(player);
                    wolf.setCustomName("Loup Protecteur");
                    wolf.setCustomNameVisible(true);
                    player.sendMessage(ChatColor.GRAY + "Vous avez invoqué un loup protecteur !");
                } else {
                    player.sendMessage(ChatColor.RED + "Vous avez besoin d'un Bone pour lancer ce sort.");
                }
                break;

            case "transformation":
                if (hasSpellComponents(player, Material.BAT_SPAWN_EGG)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 200, 1));
                    player.sendMessage(ChatColor.DARK_PURPLE + "Vous vous êtes transformé en chauve-souris !");
                } else {
                    player.sendMessage(ChatColor.RED + "Vous avez besoin d'un Bat Spawn Egg pour lancer ce sort.");
                }
                break;

            default:
                player.sendMessage(ChatColor.RED + "Sort inconnu. Utilisez /spellbook pour voir la liste des sorts.");
                return;
        }

        // Mise en place du temps de recharge
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private boolean hasSpellComponents(Player player, Material... components) {
        for (Material material : components) {
            if (!player.getInventory().contains(material)) {
                return false;
            }
        }
        for (Material material : components) {
            player.getInventory().removeItem(new ItemStack(material, 1));
        }
        return true;
    }

    private boolean isInCooldown(Player player) {
        Long lastUsed = cooldowns.get(player.getUniqueId());
        return lastUsed != null && (System.currentTimeMillis() - lastUsed) < 10000; // 10 secondes de cooldown
    }

    private void showSpellBook(Player player) {
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Livre des Sorts Enchantés :");
        player.sendMessage(ChatColor.AQUA + "- Boule de Feu (Blaze Powder + Fire Charge)");
        player.sendMessage(ChatColor.AQUA + "- Guérison (Golden Apple)");
        player.sendMessage(ChatColor.AQUA + "- Bouclier Magique (Shield + Iron Ingot)");
        player.sendMessage(ChatColor.AQUA + "- Invocation (Bone)");
        player.sendMessage(ChatColor.AQUA + "- Transformation (Bat Spawn Egg)");
    }
}

