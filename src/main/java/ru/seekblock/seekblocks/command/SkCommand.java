package ru.seekblock.seekblocks.command;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import ru.seekblock.seekblocks.SeekBlocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SkCommand implements TabExecutor {

    private final SeekBlocks plugin;

    public SkCommand(SeekBlocks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("seekblocks.admin")) {
            sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.getConfigManager().reloadConfig();
                sendMessage(sender, "reload-success");
                break;

            case "add":
                if (args.length < 2) {
                    sendMessage(sender, "usage-add");
                    return true;
                }

                String blockName = args[1].toUpperCase();
                Material material = Material.getMaterial(blockName);

                if (material == null) {
                    sendMessage(sender, "block-not-found", blockName);
                    return true;
                }

                if (!material.isBlock()) {
                    sendMessage(sender, "not-a-block", blockName);
                    return true;
                }

                if (material.name().contains("WATER") || material.name().contains("LAVA") ||
                        material.name().contains("BUBBLE") || material.name().contains("PORTAL")) {
                    sendMessage(sender, "cannot-add-fluid");
                    return true;
                }

                List<String> allowedBlocks = plugin.getConfig().getStringList("allowed-blocks");
                if (allowedBlocks.contains(material.name())) {
                    sendMessage(sender, "block-already-exists", material.name());
                    return true;
                }

                allowedBlocks.add(material.name());
                plugin.getConfig().set("allowed-blocks", allowedBlocks);
                plugin.saveConfig();
                plugin.getConfigManager().reloadConfig();

                sendMessage(sender, "block-added", material.name());
                break;

            case "remove":
                if (args.length < 2) {
                    sendMessage(sender, "usage-remove");
                    return true;
                }

                String removeBlock = args[1].toUpperCase();
                List<String> currentBlocks = plugin.getConfig().getStringList("allowed-blocks");

                if (!currentBlocks.contains(removeBlock)) {
                    sendMessage(sender, "block-not-in-config", removeBlock);
                    return true;
                }

                currentBlocks.remove(removeBlock);
                plugin.getConfig().set("allowed-blocks", currentBlocks);
                plugin.saveConfig();
                plugin.getConfigManager().reloadConfig();

                sendMessage(sender, "block-removed", removeBlock);
                break;

            case "list":
                List<String> blocks = plugin.getConfig().getStringList("allowed-blocks");
                sendMessage(sender, "allowed-blocks-header", String.valueOf(blocks.size()));
                for (String block : blocks) {
                    sender.sendMessage("§7- §f" + block);
                }
                break;

            case "help":
            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("seekblocks.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("reload", "add", "remove", "list", "help"), new ArrayList<>());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                return StringUtil.copyPartialMatches(args[1], getAllBlockMaterials(), new ArrayList<>());
            }

            if (args[0].equalsIgnoreCase("remove")) {
                List<String> currentBlocks = plugin.getConfig().getStringList("allowed-blocks");
                return StringUtil.copyPartialMatches(args[1], currentBlocks, new ArrayList<>());
            }
        }

        return Collections.emptyList();
    }

    private List<String> getAllBlockMaterials() {
        return Arrays.stream(Material.values())
                .filter(Material::isBlock)
                .filter(m -> !m.name().contains("WATER"))
                .filter(m -> !m.name().contains("LAVA"))
                .filter(m -> !m.name().contains("BUBBLE"))
                .filter(m -> !m.name().contains("PORTAL"))
                .filter(m -> !m.name().contains("AIR"))
                .filter(m -> !m.name().contains("STRUCTURE"))
                .filter(m -> !m.name().contains("COMMAND"))
                .filter(m -> !m.name().contains("BARRIER"))
                .filter(m -> !m.name().contains("LIGHT"))
                .map(Material::name)
                .sorted()
                .collect(Collectors.toList());
    }

    private void sendHelp(CommandSender sender) {
        String lang = plugin.getConfig().getString("lang", "en_en");
        if (lang.equals("ru_ru")) {
            sender.sendMessage("§6=== SeekBlocks Команды ===");
            sender.sendMessage("§7/sk reload §f- Перезагрузить конфиг");
            sender.sendMessage("§7/sk add <блок> §f- Добавить блок в список");
            sender.sendMessage("§7/sk remove <блок> §f- Удалить блок из списка");
            sender.sendMessage("§7/sk list §f- Показать разрешенные блоки");
            sender.sendMessage("§7/sk help §f- Показать эту справку");
        } else {
            sender.sendMessage("§6=== SeekBlocks Commands ===");
            sender.sendMessage("§7/sk reload §f- Reload configuration");
            sender.sendMessage("§7/sk add <block> §f- Add block to allowed list");
            sender.sendMessage("§7/sk remove <block> §f- Remove block from allowed list");
            sender.sendMessage("§7/sk list §f- Show allowed blocks");
            sender.sendMessage("§7/sk help §f- Show this help");
        }
    }

    private void sendMessage(CommandSender sender, String key, String... args) {
        String lang = plugin.getConfig().getString("lang", "en_en");
        String message = getMessage(key, lang);
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", args[i]);
        }
        sender.sendMessage(message);
    }

    private String getMessage(String key, String lang) {
        if (lang.equals("ru_ru")) {
            switch (key) {
                case "no-permission": return "§cУ вас нет прав на использование этой команды!";
                case "reload-success": return "§aКонфигурация перезагружена!";
                case "usage-add": return "§cИспользование: /sk add <блок>";
                case "usage-remove": return "§cИспользование: /sk remove <блок>";
                case "block-not-found": return "§cБлок {0} не найден!";
                case "not-a-block": return "§c{0} не является блоком!";
                case "cannot-add-fluid": return "§cНельзя добавлять жидкости или порталы!";
                case "block-already-exists": return "§cБлок {0} уже есть в конфиге!";
                case "block-added": return "§aБлок {0} добавлен в конфиг!";
                case "block-not-in-config": return "§cБлок {0} не найден в конфиге!";
                case "block-removed": return "§aБлок {0} удален из конфига!";
                case "allowed-blocks-header": return "§6Разрешенные блоки ({0}):";
                default: return "§6Сообщение не найдено!";
            }
        } else {
            switch (key) {
                case "no-permission": return "§cYou don't have permission to use this command!";
                case "reload-success": return "§aConfiguration reloaded!";
                case "usage-add": return "§cUsage: /sk add <block>";
                case "usage-remove": return "§cUsage: /sk remove <block>";
                case "block-not-found": return "§cBlock {0} not found!";
                case "not-a-block": return "§c{0} is not a block!";
                case "cannot-add-fluid": return "§cCannot add fluids or portals!";
                case "block-already-exists": return "§cBlock {0} is already in config!";
                case "block-added": return "§aBlock {0} added to config!";
                case "block-not-in-config": return "§cBlock {0} not found in config!";
                case "block-removed": return "§aBlock {0} removed from config!";
                case "allowed-blocks-header": return "§6Allowed blocks ({0}):";
                default: return "§6Message not found!";
            }
        }
    }
}