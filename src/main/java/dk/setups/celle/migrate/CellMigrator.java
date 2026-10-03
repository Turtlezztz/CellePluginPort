package dk.setups.celle.migrate;

import dk.setups.celle.cell.*;
import dk.setups.celle.database.StoreManager;
import dk.setups.celle.util.cell.CellFactory;
import lombok.Getter;
import org.bukkit.Bukkit;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public abstract class CellMigrator {
    private final StoreManager stores;
    private final CellFactory factory;
    private final File folder;
    public CellMigrator(StoreManager stores, CellFactory factory, File folder) {
        this.stores = stores;
        this.factory = factory;
        this.folder = folder;
    }

    /** Import all data atomically; repeated imports skip cells already present. */
    public void migrate() {
        Set<MigrateCell> pending = getCells().stream().filter(cell -> stores.getCellStore().getFromName(cell.getName()).isEmpty()).collect(Collectors.toSet());
        try {
            stores.transaction(() -> {
                Map<MigrateCellGroup, CellGroup> groups = new HashMap<>();
                for (MigrateCell source : pending) {
                    CellGroup group = groups.computeIfAbsent(source.getGroup(), definition -> {
                        String base = definition != null && definition.getName() != null ? definition.getName().toLowerCase(Locale.ROOT) : "staxi";
                        String name = base;
                        int suffix = 1;
                        while (stores.getGroupStore().getFromName(name).isPresent()) name = base + "_" + suffix++;
                        CellGroup created = definition == null ? factory.createGroup(name) : definition.toGroup(name, factory);
                        created.setName(name);
                        stores.getGroupStore().persist(created);
                        return created;
                    });
                    Cell cell = source.toCell(group, stores.getUserStore());
                    var existing = stores.getRegionStore().get(cell.getRegion().getName());
                    if (existing.isPresent() && !existing.get().getRegionWorld().equals(cell.getRegion().getRegionWorld())) {
                        throw new IllegalArgumentException("Legacy schema requires unique region names across worlds: " + cell.getRegion().getName());
                    }
                    stores.getRegionStore().persist(cell.getRegion());
                    if (cell.getSign() != null) stores.getSignStore().persist(cell.getSign());
                    if (cell.getTeleport() != null) stores.getTeleportStore().persist(cell.getTeleport());
                    stores.getCellStore().persist(cell);
                    Cell loaded = stores.getCellStore().get(cell.getId()).orElseThrow();
                    for (UUID uuid : source.getMembers()) {
                        loaded.addMember(stores.getUserStore().get(uuid, Bukkit.getOfflinePlayer(uuid).getName()));
                    }
                    stores.getCellStore().persist(loaded);
                }
                return null;
            });
        } finally {
            stores.getCellStore().updateCache();
        }
    }

    public Set<MigrateUser> getUsers() {
        Set<UUID> ids = new HashSet<>();
        for (MigrateCell cell : getCells()) {
            if (cell.getOwner() != null) ids.add(cell.getOwner());
            ids.addAll(cell.getMembers());
        }
        return ids.stream().map(MigrateUser::fromUUID).collect(Collectors.toSet());
    }
    public abstract Set<MigrateCell> getCells();
}
