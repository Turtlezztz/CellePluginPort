package dk.setups.celle.util.cell;

import dk.setups.celle.cell.Cell;
import dk.setups.celle.cell.CellUser;
import dk.setups.celle.database.StoreManager;
import dk.setups.celle.event.member.CellAddMemberEvent;
import dk.setups.celle.event.member.CellRemoveMemberEvent;
import dk.setups.celle.event.rent.CellExpireEvent;
import dk.setups.celle.event.rent.CellExtendEvent;
import dk.setups.celle.event.rent.CellRentEvent;
import dk.setups.celle.event.rent.CellUnrentEvent;
import dk.setups.celle.util.WorldGuardUtils;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Component;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;

import java.util.Date;
import java.util.Optional;

@Component
public class CellAPI {

    private @Inject CellUtils utils;
    private @Inject StoreManager stores;
    private @Inject PluginManager pluginManager;
    private @Inject WorldGuardUtils worldGuard;

    public EventSuccess extendCell(Cell cell, CellUser user) {
        if (!cell.canExtend()) return EventSuccess.FAILED;
        if (this.callEvent(new CellExtendEvent(cell, user))) {
            return EventSuccess.CANCELLED;
        }
        stores.getCellStore().mutate(cell, current -> {
            if (!current.canExtend()) throw new IllegalArgumentException("Cell cannot be extended");
            current.extend();
        });
        utils.update(cell);
        return EventSuccess.SUCCESS;
    }

    public EventSuccess rentCell(Cell cell, CellUser user) {
        if (cell.isRented()) return EventSuccess.FAILED;
        if (this.callEvent(new CellRentEvent(cell, user))) {
            return EventSuccess.CANCELLED;
        }
        Date rentUntil = new Date(System.currentTimeMillis() + cell.getGroup().getRentTimeMillis());
        if(stores.getCellStore().tryChangeOwner(cell, user, rentUntil)) {
            utils.update(cell);
            return EventSuccess.SUCCESS;
        }
        return EventSuccess.FAILED;
    }

    public EventSuccess unrentCell(Cell cell, CellUser user) {
        if (this.callEvent(new CellUnrentEvent(cell, user))) {
            return EventSuccess.CANCELLED;
        }
        stores.getCellStore().mutate(cell, Cell::unrent);
        utils.update(cell);
        return EventSuccess.SUCCESS;
    }

    public EventSuccess addMember(Cell cell, CellUser user, OfflinePlayer target) {
        return addMember(cell, user, stores.getUserStore().get(target));
    }

    public EventSuccess addMember(Cell cell, CellUser user, CellUser target) {
        if(this.callEvent(new CellAddMemberEvent(cell, user, target))) {
            return EventSuccess.CANCELLED;
        }
        stores.getCellStore().mutate(cell, current -> current.addMember(target));
        utils.update(cell);
        return EventSuccess.SUCCESS;
    }

    public EventSuccess removeMember(Cell cell, CellUser user, CellUser target) {
        if(this.callEvent(new CellRemoveMemberEvent(cell, user, target))) {
            return EventSuccess.CANCELLED;
        }
        stores.getCellStore().mutate(cell, current -> current.removeMember(target));
        utils.update(cell);
        return EventSuccess.SUCCESS;
    }

    public void expireCell(Cell cell) {
        callEvent(new CellExpireEvent(cell));
        stores.getCellStore().mutate(cell, Cell::unrent);
        utils.update(cell);
    }

    public Optional<Cell> getCellAtLocation(Location location) {
        if (location.getWorld() == null) return Optional.empty();
        return worldGuard.getRegionsAt(location).stream()
                .sorted(java.util.Comparator.comparingInt(com.sk89q.worldguard.protection.regions.ProtectedRegion::getPriority).reversed())
                .map(region -> stores.getCellStore().getFromRegion(region.getId(), location.getWorld().getName()))
                .flatMap(Optional::stream).findFirst();
    }

    private boolean callEvent(Event event) {
        if (!org.bukkit.Bukkit.isPrimaryThread()) throw new IllegalStateException("Cell events require the server thread");
        pluginManager.callEvent(event);
        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }
}
