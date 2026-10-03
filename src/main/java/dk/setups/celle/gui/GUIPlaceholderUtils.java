package dk.setups.celle.gui;

import dk.setups.celle.gui.state.GUIState;
import dk.setups.celle.gui.state.GUIStatePlaceholderUtils;
import eu.okaeri.i18n.minecraft.adventure.AdventureMessage;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.placeholders.Placeholders;
import eu.okaeri.placeholders.message.CompiledMessage;
import eu.okaeri.platform.core.annotation.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GUIPlaceholderUtils {

    private @Inject GUIStatePlaceholderUtils guiStateUtils;
    private @Inject Placeholders placeholders;

    public ItemStack withPlaceholders(GUIState state, Map<String, Object> placeholders, ItemStack item) {
        Map<String, Object> all = new HashMap<>(guiStateUtils.getPlaceholderValues(state));
        all.putAll(placeholders);
        return withPlaceholders(all, item);
    }

    public net.kyori.adventure.text.Component withPlaceholders(GUIState state, String text) {
        return withPlaceholders(guiStateUtils.getPlaceholderValues(state), text);
    }

    public ItemStack withPlaceholders(GUIState state, ItemStack item) {
        return withPlaceholders(guiStateUtils.getPlaceholderValues(state), item);
    }

    public List<net.kyori.adventure.text.Component> withPlaceholders(GUIState state, List<String> text) {
        return withPlaceholders(guiStateUtils.getPlaceholderValues(state), text);
    }

    public ItemStack withPlaceholders(Map<String, Object> placeholders, ItemStack item) {
        ItemStack result = item.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return result;
        if (meta.hasDisplayName()) meta.displayName(withPlaceholders(placeholders, meta.getDisplayName()));
        if (meta.hasLore()) meta.lore(withPlaceholders(placeholders, meta.getLore()));
        result.setItemMeta(meta);
        return result;
    }

    public List<net.kyori.adventure.text.Component> withPlaceholders(Map<String, Object> placeholders, List<String> text) {
        return text.stream().map(line -> withPlaceholders(placeholders, line)).collect(Collectors.toList());
    }

    public net.kyori.adventure.text.Component withPlaceholders(Map<String, Object> placeholders, String text) {
        return AdventureMessage.of(this.placeholders, CompiledMessage.of(text))
                .with(placeholders)
                .component();
    }

    /*protected <T> PlaceholderResolver<T> getResolver(Map.Entry<String, T> entry) {
        return placeholdersFactory.getResolver(entry.getValue(), entry.getKey());
    }*/
}
