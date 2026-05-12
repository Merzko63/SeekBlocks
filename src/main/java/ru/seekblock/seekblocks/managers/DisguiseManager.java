package ru.seekblock.seekblocks.managers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.seekblock.seekblocks.SeekBlocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class DisguiseManager {
    private final SeekBlocks plugin;
    private final Map<UUID, Material> playerBlocks;
    private final Map<UUID, FallingBlock> playerFallingBlocks;
    private final Map<UUID, Location> lastPlayerLocation;
    private final Map<UUID, Location> originalBlockLocations;
    private final Map<UUID, Boolean> isDisguised;
    private final Random random;

    public DisguiseManager(SeekBlocks plugin) {
        this.plugin = plugin;
        this.playerBlocks = new HashMap<>();
        this.playerFallingBlocks = new HashMap<>();
        this.lastPlayerLocation = new HashMap<>();
        this.originalBlockLocations = new HashMap<>();
        this.isDisguised = new HashMap<>();
        this.random = new Random();
    }

    public boolean canDisguiseAs(Material material) {
        return plugin.getConfigManager().isAllowedBlock(material);
    }

    public int getMaxDisguises() {
        return plugin.getConfigManager().getMaxDisguises();
    }

    public boolean isDisguised(Player player) {
        return isDisguised.containsKey(player.getUniqueId()) && isDisguised.get(player.getUniqueId());
    }

    public boolean hasActiveDisguise(Player player) {
        return playerBlocks.containsKey(player.getUniqueId());
    }

    public void startDisguise(Player player, Block clickedBlock) {
        UUID playerId = player.getUniqueId();
        Material blockType = clickedBlock.getType();

        if (!canDisguiseAs(blockType)) {
            sendMessage(player, "cannot-disguise");
            return;
        }

        if (hasActiveDisguise(player)) {
            removeDisguise(player, false);
        }

        Location blockLocation = clickedBlock.getLocation();
        originalBlockLocations.put(playerId, blockLocation);
        playerBlocks.put(playerId, blockType);
        isDisguised.put(playerId, true);
        clickedBlock.setType(Material.AIR);
        player.playSound(blockLocation, Sound.BLOCK_STONE_BREAK, 1.0F, 1.0F);

        sendMessage(player, "disguise-success");
        disguiseAsBlock(player, blockType, blockLocation);
    }

    private void disguiseAsBlock(Player player, Material blockType, Location blockLocation) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        player.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0F, 1.0F);

        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();

        Location targetLocation = blockLocation.clone().add(0.5, 0.0, 0.5);
        targetLocation.setYaw(yaw);
        targetLocation.setPitch(pitch);

        player.teleport(targetLocation);
        spawnExplosionParticles(player.getLocation(), 2.0);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && playerBlocks.containsKey(player.getUniqueId()) && playerBlocks.get(player.getUniqueId()) == blockType) {
                    moveBlockToFeet(player, blockType);
                    lastPlayerLocation.put(player.getUniqueId(), player.getLocation());
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void moveBlockToFeet(Player player, Material blockType) {
        UUID playerId = player.getUniqueId();

        if (playerFallingBlocks.containsKey(playerId) && playerFallingBlocks.get(playerId) != null) {
            playerFallingBlocks.get(playerId).remove();
        }

        Location playerLocation = player.getLocation();
        BlockData blockData = blockType.createBlockData();

        if (blockType == Material.SEA_PICKLE && blockData instanceof Waterlogged) {
            ((Waterlogged) blockData).setWaterlogged(false);
        }

        FallingBlock fallingBlock = player.getWorld().spawnFallingBlock(playerLocation, blockData);
        fallingBlock.setDropItem(false);
        fallingBlock.setGravity(false);
        fallingBlock.setInvulnerable(true);
        fallingBlock.setPersistent(false);
        fallingBlock.setVelocity(new Vector(0, 0, 0));
        playerFallingBlocks.put(playerId, fallingBlock);
        fallingBlock.teleport(playerLocation);
    }

    public void centerBlock(Player player) {
        Location playerLocation = player.getLocation();

        float yaw = playerLocation.getYaw();
        float pitch = playerLocation.getPitch();

        Location centeredLocation = new Location(
                playerLocation.getWorld(),
                Math.floor(playerLocation.getX()) + 0.5,
                Math.floor(playerLocation.getY()),
                Math.floor(playerLocation.getZ()) + 0.5
        );
        centeredLocation.setYaw(yaw);
        centeredLocation.setPitch(pitch);

        player.teleport(centeredLocation);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }

    public void removeDisguise(Player player, boolean placeBlock) {
        UUID playerId = player.getUniqueId();

        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);

        sendMessage(player, "disguise-removed");

        if (placeBlock && lastPlayerLocation.containsKey(playerId)) {
            Location blockLocation = lastPlayerLocation.get(playerId);
            Block block = blockLocation.getBlock();
            Material material = playerFallingBlocks.get(playerId) != null ?
                    playerFallingBlocks.get(playerId).getBlockData().getMaterial() : Material.STONE;

            if (material == Material.SEA_PICKLE) {
                BlockData seaPickleData = Material.SEA_PICKLE.createBlockData();
                if (seaPickleData instanceof Waterlogged) {
                    ((Waterlogged) seaPickleData).setWaterlogged(false);
                    block.setBlockData(seaPickleData);
                } else {
                    block.setType(material);
                }
            } else {
                block.setType(material);
            }
            lastPlayerLocation.remove(playerId);
        }

        if (originalBlockLocations.containsKey(playerId)) {
            Location originalLocation = originalBlockLocations.get(playerId);
            Material blockType = playerBlocks.get(playerId);
            if (blockType != null) {
                originalLocation.getBlock().setType(blockType);
            }
            originalBlockLocations.remove(playerId);
        }

        if (playerFallingBlocks.containsKey(playerId) && playerFallingBlocks.get(playerId) != null) {
            playerFallingBlocks.get(playerId).remove();
            playerFallingBlocks.remove(playerId);
        }

        playerBlocks.remove(playerId);
        isDisguised.remove(playerId);
    }

    public void removeAllDisguises() {
        for (FallingBlock fallingBlock : playerFallingBlocks.values()) {
            if (fallingBlock != null) {
                fallingBlock.remove();
            }
        }
        playerFallingBlocks.clear();
        playerBlocks.clear();
        lastPlayerLocation.clear();
        originalBlockLocations.clear();
        isDisguised.clear();
    }

    private void spawnExplosionParticles(Location location, double radius) {
        for (int i = 0; i < 50; i++) {
            double x = random.nextDouble() * 2 * radius - radius;
            double y = random.nextDouble() * 2 * radius - radius;
            double z = random.nextDouble() * 2 * radius - radius;
            Location particleLocation = location.clone().add(x, y, z);
            location.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, particleLocation, 1, 0.0, 0.0, 0.0, 0.01);
        }
    }

    private void sendMessage(Player player, String key) {
        String lang = plugin.getConfig().getString("lang", "en_en");
        String message = getMessage(key, lang);
        player.sendMessage(message);
    }

    private String getMessage(String key, String lang) {
        if (lang.equals("ru_ru")) {
            switch (key) {
                case "cannot-disguise": return "§cВы не можете замаскироваться под этот блок!";
                case "disguise-success": return "§aТеперь вы замаскированы под блок!";
                case "disguise-removed": return "§aВы больше не замаскированы!";
                default: return "§6Сообщение не найдено!";
            }
        } else {
            switch (key) {
                case "cannot-disguise": return "§cYou cannot disguise as this block!";
                case "disguise-success": return "§aYou are now disguised as a block!";
                case "disguise-removed": return "§aYou are no longer disguised!";
                default: return "§6Message not found!";
            }
        }
    }
}