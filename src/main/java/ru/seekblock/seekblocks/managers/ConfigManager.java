package ru.seekblock.seekblocks.managers;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import ru.seekblock.seekblocks.SeekBlocks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {
    private final SeekBlocks plugin;
    private Set<Material> allowedBlocks;
    private int maxDisguises;

    public ConfigManager(SeekBlocks plugin) {
        this.plugin = plugin;
        this.allowedBlocks = new HashSet<>();
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        maxDisguises = config.getInt("max-disguises", 10);

        allowedBlocks.clear();
        defaultConfig(config);
        List<String> blockList = config.getStringList("allowed-blocks");

        if (blockList.isEmpty()) {
            addDefaultBlocks();
        } else {
            for (String blockName : blockList) {
                Material material = Material.getMaterial(blockName.toUpperCase());
                if (material != null && material.isBlock()) {
                    allowedBlocks.add(material);
                } else {
                    plugin.getLogger().warning("Invalid block in config: " + blockName);
                }
            }
        }
    }

    private void defaultConfig(FileConfiguration config) {
        if (!config.contains("lang")) {
            config.set("lang", "en_en");
        }
        if (!config.contains("allowed-blocks")) {
            config.set("allowed-blocks", Arrays.asList(
                    "STONE", "GRASS_BLOCK", "DIRT", "COBBLESTONE", "OAK_LOG",
                    "BRICKS", "SAND", "GRAVEL", "SNOW_BLOCK"
            ));
        }
        if (!config.contains("max-disguises")) {
            config.set("max-disguises", 10);
        }
        plugin.saveConfig();
    }

    private void addDefaultBlocks() {
        allowedBlocks.add(Material.STONE);
        allowedBlocks.add(Material.GRASS_BLOCK);
        allowedBlocks.add(Material.DIRT);
        allowedBlocks.add(Material.COBBLESTONE);
        allowedBlocks.add(Material.OAK_LOG);
        allowedBlocks.add(Material.BRICKS);
        allowedBlocks.add(Material.SAND);
        allowedBlocks.add(Material.GRAVEL);
        allowedBlocks.add(Material.SNOW_BLOCK);
    }

    public boolean isAllowedBlock(Material material) {
        return allowedBlocks.contains(material);
    }

    public int getMaxDisguises() {
        return maxDisguises;
    }

    public Set<Material> getAllowedBlocks() {
        return new HashSet<>(allowedBlocks);
    }
}