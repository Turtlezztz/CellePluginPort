package dk.setups.celle.command;

import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.setups.celle.CellePlugin;
import dk.setups.celle.cell.*;
import dk.setups.celle.cell.log.CellLogFilter;
import dk.setups.celle.cell.log.CellLogFilterBuilder;
import dk.setups.celle.config.Config;
import dk.setups.celle.config.DefaultConfig;
import dk.setups.celle.config.LangConfig;
import dk.setups.celle.database.StoreManager;
import dk.setups.celle.gui.cell.logs.CellLogsGUI;
import dk.setups.celle.gui.cell.logs.CellLogsGUIState;
import dk.setups.celle.sign.AvailableCellsGUISign;
import dk.setups.celle.sign.CellSign;
import dk.setups.celle.util.WorldEditUtils;
import dk.setups.celle.util.cell.CellFactory;
import dk.setups.celle.util.cell.CellUtils;
import dk.setups.celle.util.WorldGuardUtils;
import eu.okaeri.commands.annotation.*;
import eu.okaeri.commands.bukkit.annotation.Sync;
import eu.okaeri.commands.bukkit.annotation.Permission;
import eu.okaeri.commands.service.CommandService;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.placeholders.context.PlaceholderContext;
import eu.okaeri.placeholders.message.CompiledMessage;
import eu.okaeri.platform.bukkit.i18n.BI18n;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Sync
@Command(label = "#{commandCeaLabel}", description = "${commandCeaDescription}", aliases = {"#{commandCeaAlias}"})
@Permission("cell.admin")
public class CellAdminCommand implements CommandService {

    private @Inject("storeManager") StoreManager stores;
    private @Inject("lang") BI18n i18n;
    private @Inject Config config;
    private @Inject LangConfig lang;
    private @Inject WorldEditUtils worldEdit;
    private @Inject WorldGuardUtils worldGuard;
    private @Inject CellFactory factory;
    private @Inject Plugin plugin;
    private @Inject CellUtils utils;
    private @Inject CellLogsGUI logsGUI;
    private @Inject DefaultConfig defaults;
    private @Inject Placeholders placeholders;

    @Executor(pattern = {"#{commandCeaCreateAutoAlias}"}, description = "${commandCeaCreateAutoDescription}", usage = "${commandCeaCreateAutoUsage}")
    public void createAuto(@Context Player executor, @Arg CellGroup group, @Arg String name) {
        Block target = executor.getTargetBlockExact(5);
        if (target == null || !(target.getState() instanceof org.bukkit.block.Sign)) {
            i18n.get(lang.getCommandCeaCreateAutoNotLookingAtSign()).sendTo(executor);
            return;
        }

        Optional<CuboidRegion> selection = worldEdit.getSelection(executor);
        if (!selection.isPresent()) {
            i18n.get(lang.getCommandCeaCreateAutoNoSelection())
                    .with("region", name)
                    .sendTo(executor);
            return;
        }
        try {
            if (stores.getCellStore().getFromName(name).isPresent()) {
                i18n.get(lang.getCommandCeaCreateCellAlreadyExists()).with("name", name).sendTo(executor);
                return;
            }
            ProtectedRegion region = worldGuard.create(name, selection.get());
            Cell cell;
            try {
                cell = newCell(executor, group, region, name);
            } catch (RuntimeException failure) {
                worldGuard.delete(new CellRegion(region.getId(), executor.getWorld().getName()));
                throw failure;
            }
            cell.setSign(new CellSign(target.getLocation()));
            Location loc = executor.getLocation();
            cell.setTeleport(new CellTeleport(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getYaw(), loc.getPitch(), loc.getWorld().getName()));
            stores.getTeleportStore().persist(cell.getTeleport());
            stores.getSignStore().persist(cell.getSign());
            utils.updateAndSave(cell);
            i18n.get(lang.getCommandCeaCreateCellCreated()).with("cell", cell).sendTo(executor);
        } catch(IllegalArgumentException ex) {
            i18n.get(lang.getCommandCeaCreateAutoRegionAlreadyExists())
                    .with("region", name)
                    .sendTo(executor);
        }
    }

