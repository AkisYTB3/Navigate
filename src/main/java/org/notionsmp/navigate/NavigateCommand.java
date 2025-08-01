package org.notionsmp.navigate;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.management.ManagementFactory;
import java.time.ZoneId;

@CommandAlias("navigate")
@CommandPermission("navigate.use")
public class NavigateCommand extends BaseCommand {
    MiniMessage miniMessage = MiniMessage.miniMessage();

    private final Navigate plugin;

    public NavigateCommand(Navigate plugin) {
        this.plugin = plugin;
    }

    @Subcommand("start")
    @Syntax("[x] [y] [z] (safeMode)")
    @CommandCompletion("@nothing @nothing @nothing true|false")
    @Description("Start navigating to target location")
    public void onStart(Player player, int x, int y, int z, @Optional String safeMode) {
        boolean safeModeBool = safeMode == null || Boolean.parseBoolean(safeMode);
        if (plugin.getActiveNavigations().containsKey(player.getUniqueId())) {
            plugin.getActiveNavigations().get(player.getUniqueId()).cancel();
            plugin.getActiveNavigations().remove(player.getUniqueId());
            player.sendMessage("Your previous navigation has been stopped.");
        }

        Location targetLocation = new Location(player.getWorld(), x, y, z);
        PathfindingTask newTask = new PathfindingTask(plugin, player, targetLocation, safeModeBool);
        newTask.runTaskTimer(plugin, 0L, 10L);
        plugin.getActiveNavigations().put(player.getUniqueId(), newTask);
        player.sendMessage("Navigation started to X:" + x + " Y:" + y + " Z:" + z + " with safe mode: " + safeModeBool);
    }

    @Subcommand("stop")
    @Description("Stop navigating")
    public void onStop(Player player) {
        if (plugin.getActiveNavigations().containsKey(player.getUniqueId())) {
            plugin.getActiveNavigations().get(player.getUniqueId()).cancel();
            plugin.getActiveNavigations().remove(player.getUniqueId());
            player.sendMessage("Navigation stopped.");
        } else {
            player.sendMessage("You are not currently navigating.");
        }
    }

    @Subcommand("startfor")
    @CommandPermission("navigate.use.others")
    @Syntax("<player> <x> <y> <z> (safeMode)")
    @CommandCompletion("@players @nothing @nothing @nothing true|false")
    @Description("Start navigating player to target location")
    public void onStartFor(CommandSender sender, String playerName, int x, int y, int z, @Optional String safeMode) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage("Player not found.");
            return;
        }

        boolean safeModeBool = safeMode == null || Boolean.parseBoolean(safeMode);
        if (plugin.getActiveNavigations().containsKey(player.getUniqueId())) {
            plugin.getActiveNavigations().get(player.getUniqueId()).cancel();
            plugin.getActiveNavigations().remove(player.getUniqueId());
            sender.sendMessage("Previous navigation for " + player.getName() + " has been stopped.");
        }

        Location targetLocation = new Location(player.getWorld(), x, y, z);
        PathfindingTask newTask = new PathfindingTask(plugin, player, targetLocation, safeModeBool);
        newTask.runTaskTimer(plugin, 0L, 10L);
        plugin.getActiveNavigations().put(player.getUniqueId(), newTask);
        sender.sendMessage("Navigation started for " + player.getName() + " to X:" + x + " Y:" + y + " Z:" + z + " with safe mode: " + safeModeBool);
        //player.sendMessage("Navigation started to X:" + x + " Y:" + y + " Z:" + z + " with safe mode: " + safeModeBool);
    }

    @Subcommand("stopfor")
    @CommandPermission("navigate.use.others")
    @Syntax("<player>")
    @CommandCompletion("@players")
    @Description("Stop navigating player")
    public void onStopFor(CommandSender sender, String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage("Player not found.");
            return;
        }

        if (plugin.getActiveNavigations().containsKey(player.getUniqueId())) {
            plugin.getActiveNavigations().get(player.getUniqueId()).cancel();
            plugin.getActiveNavigations().remove(player.getUniqueId());
            sender.sendMessage("Navigation stopped for " + player.getName() + ".");
            //player.sendMessage("Your navigation has been stopped.");
        } else {
            sender.sendMessage(player.getName() + " is not currently navigating.");
        }
    }

    @Subcommand("neofetch")
    @CommandPermission("navigate.use.neofetch")
    @Description("Show the amazing logo of the plugin")
    public void onNeofetch(CommandSender sender) {
        Server server = sender.getServer();
        int length = (sender instanceof ConsoleCommandSender ? 4 : sender.getName().length()) + 1 + server.getName().length();

        String username = sender instanceof ConsoleCommandSender ? "root" : sender.getName();
        String serverName = server.getName();
        String minecraftVersion = server.getMinecraftVersion();
        String tps = String.format("%.1f", Math.round(server.getTPS()[1] * 10) / 10.0);
        String uptime = formatMillis(ManagementFactory.getRuntimeMXBean().getUptime());
        String timezone = ZoneId.systemDefault().toString();
        String pluginVersion = plugin.getPluginMeta().getVersion();

        String message = "<br><hover:show_text:'Click to go to<br>the <color:#1bd96a>Modrinth</color> page'><click:open_url:'https://modrinth.com/project/3t39uukw'><#006565>" +
                "⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛  " + username + "<white>@</white>" + serverName + "<br>" +
                "⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛  <white>" + "-".repeat(length) + "</white><br>" +
                "⬛⬛⬛⬛⬛⬛<dark_gray>⬛</dark_gray><gray>⬛</gray>⬛⬛  Version<white>: " + minecraftVersion + "</white><br>" +
                "⬛⬛⬛⬛<dark_gray>⬛⬛</dark_gray><gray>⬛</gray><white>⬛</white>⬛⬛  TPS<white>: " + tps + "</white><br>" +
                "⬛⬛<dark_gray>⬛⬛⬛</dark_gray><gray>⬛</gray><white>⬛</white>⬛⬛⬛  Uptime<white>: " + uptime + "</white><br>" +
                "⬛<dark_gray>⬛⬛⬛</dark_gray><gray>⬛</gray><white>⬛⬛</white>⬛⬛⬛  Plugin Name<white>: Navigate</white><br>" +
                "⬛⬛⬛<gray>⬛</gray><white>⬛⬛</white>⬛⬛⬛⬛  Plugin Version<white>: " + pluginVersion + "</white><br>" +
                "⬛⬛⬛⬛<white>⬛⬛</white>⬛⬛⬛⬛  Timezone<white>: " + timezone + "</white><br>" +
                "⬛⬛⬛⬛<white>⬛</white>⬛⬛⬛⬛⬛  <black>0</black><dark_red>0</dark_red><dark_green>0</dark_green><gold>0</gold><dark_blue>0</dark_blue><dark_purple>0</dark_purple><dark_aqua>0</dark_aqua><gray>0</gray><br>" +
                "⬛⬛⬛⬛⬛⬛⬛⬛⬛⬛</#006565></click></hover>  <dark_gray>0</dark_gray><red>0</red><green>0</green><yellow>0</yellow><blue>0</blue><light_purple>0</light_purple><aqua>0</aqua><white>0</white>";

        sender.sendMessage(miniMessage.deserialize(message));
    }

    @HelpCommand()
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }

    public static String formatMillis(long milliseconds) {
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        return String.format("%02d Hours, %02d Minutes, %02d Seconds", hours, minutes, remainingSeconds);
    }
}