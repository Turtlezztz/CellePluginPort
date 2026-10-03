package dk.setups.celle.database;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import dk.setups.celle.cell.*;
import dk.setups.celle.cell.log.CellLog;
import dk.setups.celle.sign.AvailableCellsSign;
import dk.setups.celle.sign.AvailableCellsGUISign;
import dk.setups.celle.sign.CellSign;
import eu.okaeri.injector.annotation.PostConstruct;
import eu.okaeri.platform.core.annotation.Component;
import lombok.Getter;

import org.bukkit.plugin.Plugin;
import java.nio.file.Files;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@Component
public class StoreManager {

    @Getter
    private CellStore cellStore;
    @Getter
    private UserStore userStore;
    @Getter
    private CellGroupStore groupStore;
    @Getter
    private CellMemberStore memberStore;
    @Getter
    private CellSignStore signStore;
    @Getter
    private RegionStore regionStore;
    @Getter
    private CellTeleportStore teleportStore;
    @Getter
    private CellLogStore logStore;
    @Getter
    private AvailableCellSignStore availableCellsSignStore;
    @Getter
    private AvailableCellGUISignStore availableCellsGuiSignStore;

    private ConnectionSource connectionSource;

    public StoreManager() {
    }

    @PostConstruct
    public void init(Logger logger, Plugin plugin) throws Exception {
        com.j256.ormlite.logger.Logger.setGlobalLogLevel(Level.ERROR);
        LoggerFactory.setLogBackendType(LogBackendType.JAVA_UTIL);
        try {
            Files.createDirectories(plugin.getDataFolder().toPath());
            connectionSource = new JdbcConnectionSource("jdbc:sqlite:" + plugin.getDataFolder().toPath().resolve("cell.db").toAbsolutePath());

            Set<String> tableNames = getCreatedTables();
            if (!tableNames.contains(CellUser.TABLE_NAME)) {
                TableUtils.createTable(connectionSource, CellUser.class);
            }
            if (!tableNames.contains(CellRegion.TABLE_NAME)) {
                TableUtils.createTable(connectionSource, CellRegion.class);
            }
            if (!tableNames.contains(CellSign.TABLE_NAME)) {
                TableUtils.createTable(connectionSource, CellSign.class);
            }
            if (!tableNames.contains(CellGroup.TABLE_NAME)) {
                TableUtils.createTable(connectionSource, CellGroup.class);
            }
            if (!tableNames.contains(Cell.TABLE_NAME)) {
                TableUtils.createTable(connectionSource, Cell.class);
            }
            if (!tableNames.contains(CellMember.TABLE_NAME)) {
                TableUtils.createTable(connectionSource, CellMember.class);
            }
            if(!tableNames.contains(CellTeleport.TABLE_NAME)) {
                TableUtils.createTable(connectionSource, CellTeleport.class);
            }
            if(!tableNames.contains(CellLog.TABLE_NAME)) {
                TableUtils.createTable(connectionSource, CellLog.class);
            }
            if(!tableNames.contains(AvailableCellsSign.TABLE_NAME)) {
                TableUtils.createTable(connectionSource, AvailableCellsSign.class);
            }
            if(!tableNames.contains(AvailableCellsGUISign.TABLE_NAME)) {
                TableUtils.createTable(connectionSource, AvailableCellsGUISign.class);
            }
        } catch(Exception ex) {
            logger.severe("========================================");
            logger.severe("=                                      =");
            logger.severe("=   DATABASE CONNECTION FAILURE        =");
            logger.severe("=                                      =");
            logger.severe("=   Failed to establish connection     =");
            logger.severe("=   with the database.                 =");
            logger.severe("=                                      =");
            logger.severe("=   Please check your configuration.   =");
            logger.severe("=                                      =");
            logger.severe("========================================");
            throw ex;
        }

        this.cellStore = new CellStore(DaoManager.createDao(connectionSource, Cell.class), this, logger);
        this.userStore = new UserStore(DaoManager.createDao(connectionSource, CellUser.class), this, logger);
        this.groupStore = new CellGroupStore(DaoManager.createDao(connectionSource, CellGroup.class), this, logger);
        this.memberStore = new CellMemberStore(DaoManager.createDao(connectionSource, CellMember.class), this, logger);
        this.signStore = new CellSignStore(DaoManager.createDao(connectionSource, CellSign.class), this, logger);
        this.regionStore = new RegionStore(DaoManager.createDao(connectionSource, CellRegion.class), this, logger);
        this.teleportStore = new CellTeleportStore(DaoManager.createDao(connectionSource, CellTeleport.class), this, logger);
        this.logStore = new CellLogStore(DaoManager.createDao(connectionSource, CellLog.class), this, logger);
        this.availableCellsSignStore = new AvailableCellSignStore(DaoManager.createDao(connectionSource, AvailableCellsSign.class), this, logger);
        this.availableCellsGuiSignStore = new AvailableCellGUISignStore(DaoManager.createDao(connectionSource, AvailableCellsGUISign.class), this, logger);
    }

    private Set<String> getCreatedTables() throws SQLException {
        var connection = connectionSource.getReadWriteConnection("");
        try {
            DatabaseMetaData metaData = connection.getUnderlyingConnection().getMetaData();
            Set<String> names = new HashSet<>();
            try (ResultSet tables = metaData.getTables(null, null, "%", null)) {
                while (tables.next()) names.add(tables.getString(3).toLowerCase(java.util.Locale.ROOT));
            }
            return names;
        } finally {
            connectionSource.releaseConnection(connection);
        }
    }

    private static boolean isSqlite(String jdbc) {
        return jdbc.startsWith("jdbc:sqlite:");
    }

    public <T> T transaction(java.util.concurrent.Callable<T> task) {
        try {
            return com.j256.ormlite.misc.TransactionManager.callInTransaction(connectionSource, task);
        } catch (SQLException exception) {
            throw new IllegalStateException("Database transaction failed", exception);
        }
    }

    public void disconnect() throws Exception {
        if (connectionSource != null) connectionSource.close();
    }

}