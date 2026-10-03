package dk.setups.celle.gui.config.item;

import com.destroystokyo.paper.profile.ProfileProperty;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.*;
import lombok.NonNull;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class ItemStackSerializer implements ObjectSerializer<ItemStack> {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    @Override
    public boolean supports(@NonNull Class<? super ItemStack> type) { return ItemStack.class.isAssignableFrom(type); }

    @Override
    public void serialize(@NonNull ItemStack item, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.add("material", item.getType().name());
        if (item.getAmount() != 1) data.add("amount", item.getAmount());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (meta.hasDisplayName()) data.add("displayName", LEGACY.serialize(meta.displayName()));
        if (meta.hasLore()) data.add("lore", meta.lore().stream().map(LEGACY::serialize).toList());
        if (meta instanceof Damageable damage && damage.hasDamage()) data.add("damage", damage.getDamage());
        if (meta.hasEnchants()) {
            Map<String, Integer> enchants = new LinkedHashMap<>();
            meta.getEnchants().forEach((enchant, level) -> enchants.put(enchant.getKey().toString(), level));
            data.add("enchants", enchants);
        }
        if (!meta.getItemFlags().isEmpty()) data.add("itemFlags", meta.getItemFlags().stream().map(Enum::name).toList());
        if (meta instanceof SkullMeta skull && skull.getPlayerProfile() != null) {
            skull.getPlayerProfile().getProperties().stream().filter(property -> property.getName().equals("textures"))
                    .findFirst().ifPresent(property -> data.add("texture", property.getValue()));
        }
    }

    @Override
    public ItemStack deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String name = data.containsKey("material") ? data.get("material", String.class) : "PLAYER_HEAD";
        int legacyData = integer(data, "durability", 0);
        ItemStack item = new ItemStack(LegacyMaterialMigration.resolve(name, legacyData), integer(data, "amount", 1));
        if (item.getAmount() < 1 || item.getAmount() > item.getMaxStackSize()) throw new IllegalArgumentException("Invalid item amount");
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        if (data.containsKey("texture")) {
            if (!(meta instanceof SkullMeta skull)) throw new IllegalArgumentException("Texture requires a player head");
            var profile = Bukkit.createProfile(UUID.randomUUID(), null);
            profile.setProperty(new ProfileProperty("textures", data.get("texture", String.class)));
            skull.setPlayerProfile(profile);
        }
        if (data.containsKey("displayName")) meta.displayName(LEGACY.deserialize(data.get("displayName", String.class)));
        if (data.containsKey("lore")) {
            List<?> lore = data.get("lore", List.class);
            meta.lore(lore.stream().map(value -> LEGACY.deserialize(Objects.toString(value, ""))).toList());
        }
        int damage = integer(data, "damage", LegacyMaterialMigration.usesLegacyData(name) ? 0 : legacyData);
        if (damage < 0) throw new IllegalArgumentException("Negative item damage");
        if (meta instanceof Damageable damageable) damageable.setDamage(damage);
        if (data.containsKey("enchants")) {
            Object raw = data.get("enchants", Object.class);
            if (raw instanceof Map<?, ?> enchants) enchants.forEach((key, level) -> addEnchant(meta, key.toString(), ((Number) level).intValue()));
            else if (raw instanceof Collection<?> enchants) enchants.forEach(key -> addEnchant(meta, key.toString(), 1));
            else throw new IllegalArgumentException("Enchants must be a map or list");
        }
        if (data.containsKey("itemFlags")) {
            for (Object flag : data.get("itemFlags", List.class)) meta.addItemFlags(ItemFlag.valueOf(flag.toString()));
        }
        item.setItemMeta(meta);
        return item;
    }

    private int integer(DeserializationData data, String key, int fallback) {
        return data.containsKey(key) ? data.get(key, Integer.class) : fallback;
    }

    private void addEnchant(ItemMeta meta, String name, int level) {
        String key = switch (name.toUpperCase(Locale.ROOT)) {
            case "DURABILITY" -> "unbreaking";
            case "DAMAGE_ALL" -> "sharpness";
            case "PROTECTION_ENVIRONMENTAL" -> "protection";
            case "ARROW_DAMAGE" -> "power";
            case "DIG_SPEED" -> "efficiency";
            default -> name.toLowerCase(Locale.ROOT);
        };
        NamespacedKey namespaced = NamespacedKey.fromString(key);
        Enchantment enchantment = namespaced == null ? null : Registry.ENCHANTMENT.get(namespaced);
        if (enchantment == null || level < 1) throw new IllegalArgumentException("Invalid enchantment: " + name);
        meta.addEnchant(enchantment, level, true);
    }
}
