package dk.setups.celle.util.cell;

import dk.setups.celle.cell.Cell;
import dk.setups.celle.config.LangConfig;
import dk.setups.celle.database.StoreManager;
import dk.setups.celle.util.VaultUtils;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.bukkit.i18n.BI18n;
import eu.okaeri.platform.core.annotation.Component;
import org.bukkit.entity.Player;

/** Runs as one server-thread operation so a second click cannot race a payment. */
@Component
public class CellRentManager {
    private @Inject("lang") BI18n i18n;
    private @Inject LangConfig lang;
    private @Inject StoreManager stores;
    private @Inject CellUtils utils;
    private @Inject CellAPI api;
    private @Inject VaultUtils vault;

    public void handleCellUse(Player player, Cell requested) {
        Cell cell = stores.getCellStore().get(requested.getId()).orElse(null);
        if (cell == null) return;
        if (cell.isOwner(player.getUniqueId())) attemptExtendCell(player, cell);
        else attemptRentCell(player, cell);
    }

    public void attemptRentCell(Player player, Cell cell) {
        if (cell.isRented()) { message(player, cell, lang.getCellAttemptRentAlreadyRented()); return; }
        // Clean up a stale lease before assigning it to someone else.
        if (cell.getRentedUntil() != null) api.expireCell(cell);
        if (!player.hasPermission(cell.getGroup().getRentPermission()) && !player.hasPermission(cell.getRentPermission())) {
            message(player, cell, lang.getCellAttemptRentNoPermission()); return;
        }
        if (utils.hasRentedMax(player, cell.getGroup())) { message(player, cell, lang.getCellAttemptRentMaxRented()); return; }
        double price = cell.getGroup().getRentPrice();
        if (!vault.tryTakeMoney(player, price)) { message(player, cell, lang.getCellAttemptRentNotEnoughMoney()); return; }
        EventSuccess result;
        try {
            result = api.rentCell(cell, stores.getUserStore().get(player));
        } catch (RuntimeException failure) {
            vault.addMoney(player, price);
            throw failure;
        }
        if (result != EventSuccess.SUCCESS) {
            vault.addMoney(player, price);
            if (result == EventSuccess.FAILED) message(player, cell, lang.getCellAttemptRentAlreadyRented());
            return;
        }
        message(player, cell, lang.getCellAttemptRentSuccess());
    }

    public void attemptExtendCell(Player player, Cell cell) {
        if (!cell.isOwner(player.getUniqueId())) { message(player, cell, lang.getCellAttemptExtendNotOwned()); return; }
        if (!player.hasPermission(cell.getGroup().getRentPermission()) && !player.hasPermission(cell.getRentPermission())
                && !player.hasPermission(cell.getGroup().getExtendPermission())) {
            message(player, cell, lang.getCellAttemptExtendNoPermission()); return;
        }
        if (!cell.canExtend()) { message(player, cell, lang.getCellAttemptExtendFullyExtended()); return; }
        double price = cell.getGroup().getRentPrice();
        if (!vault.tryTakeMoney(player, price)) { message(player, cell, lang.getCellAttemptExtendNotEnoughMoney()); return; }
        EventSuccess result;
        try {
            result = api.extendCell(cell, stores.getUserStore().get(player));
        } catch (RuntimeException failure) {
            vault.addMoney(player, price);
            throw failure;
        }
        if (result != EventSuccess.SUCCESS) { vault.addMoney(player, price); return; }
        message(player, cell, lang.getCellAttemptExtendSuccess());
    }

    private void message(Player player, Cell cell, String text) {
        i18n.get(text).with("cell", cell).sendTo(player);
    }
}
