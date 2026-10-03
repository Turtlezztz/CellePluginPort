package dk.setups.celle.listener;

import dk.setups.celle.config.Config;
import dk.setups.celle.util.WorldGuardUtils;
import eu.okaeri.injector.annotation.Inject;
import eu.okaeri.platform.core.annotation.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

@Component
public class IronDoorListener implements Listener {
    private @Inject Config config;
    private @Inject WorldGuardUtils worldGuard;

    @EventHandler(ignoreCancelled = true)
    public void onIronDoorOpen(PlayerInteractEvent event) {
        if (!config.isIronDoorOpen() || event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.IRON_DOOR || !(block.getBlockData() instanceof Door door)) return;
        Block bottom = door.getHalf() == Bisected.Half.TOP ? block.getRelative(0, -1, 0) : block;
        Block top = bottom.getRelative(0, 1, 0);
        if (!(bottom.getBlockData() instanceof Door lower) || !(top.getBlockData() instanceof Door upper)) return;
        if (!worldGuard.canBuild(event.getPlayer(), bottom)) return;
        boolean open = !lower.isOpen();
        lower.setOpen(open);
        upper.setOpen(open);
        bottom.setBlockData(lower, false);
        top.setBlockData(upper, false);
        event.setCancelled(true);
    }
}
