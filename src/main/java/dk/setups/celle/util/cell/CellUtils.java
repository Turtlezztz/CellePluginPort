package dk.setups.celle.util.cell;

import dk.setups.celle.cell.Cell;
import dk.setups.celle.cell.CellGroup;
import dk.setups.celle.config.Config;
import dk.setups.celle.database.StoreManager;
import dk.setups.celle.util.NearbyPlayerMap;
import dk.setups.celle.util.PlayerSignDisallow;
import dk.setups.celle.util.SignContentCreator;
import dk.setups.celle.util.WorldGuardUtils;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;

@Component
public class CellUtils {
    private @Inject SignContentCreator content;
    private @Inject StoreManager stores;
    private @Inject WorldGuardUtils worldGuard;
    private @Inject Config config;
    private @Inject PlayerSignDisallow disallow;
    private @Inject JavaPlugin plugin;

    public void updateAndSave(Cell cell) {
        stores.getCellStore().persist(cell);
        update(cell);
    }

    public void update(Cell cell) {
        try {
            worldGuard.updateRegion(cell);
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to update region for cell " + cell.getName(), exception);
        }
        try {
            updateSign(cell);
        } catch (RuntimeException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to update sign for cell " + cell.getName(), exception);
        }
    }

    public void updateSign(Cell cell) {
        updateSign(NearbyPlayerMap.from(disallow.filter(Bukkit.getOnlinePlayers())), cell);
    }

    public void updateSign(NearbyPlayerMap nearby, Cell cell) {
        if (cell.getSign() == null) return;
        Location location = cell.getSign().getLocation();
        if (location.getWorld() == null || !location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) return;
        if (!(location.getBlock().getState() instanceof Sign sign)) return;
        for (Player player : nearby.getNearbyPlayers(location)) {
            if (!player.isOnline() || disallow.isDisallowed(player.getUniqueId())) continue;
            var lines = Arrays.stream(content.getSignContent(cell, player))
                    .map(LegacyComponentSerializer.legacySection()::deserialize).toList();
            for (org.bukkit.block.sign.Side side : org.bukkit.block.sign.Side.values()) {
                for (int index = 0; index < 4; index++) sign.getSide(side).line(index, lines.get(index));
            }
            player.sendBlockUpdate(location, sign);
        }
    }

    public boolean hasRentedMax(Player player, CellGroup group) {
        Collection<Cell> cells = stores.getCellStore().getOwnedCells(stores.getUserStore().get(player));
        if (cells.size() >= getGlobalMaxCells(player)) return true;
        long inGroup = cells.stream().filter(cell -> cell.getGroup().getId() == group.getId()).count();
        return inGroup >= group.getMaxRentedCells();
    }

    public int getGlobalMaxCells(Player player) {
        return config.getMaxCellsPerPlayer().keySet().stream()
                .filter(permission -> player.hasPermission(permission) || permission.equals("default"))
                .findFirst().map(config.getMaxCellsPerPlayer()::get).orElse(0);
    }
}