    @Executor(pattern = "#{commandCeaCreateCellAlias}", description = "${commandCeaCreateCellDescription}", usage = "${commandCeaCreateCellUsage}")
    @Sync
    public void createCell(@Context Player executor, @Arg CellGroup group, @Arg ProtectedRegion region, @Arg String name) {
        if (stores.getCellStore().getFromName(name).isPresent()) {
            i18n.get(lang.getCommandCeaCreateCellAlreadyExists()).with("name", name).sendTo(executor);
            return;
        }
        Cell cell = newCell(executor, group, region, name);
        utils.update(cell);
        i18n.get(lang.getCommandCeaCreateCellCreated()).with("cell", cell).sendTo(executor);
    }

    private Cell newCell(Player executor, CellGroup group, ProtectedRegion region, String name) {
        if (stores.getCellStore().getFromRegion(region.getId(), executor.getWorld().getName()).isPresent()) {
            throw new IllegalArgumentException("Region is already assigned to a cell");
        }
        var stored = stores.getRegionStore().get(region.getId());
        if (stored.isPresent() && !stored.get().getRegionWorld().equals(executor.getWorld().getName())) {
            throw new IllegalArgumentException("Region names must be unique across worlds in the existing database schema");
        }
        return stores.transaction(() -> {
            CellRegion cellRegion = new CellRegion(region.getId(), executor.getWorld().getName());
            stores.getRegionStore().persist(cellRegion);
            Cell cell = new Cell(name, group, cellRegion);
            stores.getCellStore().persist(cell);
            return stores.getCellStore().get(cell.getId()).orElseThrow();
        });
    }

    @Executor(pattern = "#{commandCeaCellSetSignAlias}", description = "${commandCeaCellSetSignDescription}", usage = "${commandCeaCellSetSignUsage}")
    @Completion(arg = "cell", value = "@cells")
    @Sync
    public void setSign(@Context Player player, @Arg Cell cell) {
        Block target = player.getTargetBlockExact(5);
        if (target == null || !(target.getState() instanceof org.bukkit.block.Sign)) {
            i18n.get(lang.getCommandCeaCellSetSignNotLookingAtSign()).sendTo(player);
            return;
        }
        var assigned = stores.getCellStore().getFromSignLoc(target.getX(), target.getY(), target.getZ(), target.getWorld().getName());
        if (assigned.isPresent() && assigned.get().getId() != cell.getId()) throw new IllegalArgumentException("Sign is assigned to another cell");
        if (stores.getAvailableCellsGuiSignStore().getSign(target.getLocation()).isPresent()) throw new IllegalArgumentException("Sign is assigned to a cell list");
        CellSign sign = new CellSign(target.getLocation());
        if (cell.getSign() != null) sign.setId(cell.getSign().getId());
        stores.getSignStore().persist(sign);
        cell.setSign(sign);
        utils.updateAndSave(cell);
        i18n.get(lang.getCommandCeaCellSetSignSuccess()).with("cell", cell).sendTo(player);
    }

