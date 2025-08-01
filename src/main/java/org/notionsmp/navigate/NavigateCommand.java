package org.notionsmp.navigate;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("navigate")
@CommandPermission("navigate.use")
public class NavigateCommand extends BaseCommand {

    private final Navigate plugin;

    public NavigateCommand(Navigate plugin) {
        this.plugin = plugin;
    }

    @Subcommand("start")
    @Syntax("[x] [y] [z] (safeMode)")
    @CommandCompletion("@nothing @nothing @nothing true|false")
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
}