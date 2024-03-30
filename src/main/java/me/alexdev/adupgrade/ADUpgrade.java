package me.alexdev.adupgrade;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class ADUpgrade extends JavaPlugin implements Listener {

    private static final int MAX_LEVEL = 5;
    private HashMap<UUID, Integer> kills = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("§aPlugin ativado!");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("§cPlugin desativado!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Limpar as estatísticas do jogador quando ele entra no servidor
        kills.put(event.getPlayer().getUniqueId(), 0);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = (Player) event.getEntity().getKiller();
            UUID playerId = player.getUniqueId();

            // Verificar se o jogador tem uma espada na mão
            ItemStack itemInHand = player.getItemInHand();
            if (itemInHand != null && itemInHand.getType() == Material.DIAMOND_SWORD) {
                // Verificar se o jogador já matou mobs suficientes para fazer um upgrade
                int currentKills = kills.getOrDefault(playerId, 0) + 1;
                kills.put(playerId, currentKills);

                // Exibir barra de progresso na Lore da espada
                showProgressBar(player, itemInHand, currentKills);

                if (currentKills >= 30) {
                    upgradeSword(player, itemInHand);
                    kills.put(playerId, 0); // Resetar o contador de kills
                }
            }
        }
    }

    private void upgradeSword(Player player, ItemStack sword) {
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            // Aumentar o nível da espada
            int currentLevel = sword.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
            int nextLevel = currentLevel + 1;

            if (nextLevel > MAX_LEVEL) { // MAX_LEVEL é uma constante que você pode definir
                player.sendMessage("§cSua espada já está no nível máximo!");
                return;
            }

            sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, nextLevel);

            // Aumentar a afiação da espada
            int sharpnessLevel = nextLevel; // Afiação é igual ao nível da espada
            meta.addEnchant(Enchantment.DAMAGE_ALL, sharpnessLevel, true);
            meta.addEnchant(Enchantment.DURABILITY, 3, true);

            // Remover a barra de progresso da Lore
            meta.setLore(null);

            sword.setItemMeta(meta);

            // Notificar o jogador sobre o upgrade
            player.sendMessage("§bSua espada foi aprimorada para o nível " + nextLevel + "!");
        }
    }

    private void showProgressBar(Player player, ItemStack item, int currentKills) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            int currentLevel = item.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
            if (currentLevel >= 5) { // Se já tiver atingido o primeiro nível de upgrade, não exibir a barra de progresso
                meta.setLore(null);
                item.setItemMeta(meta);
                return; // Sair da função
            }

            double progress = ((double) currentKills / 30) * 100;
            String progressBar = getProgressBar(progress);

            if (progress < 100) {
                meta.setLore(Collections.singletonList("§6Progresso de Upgrade: " + progressBar));
                item.setItemMeta(meta);
            } else {
                // Remover a barra de progresso da Lore quando o upgrade estiver completo
                meta.setLore(null);
                item.setItemMeta(meta);
            }
        }
    }

    private String getProgressBar(double progress) {
        StringBuilder progressBar = new StringBuilder("§e[");
        int progressBars = (int) (progress / 10);

        for (int i = 0; i < 10; i++) {
            if (i < progressBars) {
                progressBar.append("§a█");
            } else {
                progressBar.append("§7-");
            }
        }

        progressBar.append("§e] ").append((int) progress).append("§e%");
        return progressBar.toString();
    }
}
