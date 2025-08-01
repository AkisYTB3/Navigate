package org.notionsmp.navigate;

import co.aikar.commands.PaperCommandManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Navigate extends JavaPlugin {

    private Map<UUID, PathfindingTask> activeNavigations;

    @Override
    public void onEnable() {
        activeNavigations = new HashMap<>();
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("help");
        manager.registerCommand(new NavigateCommand(this));
    }

    @Override
    public void onDisable() {
        for (PathfindingTask task : activeNavigations.values()) {
            task.cancel();
        }
        activeNavigations.clear();
    }

    public Map<UUID, PathfindingTask> getActiveNavigations() {
        return activeNavigations;
    }
}
