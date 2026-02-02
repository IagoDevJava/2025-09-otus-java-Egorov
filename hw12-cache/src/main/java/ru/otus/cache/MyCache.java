package ru.otus.cache;

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyCache<K, V> implements HwCache<K, V> {

    private final Map<K, SoftReference<V>> cache = new WeakHashMap<>();
    private final List<HwListener<K, V>> listeners = new CopyOnWriteArrayList<>();
    private final Map<K, Long> accessTimes = new ConcurrentHashMap<>();

    @Override
    public void put(K key, V value) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(value, "Value cannot be null");

        cache.put(key, new SoftReference<>(value));
        accessTimes.put(key, System.currentTimeMillis());
        notifyListeners(key, value, "PUT");
    }

    @Override
    public void remove(K key) {
        Objects.requireNonNull(key, "Key cannot be null");

        V value = null;
        SoftReference<V> ref = cache.get(key);
        if (ref != null) {
            value = ref.get();
        }

        cache.remove(key);
        accessTimes.remove(key);
        notifyListeners(key, value, "REMOVE");
    }

    @Override
    public V get(K key) {
        Objects.requireNonNull(key, "Key cannot be null");

        SoftReference<V> ref = cache.get(key);
        if (ref == null) {
            return null;
        }

        V value = ref.get();
        if (value != null) {
            accessTimes.put(key, System.currentTimeMillis());
            notifyListeners(key, value, "GET");
        } else {
            cache.remove(key);
            accessTimes.remove(key);
        }

        return value;
    }

    @Override
    public int size() {
        return (int) cache.values().stream().filter(ref -> ref.get() != null).count();
    }

    @Override
    public void clear() {
        cache.clear();
        accessTimes.clear();
        notifyListeners(null, null, "CLEAR");
    }

    @Override
    public void addListener(HwListener<K, V> listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        listeners.add(listener);
    }

    @Override
    public void removeListener(HwListener<K, V> listener) {
        Objects.requireNonNull(listener, "Listener cannot be null");
        listeners.remove(listener);
    }

    private void notifyListeners(K key, V value, String action) {
        for (HwListener<K, V> listener : listeners) {
            try {
                listener.notify(key, value, action);
            } catch (Exception e) {
                System.err.println("Error in cache listener: " + e.getMessage());
            }
        }
    }
}
