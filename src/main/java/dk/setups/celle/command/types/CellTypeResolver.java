package dk.setups.celle.command.types;

import dk.setups.celle.cell.Cell;
import dk.setups.celle.database.StoreManager;
import dk.setups.celle.util.WorldGuardUtils;
import eu.okaeri.commands.meta.ArgumentMeta;
import eu.okaeri.commands.service.CommandData;
import eu.okaeri.commands.service.Invocation;
import eu.okaeri.commands.type.resolver.BasicTypeResolver;
import eu.okaeri.injector.annotation.Inject;
import lombok.NonNull;


public class CellTypeResolver extends BasicTypeResolver<Cell> {

    private @Inject StoreManager stores;
    private @Inject WorldGuardUtils worldGuard;

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return type.equals(Cell.class);
    }

    @Override
    public Cell resolve(@NonNull Invocation invocation, @NonNull CommandData data, @NonNull ArgumentMeta argumentMeta, @NonNull String text) {
        return stores.getCellStore().getFromName(text).orElse(null);
    }
}
