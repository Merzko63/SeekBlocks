// ===========================
// DisguiseListener.java
// ===========================
package ru.seekblock.seekblocks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import ru.seekblock.seekblocks.managers.DisguiseManager;

public class DisguiseListener implements Listener {
    private final SeekBlocks plugin;
    private final DisguiseManager disguiseManager;

    public DisguiseListener(SeekBlocks plugin) {
        this.plugin = plugin;
        this.disguiseManager = plugin.getDisguiseManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking()) {
            Block clickedBlock = event.getClickedBlock();

            if (clickedBlock != null && clickedBlock.getType() != Material.AIR) {
                event.setCancelled(true);
                disguiseManager.startDisguise(player, clickedBlock);
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (event.isSneaking()) {
            if (disguiseManager.isDisguised(player)) {
                disguiseManager.removeDisguise(player, true);
            } else if (disguiseManager.hasActiveDisguise(player)) {
                disguiseManager.centerBlock(player);
            }
        }
    }
}