package dk.setups.celle.database;

import dk.setups.celle.cell.*;
import dk.setups.celle.config.DefaultConfig;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;

class StoreManagerTest {
    @TempDir Path folder;
    StoreManager stores;
    Plugin plugin;

    @BeforeEach void open() throws Exception {
        new DefaultConfig();
        plugin = (Plugin) Proxy.newProxyInstance(Plugin.class.getClassLoader(), new Class<?>[]{Plugin.class},
                (proxy, method, args) -> method.getName().equals("getDataFolder") ? folder.toFile() : null);
        stores = new StoreManager();
        stores.init(Logger.getLogger("test"), plugin);
    }
    @AfterEach void close() throws Exception { stores.disconnect(); }

    Cell create(String name) {
        CellGroup group = new CellGroup("a");
        stores.getGroupStore().persist(group);
        CellRegion region = new CellRegion(name, "world");
        stores.getRegionStore().persist(region);
        Cell cell = new Cell(name, group, region);
        stores.getCellStore().persist(cell);
        return stores.getCellStore().get(cell.getId()).orElseThrow();
    }

    @Test void generatedIdsAreCachedAfterInsertion() {
        Cell cell = create("c1");
        assertTrue(cell.getId() > 0);
        assertNotNull(stores.getCellStore().getCache().getCached(cell.getId()));
        assertNull(stores.getCellStore().getCache().getCached(0));
    }

    @Test void aLeaseCannotBeSoldTwiceAndSurvivesRestart() throws Exception {
        Cell cell = create("c1");
        CellUser owner = stores.getUserStore().get(UUID.randomUUID(), "Owner");
        CellUser other = stores.getUserStore().get(UUID.randomUUID(), "Other");
        assertTrue(stores.getCellStore().tryChangeOwner(cell, owner, new Date(System.currentTimeMillis() + 60_000)));
        assertFalse(stores.getCellStore().tryChangeOwner(cell, other, new Date(System.currentTimeMillis() + 60_000)));
        stores.disconnect();
        stores = new StoreManager();
        stores.init(Logger.getLogger("test"), plugin);
        Cell loaded = stores.getCellStore().get(cell.getId()).orElseThrow();
        assertEquals(owner.getUuid(), loaded.getOwner().getUuid());
        assertTrue(loaded.isRented());
        assertEquals(1, stores.getCellStore().getOwnedCells(owner).size());
    }

    @Test void membershipAndUnrentPersist() {
        Cell cell = create("c1");
        CellUser owner = stores.getUserStore().get(UUID.randomUUID(), "Owner");
        CellUser member = stores.getUserStore().get(UUID.randomUUID(), "Member");
        assertTrue(stores.getCellStore().tryChangeOwner(cell, owner, new Date(System.currentTimeMillis() + 60_000)));
        cell = stores.getCellStore().get(cell.getId()).orElseThrow();
        cell.addMember(member);
        stores.getCellStore().persist(cell);
        assertTrue(stores.getCellStore().get(cell.getId()).orElseThrow().isPermitted(member));
        assertEquals(1, stores.getCellStore().getPermittedCells(member).size());
        cell.unrent();
        stores.getCellStore().persist(cell);
        Cell loaded = stores.getCellStore().get(cell.getId()).orElseThrow();
        assertFalse(loaded.isRented());
        assertNull(loaded.getOwner());
        assertTrue(loaded.getMembers().isEmpty());
    }

    @Test void failedWritesDoNotCreatePhantomCacheEntries() {
        create("c1");
        CellGroup group = stores.getGroupStore().getFromName("a").orElseThrow();
        Cell duplicate = new Cell("c1", group, new CellRegion("c2", "world"));
        assertThrows(IllegalStateException.class, () -> stores.getCellStore().persist(duplicate));
        assertNull(stores.getCellStore().getCache().getCached(0));
    }

    @Test void cacheRefreshRemovesDeletedDatabaseRows() throws Exception {
        Cell cell = create("c1");
        stores.getCellStore().getDao().deleteById(cell.getId());
        stores.getCellStore().updateCache();
        assertTrue(stores.getCellStore().getCache().getAll().isEmpty());
    }
    @Test void databaseTransactionsRollBackFailedImports() {
        assertThrows(IllegalStateException.class, () -> stores.transaction(() -> {
            stores.getGroupStore().persist(new CellGroup("rollback"));
            throw new java.sql.SQLException("Simulated import failure");
        }));
        assertTrue(stores.getGroupStore().getFromName("rollback").isEmpty());
    }

    @Test void migrationSupportsUnrentedCellsWithNoSignOrOwnerAndCanBeRepeated() {
        var source = new dk.setups.celle.migrate.MigrateCell("imported", new CellRegion("imported", "world"), new dk.setups.celle.migrate.MigrateCellGroup(100, "prisoner"));
        var migration = new dk.setups.celle.migrate.CellMigrator(stores, new dk.setups.celle.util.cell.CellFactory(), folder.toFile()) {
            @Override public java.util.Set<dk.setups.celle.migrate.MigrateCell> getCells() { return java.util.Set.of(source); }
        };
        migration.migrate();
        migration.migrate();
        Cell loaded = stores.getCellStore().getFromName("imported").orElseThrow();
        assertNotNull(loaded.getGroup());
        assertEquals(100, loaded.getGroup().getRentPrice());
        assertFalse(loaded.isRented());
        assertEquals(1, stores.getCellStore().getAll().size());
        assertEquals(1, stores.getGroupStore().getAll().size());
    }

    @Test void logFiltersCombineCellActorAndAction() {
        Cell cell = create("c1");
        CellUser actor = stores.getUserStore().get(UUID.randomUUID(), "actor");
        var rent = new dk.setups.celle.cell.log.CellLog(dk.setups.celle.cell.log.LoggableAction.RENT, cell, actor);
        stores.getLogStore().persist(rent);
        stores.getLogStore().persist(new dk.setups.celle.cell.log.CellLog(dk.setups.celle.cell.log.LoggableAction.EXTEND, cell, actor));
        var filter = new dk.setups.celle.cell.log.CellLogFilter(actor,null,false,cell,dk.setups.celle.cell.log.LoggableAction.RENT);
        assertEquals(1, stores.getLogStore().getLogs(filter,10,0).size());
    }

    @Test void failedLeaseMutationRollsBackOwnerAndMembers() {
        Cell cell = create("c1");
        CellUser owner = stores.getUserStore().get(UUID.randomUUID(), "Owner");
        CellUser member = stores.getUserStore().get(UUID.randomUUID(), "Member");
        assertTrue(stores.getCellStore().tryChangeOwner(cell, owner, new Date(System.currentTimeMillis() + 60_000)));
        stores.getCellStore().mutate(cell, current -> current.addMember(member));
        assertThrows(RuntimeException.class, () -> stores.getCellStore().mutate(cell, current -> {
            current.unrent();
            throw new IllegalArgumentException("Simulated failed mutation");
        }));
        Cell loaded = stores.getCellStore().get(cell.getId()).orElseThrow();
        assertTrue(loaded.isOwner(owner));
        assertTrue(loaded.isPermitted(member));
        assertTrue(stores.getCellStore().getCache().getCached(cell.getId()).isRented());
    }

}
