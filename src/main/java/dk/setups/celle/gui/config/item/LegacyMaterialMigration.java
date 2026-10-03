package dk.setups.celle.gui.config.item;

import org.bukkit.Material;
import java.util.Locale;

/** Converts the item names/data used by the 1.8 GUI files to flattened materials. */
public final class LegacyMaterialMigration {
    private static final String[] COLORS = {"WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK", "GRAY", "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK"};
    private LegacyMaterialMigration() {}

    public static Material resolve(String name, int data) {
        if (name == null) throw new IllegalArgumentException("Missing material");
        String normalized = name.toUpperCase(Locale.ROOT);
        if (normalized.startsWith("LEGACY_")) normalized = normalized.substring(7);
        normalized = switch (normalized) {
            case "STAINED_GLASS_PANE", "STAINED_GLASS", "WOOL", "STAINED_CLAY", "CARPET" -> {
                if (data < 0 || data >= COLORS.length) throw new IllegalArgumentException("Invalid color data: " + data);
                String suffix = normalized.equals("STAINED_CLAY") ? "TERRACOTTA" : normalized;
                yield COLORS[data] + "_" + suffix;
            }
            case "SIGN", "SIGN_POST" -> "OAK_SIGN";
            case "WALL_SIGN" -> "OAK_WALL_SIGN";
            case "BOOK_AND_QUILL" -> "WRITABLE_BOOK";
            case "SKULL_ITEM" -> switch (data) {
                case 0 -> "SKELETON_SKULL";
                case 1 -> "WITHER_SKELETON_SKULL";
                case 2 -> "ZOMBIE_HEAD";
                case 3 -> "PLAYER_HEAD";
                case 4 -> "CREEPER_HEAD";
                case 5 -> "DRAGON_HEAD";
                default -> throw new IllegalArgumentException("Invalid skull data: " + data);
            };
            case "IRON_DOOR_BLOCK" -> "IRON_DOOR";
            case "WOOD_DOOR", "WOODEN_DOOR" -> "OAK_DOOR";
            case "INK_SACK" -> "BLACK_DYE";
            case "WATCH" -> "CLOCK";
            case "GOLD_SPADE" -> "GOLDEN_SHOVEL";
            case "WOOD_SWORD" -> "WOODEN_SWORD";
            default -> normalized;
        };
        Material material = Material.matchMaterial(normalized);
        if (material == null || (!material.isItem() && material != Material.AIR)) throw new IllegalArgumentException("Invalid item material: " + name);
        return material;
    }

    public static boolean usesLegacyData(String name) {
        String key = name.toUpperCase(Locale.ROOT).replace("LEGACY_", "");
        return switch (key) {
            case "STAINED_GLASS_PANE", "STAINED_GLASS", "WOOL", "STAINED_CLAY", "CARPET", "SKULL_ITEM" -> true;
            default -> false;
        };
    }
}
