package dk.setups.celle.gui.config.item;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import dk.setups.celle.gui.config.GUISerdesPack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class ItemStackSerializerTest {
    public static class ItemConfig extends OkaeriConfig { public ItemStack item; }
    @BeforeEach void mock() { MockBukkit.mock(); }
    @AfterEach void unmock() { MockBukkit.unmock(); }
    ItemConfig config() {
        return ConfigManager.create(ItemConfig.class, it -> it.withConfigurer(new YamlSnakeYamlConfigurer(), new GUISerdesPack()));
    }
    @Test void oldColoredPaneConfigLoadsAsModernMaterial() {
        ItemConfig config = config();
        config.load("item:\n  material: STAINED_GLASS_PANE\n  durability: 15\n  displayName: '§aTest'\n");
        assertEquals(Material.BLACK_STAINED_GLASS_PANE, config.item.getType());
        assertEquals("§aTest", config.item.getItemMeta().getDisplayName());
    }
    @Test void enchantmentLevelsAndFlagsRoundTrip() {
        ItemConfig original = config();
        original.load("item:\n  material: DIAMOND_SWORD\n  damage: 12\n  enchants:\n    minecraft:sharpness: 4\n  itemFlags: [HIDE_ENCHANTS]\n");
        ItemConfig loaded = config();
        loaded.load(original.saveToString());
        assertEquals(original.item, loaded.item);
    }
    @Test void unknownMaterialsFailClearly() {
        assertThrows(IllegalArgumentException.class, () -> LegacyMaterialMigration.resolve("NO_SUCH_ITEM", 0));
    }
    @Test void acceptsServerItemStackSubclasses() {
        class ServerItemStack extends ItemStack { ServerItemStack() { super(Material.PAPER); } }
        assertTrue(new ItemStackSerializer().supports((Class) ServerItemStack.class));
    }
    @Test void customSkullTexturesRoundTripThroughPublicProfileApi() {
        ItemConfig original = config();
        original.load("item:\n  material: PLAYER_HEAD\n  texture: dGVzdA==\n");
        ItemConfig loaded = config();
        loaded.load(original.saveToString());
        assertEquals("dGVzdA==", ((org.bukkit.inventory.meta.SkullMeta) loaded.item.getItemMeta()).getPlayerProfile().getProperties().iterator().next().getValue());
    }

}
