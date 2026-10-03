package dk.setups.celle.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dk.setups.celle.cell.Cell;
import dk.setups.celle.cell.CellRegion;
import eu.okaeri.platform.core.annotation.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

@Component
public class WorldGuardUtils {
    private RegionManager manager(World world) {
        if (world == null) throw new IllegalArgumentException("World is not loaded");
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (manager == null) throw new IllegalArgumentException("WorldGuard regions unavailable in " + world.getName());
        return manager;
    }

    public ProtectedRegion create(String name, CuboidRegion selection) {
        if (selection.getWorld() == null) throw new IllegalArgumentException("Selection has no world");
        RegionManager manager = manager(Bukkit.getWorld(selection.getWorld().getName()));
        if (manager.hasRegion(name)) throw new IllegalArgumentException("Region already exists");
        ProtectedRegion region = new ProtectedCuboidRegion(name, selection.getMinimumPoint(), selection.getMaximumPoint());
        manager.addRegion(region);
        return region;
    }

    public void delete(CellRegion region) {
        manager(Bukkit.getWorld(region.getRegionWorld())).removeRegion(region.getName());
    }

    public Set<ProtectedRegion> getRegionsAt(Location location) {
        if (location.getWorld() == null) return Collections.emptySet();
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
        return manager == null ? Collections.emptySet() : manager.getApplicableRegions(BukkitAdapter.asBlockVector(location)).getRegions();
    }

    public Collection<ProtectedRegion> getRegionsIn(World world, ProtectedRegion region) {
        Collection<ProtectedRegion> regions = manager(world).getRegions().values();
        return region.getId().equals("__global__") ? regions : region.getIntersectingRegions(regions);
    }

    public Optional<ProtectedRegion> getRegionByName(World world, String name) {
        if (world == null) return Optional.empty();
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        return manager == null ? Optional.empty() : Optional.ofNullable(manager.getRegion(name));
    }

    public void updateRegion(Cell cell) {
        ProtectedRegion region = getRegionByName(Bukkit.getWorld(cell.getRegion().getRegionWorld()), cell.getRegion().getName())
                .orElseThrow(() -> new IllegalArgumentException("Region not found for cell " + cell.getName()));
        DefaultDomain owners = new DefaultDomain();
        DefaultDomain members = new DefaultDomain();
        if (cell.isRented() && cell.getOwner() != null) {
            owners.addPlayer(cell.getOwner().getUuid());
            if (cell.getMembers() != null) cell.getMembers().forEach(member -> owners.addPlayer(member.getUser().getUuid()));
        }
        owners.addGroup("cell.build");
        members.addGroup("cell.interact");
        region.setOwners(owners);
        region.setMembers(members);
        region.setPriority(5);
        // Only replace flags managed by Celler; preserve unrelated administrator flags.
        region.setFlag(Flags.WATER_FLOW, StateFlag.State.DENY);
        setGroupFlag(region, Flags.USE, RegionGroup.MEMBERS);
        setGroupFlag(region, Flags.INTERACT, RegionGroup.MEMBERS);
        setGroupFlag(region, Flags.CHEST_ACCESS, RegionGroup.MEMBERS);
        setGroupFlag(region, Flags.BLOCK_PLACE, RegionGroup.OWNERS);
        setGroupFlag(region, Flags.BLOCK_BREAK, RegionGroup.OWNERS);
        // WorldGuard's BUILD flag has no region group. Its normal ownership check protects building.
        region.setFlag(Flags.BUILD, null);
    }

    private void setGroupFlag(ProtectedRegion region, StateFlag flag, RegionGroup group) {
        region.setFlag(flag, StateFlag.State.ALLOW);
        if (flag.getRegionGroupFlag() != null) region.setFlag(flag.getRegionGroupFlag(), group);
    }

    public Optional<ProtectedRegion> getHighestPriority(Location location) {
        return getRegionsAt(location).stream().max(Comparator.comparingInt(ProtectedRegion::getPriority).thenComparing(ProtectedRegion::getId));
    }

    public boolean canBuild(Player player, Block block) {
        var localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        if (WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, BukkitAdapter.adapt(block.getWorld()))) return true;
        return WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
                .testBuild(BukkitAdapter.adapt(block.getLocation()), localPlayer);
    }
}
