package dk.setups.celle.task;

import dk.setups.celle.cell.Cell;
import dk.setups.celle.database.StoreManager;
import dk.setups.celle.util.cell.CellAPI;
import dk.setups.celle.util.cell.CellUtils;
import dk.setups.celle.util.NearbyPlayerMap;
import dk.setups.celle.util.PlayerSignDisallow;
import eu.okaeri.injector.annotation.Inject;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

/** All cell mutations and player/world access are serialized on Paper's server thread. */
public class TaskUpdateCells extends BukkitRunnable {
    private @Inject StoreManager stores;
    private @Inject CellUtils utils;
    private @Inject Plugin plugin;
    private @Inject PlayerSignDisallow disallow;
    private @Inject CellAPI api;

    @Override
    public void run() {
        if (!plugin.isEnabled()) { cancel(); return; }
        NearbyPlayerMap nearby = NearbyPlayerMap.from(disallow.filter(Bukkit.getOnlinePlayers()));
        for (Cell cached : stores.getCellStore().getCache().getAll()) {
            try {
                if (cached.getRentedUntil() != null && !cached.isRented()) {
                    Cell cell = stores.getCellStore().get(cached.getId()).orElse(null);
                    if (cell != null && cell.getRentedUntil() != null && !cell.isRented()) api.expireCell(cell);
                } else if (cached.getSign() != null) {
                    utils.updateSign(nearby, cached);
                }
            } catch (Exception exception) {
                plugin.getLogger().log(Level.SEVERE, "Failed to update cell " + cached.getName(), exception);
            }
        }
    }
}
