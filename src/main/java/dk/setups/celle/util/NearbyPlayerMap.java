package dk.setups.celle.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.*;

/** A snapshot of players by world and chunk, built on the server thread. */
public class NearbyPlayerMap {
    private record WorldChunk(UUID world, long chunk) {}
    private final Map<WorldChunk, Collection<Player>> chunkToPlayer = new HashMap<>();

    public static NearbyPlayerMap from(Collection<? extends Player> players) {
        NearbyPlayerMap map = new NearbyPlayerMap();
        players.forEach(map::add);
        return map;
    }
    public void add(Player player) {
        Location location = player.getLocation();
        if (location.getWorld() == null) return;
        WorldChunk key = new WorldChunk(location.getWorld().getUID(), ChunkUtils.toLong(location));
        chunkToPlayer.computeIfAbsent(key, ignored -> new HashSet<>()).add(player);
    }
    public Collection<? extends Player> getNearbyPlayers(Location location) {
        if (location.getWorld() == null) return List.of();
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        Set<Player> players = new HashSet<>();
        for (int x = chunkX - 2; x <= chunkX + 2; x++) {
            for (int z = chunkZ - 2; z <= chunkZ + 2; z++) {
                var key = new WorldChunk(location.getWorld().getUID(), ChunkUtils.toLong(x, z));
                players.addAll(chunkToPlayer.getOrDefault(key, List.of()));
            }
        }
        return players;
    }
}
