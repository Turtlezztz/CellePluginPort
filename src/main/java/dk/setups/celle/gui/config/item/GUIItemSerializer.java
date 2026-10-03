package dk.setups.celle.gui.config.item;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.*;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class GUIItemSerializer implements ObjectSerializer<ConfigGUIItem> {
    @Override public boolean supports(@NonNull Class<? super ConfigGUIItem> type) { return type.equals(ConfigGUIItem.class); }
    @Override public void serialize(@NonNull ConfigGUIItem item, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.add("item", item.getItem());
        data.add("slots", item.getSlots());
    }
    @Override public ConfigGUIItem deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        ItemStack item = data.get("item", ItemStack.class);
        List<?> values = data.get("slots", List.class);
        if (values == null) throw new IllegalArgumentException("Missing GUI slots");
        List<Integer> slots = values.stream().map(value -> {
            if (!(value instanceof Number number) || number.doubleValue() != number.intValue() || number.intValue() < 0) {
                throw new IllegalArgumentException("Invalid GUI slot: " + value);
            }
            return number.intValue();
        }).toList();
        return new ConfigGUIItem(slots, item);
    }
}
