package dk.setups.celle.gui;

import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dk.setups.celle.gui.config.GUIConfiguration;
import dk.setups.celle.gui.state.GUIState;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Map;

public abstract class ConfigurableGUI<T extends GUIState> extends GUIConfiguration {

    public BaseGui create(T state) {
        if (getRows() < 1 || getRows() > 6) throw new IllegalArgumentException("GUI rows must be between 1 and 6");
        BaseGui gui = Gui.gui()
                .title(getPlaceholderUtils().withPlaceholders(state, getTitle()))
                .rows(getRows())
                .disableAllInteractions()
                .create();

        addItems(state, gui);
        return gui;
    }

    protected void addItems(T state, BaseGui gui) {
        Map<String, GuiAction<InventoryClickEvent>> clickEvents = new HashMap<>();
        addClickEvents(clickEvents, state);

        getItems().forEach((key, item) -> {
            GuiItem guiItem =
                    new GuiItem(getPlaceholderUtils().withPlaceholders(state, item.getItem()), clickEvents.get(key));

            item.getSlots().forEach(slot -> {
                if (slot < 0 || slot >= getRows() * 9) throw new IllegalArgumentException("Invalid GUI slot: " + slot);
                gui.setItem(slot, guiItem);
            });
        });
    }

    public abstract GUIPlaceholderUtils getPlaceholderUtils();

    public abstract void addClickEvents(Map<String, GuiAction<InventoryClickEvent>> events, T state);

}