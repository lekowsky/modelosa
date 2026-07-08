package com.mondraq.command;

import com.mondraq.area.Greenhouse;
import com.mondraq.main.Main;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class GardenerCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public GardenerCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("\u00a7cTylko gracz może używać tej komendy.");
            return true;
        }
        if (!p.hasPermission("gardener.admin")) {
            p.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }
        if (args.length == 0) {
            sendHelp(p);
            return true;
        }

        String action = args[0].toLowerCase();
        switch (action) {
            case "setarea" -> {
                if (args.length < 2) {
                    p.sendMessage("\u00a7cUżycie: /gardener setarea <nazwa> [confirm]");
                    return true;
                }
                String name = args[1].toLowerCase();
                if (args.length > 2 && args[2].equalsIgnoreCase("confirm")) {
                    if (plugin.getAreaManager().confirmArea(p.getUniqueId(), name)) {
                        p.sendMessage("\u00a7aObszar szklarni '\u00a7e" + name + "\u00a7a' został ustawiony!");
                    } else {
                        p.sendMessage("\u00a7cUstaw oba punkty przed potwierdzeniem!");
                    }
                } else {
                    plugin.getAreaManager().startSettingUp(p.getUniqueId(), name);
                    p.getInventory().addItem(plugin.getItemManager().createSelectionWand());
                    p.sendMessage(plugin.getConfigManager().getMessage("give-wand"));
                    p.sendMessage("\u00a77Ustawiasz obszar dla: \u00a7e" + name);
                }
            }

            case "npc" -> {
                if (args.length < 2) {
                    p.sendMessage("\u00a7cUżycie: /gardener npc <nazwa>");
                    return true;
                }
                String name = args[1].toLowerCase();
                Greenhouse gh = plugin.getAreaManager().getGreenhouse(name);
                if (gh == null || !gh.isAreaSet()) {
                    p.sendMessage("\u00a7cSzklarnia nie istnieje lub nie ma obszaru!");
                    return true;
                }
                plugin.getNpcManager().spawnNpc(p.getLocation(), name);
                p.sendMessage("\u00a7aNPC szklarni '\u00a7e" + name + "\u00a7a' został postawiony.");
            }

            case "list" -> {
                var names = plugin.getAreaManager().getGreenhouseNames();
                if (names.isEmpty()) {
                    p.sendMessage("\u00a77Brak zdefiniowanych szklarni.");
                    return true;
                }
                p.sendMessage("\u00a7e\u00a7l\u2618 Szklarnie (" + names.size() + "):");
                for (String name : names) {
                    Greenhouse gh = plugin.getAreaManager().getGreenhouse(name);
                    boolean hasArea = gh.isAreaSet(), hasNpc = gh.getNpcUUID() != null;
                    p.sendMessage("  \u00a7e" + name + " \u00a77- "
                            + (hasArea ? "\u00a7a\u2713 Obszar" : "\u00a7c\u2717 Obszar") + " \u00a77| "
                            + (hasNpc ? "\u00a7a\u2713 NPC" : "\u00a7c\u2717 NPC"));
                }
            }

            case "remove" -> {
                if (args.length < 2) return true;
                String name = args[1].toLowerCase();
                plugin.getAreaManager().removeGreenhouse(name);
                p.sendMessage("\u00a7aSzklarnia '\u00a7e" + name + "\u00a7a' usunięta.");
            }

            case "reload" -> {
                plugin.fullReload();
                p.sendMessage(plugin.getConfigManager().getMessage("config-reloaded"));
            }

            case "debug" -> {
                p.sendMessage("\u00a7e\u00a7l=== Gardener Debug ===");
                p.sendMessage("\u00a7fSzklarnie: \u00a7e" + plugin.getAreaManager().getGreenhouseNames().size());
                p.sendMessage("\u00a7fAktywne sesje: \u00a7e" + plugin.getJobManager().getAllSessions().size());
                Location p1 = plugin.getAreaManager().getPos1(p.getUniqueId());
                Location p2 = plugin.getAreaManager().getPos2(p.getUniqueId());
                p.sendMessage("\u00a7fPunkt 1: " + format(p1));
                p.sendMessage("\u00a7fPunkt 2: " + format(p2));
            }

            default -> sendHelp(p);
        }
        return true;
    }

    private String format(Location loc) {
        return loc == null ? "\u00a7cbrak" : "\u00a7a" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private void sendHelp(Player p) {
        p.sendMessage("\u00a7e\u00a7lGardener \u00a77- Komendy:");
        p.sendMessage("\u00a7e/gardener setarea <nazwa> \u00a77- Ustaw obszar");
        p.sendMessage("\u00a7e/gardener npc <nazwa> \u00a77- Postaw NPC");
        p.sendMessage("\u00a7e/gardener list \u00a77- Lista szklarni");
        p.sendMessage("\u00a7e/gardener remove <nazwa> \u00a77- Usuń");
        p.sendMessage("\u00a7e/gardener reload \u00a77- Przeładuj");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("setarea", "npc", "list", "remove", "reload", "debug").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2) {
            String act = args[0].toLowerCase();
            if (act.equals("npc") || act.equals("remove") || act.equals("setarea")) {
                return new ArrayList<>(plugin.getAreaManager().getGreenhouseNames()).stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase())).toList();
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("setarea")) {
            return List.of("confirm").stream().filter(s -> s.startsWith(args[2].toLowerCase())).toList();
        }
        return List.of();
    }
}
