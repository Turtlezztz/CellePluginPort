package dk.setups.celle.database;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache<K, V> {

    private final Map<K, V> cache = new ConcurrentHashMap<>();

    public V getCached(K key) {
        return this.cache.get(key);
    }

    public Collection<V> getAll() {
        return java.util.List.copyOf(this.cache.values());
    }

    public void update(K key, V value) {
        this.cache.put(key, value);
    }

    public void clear() { this.cache.clear(); }

    public void invalidate(K key) {
        this.cache.remove(key);
    }

}
