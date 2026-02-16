package ru.otus.cache;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyCache<K, V> implements HwCache<K, V> {

  private final Map<K, V> cache = new WeakHashMap<>();
  private final List<HwListener<K, V>> listeners = new CopyOnWriteArrayList<>();

  @Override
  public void put(K key, V value) {
    Objects.requireNonNull(key, "Key cannot be null");
    Objects.requireNonNull(value, "Value cannot be null");

    cache.put(key, value);
    notifyListeners(key, value, "PUT");
  }

  @Override
  public void remove(K key) {
    Objects.requireNonNull(key, "Key cannot be null");
    V value = cache.remove(key);
    notifyListeners(key, value, "REMOVE");
  }

  @Override
  public V get(K key) {
    Objects.requireNonNull(key, "Key cannot be null");
    V value = cache.get(key);
    if (value != null) {
      notifyListeners(key, value, "GET");
    }
    return value;
  }

  @Override
  public int size() {
    return cache.size();
  }

  @Override
  public void clear() {
    cache.clear();
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