    @Executor(pattern = "#{commandCeaCellSetTeleportAlias}", description = "${commandCeaCellSetTeleportDescription}", usage = "${commandCeaCellSetTeleportUsage}")
    @Completion(arg = "cell", value = "@cells")
    public void setTeleport(@Context Player player, @Arg Cell cell) {
        Location location = player.getLocation();
        CellTeleport previous = cell.getTeleport();
        CellTeleport teleport = new CellTeleport(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), location.getPitch(), location.getWorld().getName());
        if (previous != null) teleport.setId(previous.getId());
        stores.transaction(() -> {
            stores.getTeleportStore().persist(teleport);
            cell.setTeleport(teleport);
            stores.getCellStore().persist(cell);
            return null;
        });
        utils.update(cell);
        i18n.get(lang.getCommandCeaCellSetTeleportSuccess()).with("x", location.getX()).with("y", location.getY()).with("z", location.getZ()).sendTo(player);
    }

    @Executor(pattern = "#{commandCeaCellDeleteSignAlias}", description = "${commandCeaCellDeleteSignDescription}", usage = "${commandCeaCellDeleteSignUsage}")
    @Sync
    public void deleteSign(@Context Player player) {
        Block lookingAt = player.getTargetBlockExact(5);
        if(lookingAt == null || !(lookingAt.getState() instanceof org.bukkit.block.Sign)) {
            i18n.get(lang.getCommandCeaCellDeleteSignNotLookingAtSign()).sendTo(player);
            return;
        }

        if(stores.getSignStore().delete(lookingAt.getX(), lookingAt.getY(), lookingAt.getZ(),
                lookingAt.getWorld().getName())) {
            i18n.get(lang.getCommandCeaCellDeleteSignSuccess()).sendTo(player);
            return;
        }
        i18n.get(lang.getCommandCeaCellDeleteSignFailure()).sendTo(player);
    }



    @Executor(pattern = "#{commandCeaCreateGroupAlias}", description = "${commandCeaCreateGroupDescription}", usage = "${commandCeaCreateGroupUsage}")
    @Sync
    public void createGroup(@Context CommandSender executor, @Arg String name) {
         if(stores.getGroupStore().getFromName(name).isPresent()) {
             i18n.get(lang.getCommandCeaCreateGroupAlreadyExists())
                     .with("group", stores.getGroupStore().getFromName(name))
                     .sendTo(executor);
             return;
         }
         CellGroup group = factory.createGroup(name);
         stores.getGroupStore().persist(group);
        stores.getCellStore().updateCache();

         i18n.get(lang.getCommandCeaCreateGroupCreated()).with("group", group).sendTo(executor);
    }

    @Executor(pattern = "#{commandCeaDeleteCellAlias}" , description = "${commandCeaDeleteCellDescription}", usage = "${commandCeaDeleteCellUsage}")
    @Sync
    public void deleteCell(@Context CommandSender sender, @Arg Cell cell) {
        stores.getCellStore().delete(cell.getId());
        worldGuard.delete(cell.getRegion());
        i18n.get(lang.getCommandCeaDeleteCellSuccess()).with("cell", cell).sendTo(sender);
    }

    @Executor(pattern = "#{commandCeaUnrentAlias}", description = "${commandCeaUnrentDescription}", usage = "${commandCeaUnrentUsage}")
    @Sync
    public void unrentCell(@Context CommandSender sender, @Arg Cell cell) {
        cell.unrent();
        utils.updateAndSave(cell);
        i18n.get(lang.getCommandCeaUnrentSuccess()).with("cell", cell).sendTo(sender);
    }

    @Executor(pattern = "#{commandCeaUnrentAllAlias}", description = "${commandCeaUnrentAllDescription}", usage = "${commandCeaUnrentAllUsage}")
    @Sync
    public void unrentAllCells(@Context CommandSender sender, @Arg CellUser user) {
        Collection<Cell> cells = stores.getCellStore().getOwnedCells(user);
        for(Cell cell : cells) {
            cell.unrent();
            utils.updateAndSave(cell);
        }
        Collection<Cell> permittedCells = stores.getCellStore().getPermittedCells(user);
        for(Cell cell : permittedCells) {
            cell.removeMember(user);
            utils.updateAndSave(cell);
        }

        i18n.get(lang.getCommandCeaUnrentAllSuccess())
                .with("count", cells.size())
                .with("player", user.getName())
                .sendTo(sender);
    }

    @Executor(pattern = "#{commandCeaExtendCellAlias}", description = "${commandCeaExtendCellDescription}", usage = "${commandCeaExtendCellUsage}")
    @Sync
    public void extendCell(@Context CommandSender sender, @Arg Cell cell) {
        if(!cell.isRented()) {
            i18n.get(lang.getCommandCeaExtendCellNotRented())
                    .with("cell", cell)
                    .sendTo(sender);
            return;
        }
        cell.extend();
        utils.updateAndSave(cell);
        i18n.get(lang.getCommandCeaExtendCellSuccess())
                .with("cell", cell)
                .sendTo(sender);
    }

    @Executor(pattern = "#{commandCeaDeleteGroupAlias}", description = "${commandCeaDeleteGroupDescription}", usage = "${commandCeaDeleteGroupUsage}")
    @Sync
    public void deleteGroup(@Context CommandSender sender, @Arg CellGroup group, @Arg CellGroup newGroup) throws Exception {
        if (group.getId() == newGroup.getId()) throw new IllegalArgumentException("Replacement group must be different");
        Set<Cell> cellsInGroup = new HashSet<>(group.getCells());
        i18n.get(lang.getCommandCeaDeleteGroupFoundCellsInGroup())
                .with("count", cellsInGroup.size())
                .sendTo(sender);

        stores.getCellStore().getDao().callBatchTasks(() -> {
            cellsInGroup.forEach(cell -> {
                cell.setGroup(newGroup);
                stores.getCellStore().persist(cell);
            });
            return null;
        });

        stores.getGroupStore().delete(group.getId());
        i18n.get(lang.getCommandCeaDeleteCellSuccess())
                .with("name", group.getName())
                .sendTo(sender);

        i18n.get(lang.getCommandCeaDeleteGroupSuccess())
                .with("group", group)
                .with("newGroup", newGroup)
                .with("count", cellsInGroup.size())
                .sendTo(sender);
    }

    @Executor(pattern = "#{commandCeaLogsPlayerAlias}", description = "${commandCeaLogsPlayerDescription}", usage = "${commandCeaLogsPlayerUsage}")
    @Sync
    public void logsPlayer(@Context Player player, @Arg CellUser user) {
        CellLogFilter filter = new CellLogFilterBuilder()
                .user(user)
                .targetOrUser(true)
                .build();
        logsGUI.create(new CellLogsGUIState(player, filter)).open(player);
    }

    @Executor(pattern = "#{commandCeaLogsCellAlias}", description = "${commandCeaLogsCellDescription}", usage = "${commandCeaLogsCellUsage}")
    @Sync
    public void logsCell(@Context Player player, @Arg Cell cell) {
        CellLogFilter filter = new CellLogFilterBuilder()
                .cell(cell)
                .build();
        logsGUI.create(new CellLogsGUIState(player,filter)).open(player);
    }

    @Executor(pattern = "#{commandCeaGroupSetSignLineAlias}", description = "${commandCeaGroupSetSignLineDescription}", usage = "${commandCeaGroupSetSignLineUsage}")
    @Completion(arg = "group", value = "@groups")
    @Completion(arg = "state", value = {"unrented", "rented-non-member", "rented-member", "rented-owner"})
    @Completion(arg = "line", value = {"1", "2", "3", "4"})
    @Sync
    public void setSignLine(@Context CommandSender player, @Arg CellGroup group, @Arg String state, @Arg int line, @Arg String text) {
        if(line < 1 || line > 4) {
            i18n.get(lang.getCommandCeaGroupSetSignLineNotCorrectLine()).with("line", line).sendTo(player);
            return;
        }
        switch(state.toLowerCase()) {
            case "unrented":
                group.setUnrentedSignLine(line, text);
                break;
            case "rented-non-member":
                group.setRentedNonMemberSignLine(line, text);
                break;
            case "rented-member":
                group.setRentedMemberSignLine(line, text);
                break;
            case "rented-owner":
                group.setRentedOwnerSignLine(line, text);
                break;
            default:
                i18n.get(lang.getCommandCeaGroupSetSignLineNotCorrectState()).with("state", state).sendTo(player);
                return;
        }
        i18n.get(lang.getCommandCeaGroupSetSignLineSuccess())
                .with("group", group)
                .with("state", state)
                .with("line", line)
                .with("text", text).sendTo(player);
        stores.getGroupStore().persist(group);
        stores.getCellStore().updateCache();
    }


    @Executor(pattern = "#{commandCeaGroupSetRentPriceAlias}", description = "${commandCeaGroupSetRentPriceDescription}", usage = "${commandCeaGroupSetRentPriceUsage}")
    @Completion(arg = "group", value = "@groups")
    public void setRentPrice(@Context CommandSender sender, @Arg CellGroup group, @Arg double price) {
        if (!Double.isFinite(price) || price < 0) throw new IllegalArgumentException("Rent price must be finite and nonnegative");
        group.setRentPrice(price);
        stores.getGroupStore().persist(group);
        stores.getCellStore().updateCache();
        i18n.get(lang.getCommandCeaGroupSetRentPriceSuccess()).with("group", group).with("price", price).sendTo(sender);
    }

    @Executor(pattern = "#{commandCeaGroupSetMaxRentTimeAlias}", description = "${commandCeaGroupSetMaxRentTimeDescription}", usage = "${commandCeaGroupSetMaxRentTimeUsage}")
    @Completion(arg = "group", value = "@groups")
    @Sync
    public void setGroupMaxRentTime(@Context CommandSender player, @Arg CellGroup group, @Arg Duration time) {
        if (time.isZero() || time.isNegative()) throw new IllegalArgumentException("Rent time must be positive");
        group.setMaxRentTimeMillis(time.toMillis());
        stores.getGroupStore().persist(group);
        stores.getCellStore().updateCache();
        i18n.get(lang.getCommandCeaGroupSetMaxRentTimeSuccess())
                .with("group", group)
                .with("time", time).sendTo(player);
    }

    @Executor(pattern = "#{commandCeaGroupSetRentTimeAlias}", description = "${commandCeaGroupSetRentTimeDescription}", usage = "${commandCeaGroupSetRentTimeUsage}")
    @Completion(arg = "group", value = "@groups")
    @Sync
    public void setGroupRentTime(@Context CommandSender player, @Arg CellGroup group, @Arg Duration time) {
        if (time.isZero() || time.isNegative()) throw new IllegalArgumentException("Rent time must be positive");
        group.setRentTimeMillis(time.toMillis());
        stores.getGroupStore().persist(group);
        stores.getCellStore().updateCache();
        i18n.get(lang.getCommandCeaGroupSetRentTimeSuccess())
                .with("group", group)
                .with("time", time).sendTo(player);
    }

    @Executor(pattern = "#{commandCeaGroupSetMaxRentedCellsAlias}", description = "${commandCeaGroupSetMaxRentedCellsDescription}", usage = "${commandCeaGroupSetMaxRentedCellsUsage}")
    @Completion(arg = "group", value = "@groups")
    @Sync
    public void setGroupMaxRentedCells(@Context CommandSender player, @Arg CellGroup group, @Arg int count) {
        if (count < 0) throw new IllegalArgumentException("Cell limit cannot be negative");
        group.setMaxRentedCells(count);
        stores.getGroupStore().persist(group);
        stores.getCellStore().updateCache();
        i18n.get(lang.getCommandCeaGroupSetMaxRentedCellsSuccess())
                .with("group", group)
                .with("count", count).sendTo(player);
    }

    @Executor(pattern = "#{commandCeaGroupInfoAlias}", description = "${commandCeaGroupInfoDescription}", usage = "${commandCeaGroupInfoUsage}")
    @Completion(arg = "group", value = "@groups")
    @Sync
    public void groupInfo(@Context CommandSender player, @Arg CellGroup group) {
        i18n.get(lang.getCommandCeaGroupInfoMessage())
                .with("group", group)
                .sendTo(player);
    }

    @Executor(pattern = "#{commandCeaSignGUICreateAlias}", description = "${commandCeaSignGUICreateDescription}", usage = "${commandCeaSignGUICreateUsage}")
    @Sync
    public void createGUISign(@Context Player player, @Arg String region) {
        Block target = player.getTargetBlockExact(5);
        if(target == null || !(target.getState() instanceof org.bukkit.block.Sign)) {
            i18n.get(lang.getCommandCeaSignGuiCreateNotLookingAtSign()).sendTo(player);
            return;
        }
        Location location = target.getLocation();
        if(stores.getAvailableCellsGuiSignStore().getSign(target.getLocation()).isPresent()) {
            i18n.get(lang.getCommandCeaSignGUICreateAlias()).sendTo(player);
            return;
        }
        if(!worldGuard.getRegionByName(location.getWorld(), region).isPresent()) {
            i18n.get(lang.getCommandCeaSignGuiCreateRegionNotFound())
                    .with("region", region)
                    .sendTo(player);
            return;
        }
        AvailableCellsGUISign sign =
                new AvailableCellsGUISign(location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                        region, location.getWorld().getName());
        stores.getAvailableCellsGuiSignStore().persist(sign);
        i18n.get(lang.getCommandCeaSignGuiCreateSuccess()).sendTo(player);
    }

    @Executor(pattern = "#{commandCeaCommandEachAlias}", description = "${commandCeaCommandEachDescription}", usage = "${commandCeaCommandEachUsage}")
    @Sync
    public void commandEach(@Context Player sender, @Arg String command) {
        if(!sender.isOp()) {
            return;
        }
        CompiledMessage compiledMessage = CompiledMessage.of(command);
        for(Cell cell : stores.getCellStore().getAll()) {
            String parsedCommand = PlaceholderContext.of(placeholders, compiledMessage)
                    .with("cell", cell)
                    .apply();

            i18n.get(lang.getCommandCeaCommandEachSentCommand())
                    .with("command", parsedCommand)
                    .sendTo(sender);

            sender.performCommand(parsedCommand);
        }
    }

    @Executor(pattern = "#{commandCeaTeleportOtherAlias}", description = "${commandCeaTeleportOtherDescription}", usage = "${commandCeaTeleportOtherUsage}")
    @Sync
    public void teleportOther(@Context CommandSender sender, @Arg Player target, @Arg Cell cell) {
        CellTeleport cellTeleport = cell.getTeleport();
        if(cellTeleport == null) {
            i18n.get(lang.getCommandCeaTeleportOtherNoTeleport())
                    .with("cell", cell)
                    .sendTo(sender);
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            target.teleport(cellTeleport.asBukkit());
            i18n.get(lang.getCommandCeaTeleportOtherSuccess())
                    .with("cell", cell)
                    .with("target", target)
                    .sendTo(sender);
        });
    }

    @Executor(pattern = "#{commandCeaSignGUIDeleteAlias}", description = "${commandCeaSignGUIDeleteDescription}", usage = "${commandCeaSignGUIDeleteUsage}")
    @Sync
    public void deleteGUISign(@Context Player player) {
        Block target = player.getTargetBlockExact(5);
        if(target == null || !(target.getState() instanceof org.bukkit.block.Sign)
            || !stores.getAvailableCellsGuiSignStore().getSign(target.getLocation()).isPresent()) {
            i18n.get(lang.getCommandCeaSignGuiDeleteSignNotFound()).sendTo(player);
            return;
        }
        stores.getAvailableCellsGuiSignStore().getSign(target.getLocation()).ifPresent(sign -> {
            stores.getAvailableCellsGuiSignStore().delete(sign.getId());
            i18n.get(lang.getCommandCeaSignGuiDeleteSuccess()).sendTo(player);
        });
    }

    @Executor(pattern = "#{commandCeaReloadAlias}", description = "${commandCeaReloadDescription}", usage = "${commandCeaReloadUsage}")
    @Sync
    public void reload(@Context CommandSender sender) {
        long started = System.currentTimeMillis();

        config.load();
        defaults.load();
        i18n.load();
        stores.getCellStore().updateCache();
        CellePlugin cellePlugin = (CellePlugin) plugin;
        cellePlugin.getInjector().get("cellAdminGUI", dk.setups.celle.gui.cell.CellAdminGUI.class).orElseThrow().load();
        logsGUI.load();
        cellePlugin.getInjector().get("cellsInRegionGUI", dk.setups.celle.gui.region.CellsInRegionGUI.class).orElseThrow().load();
        cellePlugin.setupTasks(cellePlugin.getInjector(), config);

        long elapsed = System.currentTimeMillis() - started;
        i18n.get(lang.getCommandCeaReloadSuccess())
                .with("elapsed", elapsed)
                .sendTo(sender);
    }

}
