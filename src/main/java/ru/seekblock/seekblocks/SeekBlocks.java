// ===========================
// SeekBlocks.java
// ===========================
package ru.seekblock.seekblocks;

import org.bukkit.plugin.java.JavaPlugin;
import ru.seekblock.seekblocks.command.SkCommand;
import ru.seekblock.seekblocks.managers.ConfigManager;
import ru.seekblock.seekblocks.managers.DisguiseManager;

public final class SeekBlocks extends JavaPlugin {
    private static SeekBlocks instance;
    private DisguiseManager disguiseManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        disguiseManager = new DisguiseManager(this);

        getServer().getPluginManager().registerEvents(new DisguiseListener(this), this);

        SkCommand skCommand = new SkCommand(this);
        getCommand("sk").setExecutor(skCommand);
        getCommand("sk").setTabCompleter(skCommand);

        getLogger().info("SeekBlocks enabled!");
    }

    @Override
    public void onDisable() {
        if (disguiseManager != null) {
            disguiseManager.removeAllDisguises();
        }
        getLogger().info("SeekBlocks disabled!");
    }

    public static SeekBlocks getInstance() {
        return instance;
    }

    public DisguiseManager getDisguiseManager() {
        return disguiseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}