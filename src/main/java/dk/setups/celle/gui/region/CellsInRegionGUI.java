package dk.setups.celle.gui.region;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.GuiItem;
import dk.setups.celle.cell.Cell;
import dk.setups.celle.database.StoreManager;
import dk.setups.celle.gui.ConfigGuiItemBuilder;
import dk.setups.celle.gui.ConfigurableGUI;
import dk.setups.celle.gui.GUIPlaceholderUtils;
import dk.setups.celle.gui.config.GUISerdesPack;
import dk.setups.celle.gui.config.item.ConfigGUIItem;
import dk.setups.celle.gui.config.item.ItemMapBuilder;
import dk.setups.celle.gui.state.GUIState;
import dk.setups.celle.util.WorldGuardUtils;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Configuration;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Configuration(path = "guis/cellsinregion.yml", provider = YamlSnakeYamlConfigurer.class, serdes = GUISerdesPack.class)
@SuppressWarnings({"FieldMayBeFinal", "deprecation"})
public class CellsInRegionGUI extends ConfigurableGUI<CellsInRegionGUIState> {

    @Getter
    private String title = "Cells in {region.name}";
    @Getter
    private int rows = 6;
    @Getter
    private LinkedHashMap<String, ConfigGUIItem> items = new ItemMapBuilder()
            .addItem("decoration", new ConfigGuiItemBuilder()
                    .setItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE))
                    .setSlots(IntStream.range(0, 9), IntStream.range(45, 54))
                    .build())
            .addItem("nextpage", new ConfigGuiItemBuilder().setItem(ItemBuilder.from(Material.ARROW).setName("§aNext page").build()).setSlot(5, 6).build())
            .addItem("prevpage", new ConfigGuiItemBuilder().setItem(ItemBuilder.from(Material.ARROW).setName("§aPrevious page").build()).setSlot(5, 2).build())
            .build();

    private List<Integer> cellItemSlots = IntStream.range(9, 45).boxed().collect(Collectors.toList());
    private ItemStack cellItem = ItemBuilder.from(Material.IRON_DOOR)
            .setName("§7{cell.name}")
            .setLore(" §7§l» §7Price: §a${cell.group.price.format-long}")
            .build();

    private transient @Inject StoreManager store;
    @Getter
    private transient @Inject GUIPlaceholderUtils placeholderUtils;
    private transient @Inject WorldGuardUtils worldGuard;



    @Override
    protected void addItems(CellsInRegionGUIState state, BaseGui gui) {
        super.addItems(state, gui);
        Collection<ProtectedRegion> childRegions = worldGuard.getRegionsIn(state.getPlayer().getWorld(), state.getRegion());
        List<Cell> unrented = store.getCellStore().getCellsInRegions(childRegions, state.getPlayer().getWorld().getName())
                .stream()
                .filter(c -> !c.isRented())
                .sorted(Comparator.comparing(Cell::getName))
                .collect(Collectors.toList());

        if (cellItemSlots.isEmpty()) return;
        state.setPaginationProvider(new dk.setups.celle.gui.pagination.PaginationProvider<>(gui,
                (page, size) -> {
                    int start = Math.min(unrented.size(), Math.multiplyExact(page - 1, size));
                    return unrented.subList(start, Math.min(unrented.size(), start + size));
                }, cellItemSlots, cell -> new GuiItem(cell.map(value -> createCellItem(value, state)).orElseGet(() -> new ItemStack(Material.AIR)))));
        state.getPaginationProvider().update();
    }

    protected ItemStack createCellItem(Cell cell, GUIState state) {
        return placeholderUtils.withPlaceholders(state, Collections.singletonMap("cell", cell), cellItem.clone());
    }

    @Override
    public void addClickEvents(Map<String, GuiAction<InventoryClickEvent>> events, CellsInRegionGUIState state) {
        events.put("nextpage", event -> changePage(state, 1));
        events.put("prevpage", event -> changePage(state, -1));
    }
    private void changePage(CellsInRegionGUIState state, int offset) {
        state.setPage(Math.max(1, state.getPage() + offset));
        if (state.getPaginationProvider() != null) state.getPaginationProvider().setPage(state.getPage());
    }
}
