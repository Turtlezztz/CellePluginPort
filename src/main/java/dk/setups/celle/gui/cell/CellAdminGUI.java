package dk.setups.celle.gui.cell;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiAction;
import dk.setups.celle.cell.log.CellLogFilter;
import dk.setups.celle.cell.log.CellLogFilterBuilder;
import dk.setups.celle.command.CellAdminCommand;
import dk.setups.celle.gui.ConfigGuiItemBuilder;
import dk.setups.celle.gui.ConfigurableGUI;
import dk.setups.celle.gui.GUIPlaceholderUtils;
import dk.setups.celle.gui.cell.logs.CellLogsGUI;
import dk.setups.celle.gui.cell.logs.CellLogsGUIState;
import dk.setups.celle.gui.config.GUISerdesPack;
import dk.setups.celle.gui.config.item.ConfigGUIItem;
import dk.setups.celle.gui.config.item.ItemMapBuilder;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Configuration;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

@Getter
@SuppressWarnings({"FieldMayBeFinal", "deprecation"})
@Configuration(path = "guis/celladmin.yml", provider = YamlSnakeYamlConfigurer.class, serdes = GUISerdesPack.class)
public class CellAdminGUI extends ConfigurableGUI<CellGUIState> {

    private String title = "Cell Admin";
    private int rows = 6;
    private LinkedHashMap<String, ConfigGUIItem> items = new ItemMapBuilder()
            .addItem("decoration", new ConfigGuiItemBuilder()
                    .setItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE))
                    .setSlots(IntStream.range(0, 9), IntStream.range(45, 54))
                    .build())
            .addItem("decoration2", new ConfigGuiItemBuilder()
                    .setItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
                    .setSlots(IntStream.range(9, 45))
                    .build())
            .addItem("delete", new ConfigGuiItemBuilder()
                    .setItem(ItemBuilder.from(Material.BARRIER)
                            .setName("§4§lDELETE CELL")
                            .setLore("",
                                    " §7§l» §cClick to delete §4{cell.name}§c.")
                            .build())
                    .setSlot(2, 1)
                    .build())
            .addItem("unrent", new ConfigGuiItemBuilder()
                    .setItem(ItemBuilder.from(Material.REDSTONE_BLOCK)
                            .setName("§4§lUNRENT")
                            .setLore("",
                                    " §7§l» §cClick to end the rental of §4{cell.name}§c.")
                            .build())
                    .setSlot(2, 4)
                    .build())
            .addItem("extend", new ConfigGuiItemBuilder()
                    .setItem(ItemBuilder.from(Material.OAK_SIGN)
                            .setName("§6§lEXTEND")
                            .setLore("",
                                    " §7§l» §aClick to extend the rental of §2{cell.name}§a.")
                            .build()
                    )
                    .setSlot(2, 7)
                    .build())
            .addItem("info", new ConfigGuiItemBuilder()
                    .setItem(ItemBuilder.from(Material.WRITABLE_BOOK)
                            .setName("§6§lINFO")
                            .setLore("§6§lINFORMATION",
                                    "",
                                    " §7§l» §aName: §2{cell.name}",
                                    " §7§l» §aOwner: §2{cell.owner.name}",
                                    " §7§l» §aRented until: §2{cell.rent.format-long}",
                                    " §7§l» §aTime remaining: §2{cell.rent.left-long}",
                                    " §7§l» §aPrice: §2{cell.group.price.format-long}",
                                    " §7§l» §aGroup: §2{cell:group.name}",
                                    " §7§l» §aRegion: §2{cell:region.name}",
                                    "")
                            .build())
                    .setSlot(4, 3)
                    .build())
            .addItem("logs", new ConfigGuiItemBuilder()
                    .setItem(ItemBuilder.from(Material.PAPER)
                            .setName("§6§lLOGS")
                            .setLore("§6§lLOGS",
                                    "",
                                    " §7§l» §aClick to view the logs for §2{cell.name}§a.",
                                    "")
                            .build())
                    .setSlot(4, 5)
                    .build())
            .build();

    private transient @Inject CellAdminCommand adminCommand;
    private transient @Inject GUIPlaceholderUtils placeholderUtils;
    private transient @Inject CellLogsGUI logs;

    @Override
    public void addClickEvents(Map<String, GuiAction<InventoryClickEvent>> events, CellGUIState state) {
        events.put("delete", event -> adminCommand.deleteCell(state.getPlayer(), state.getCell()));
        events.put("unrent", event -> adminCommand.unrentCell(state.getPlayer(), state.getCell()));
        events.put("extend", event -> adminCommand.extendCell(state.getPlayer(), state.getCell()));
        events.put("logs", event -> {
            CellLogFilter filter = new CellLogFilterBuilder().cell(state.getCell()).build();
            CellLogsGUIState logsState = new CellLogsGUIState(state.getPlayer(), filter);

            logs.create(new CellLogsGUIState(state.getPlayer(), filter)).open(state.getPlayer());
        });
    }
}