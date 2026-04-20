package fr.dreamin.dreamapi.api.map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * A thread-safe map whose entries automatically expire after a given duration.
 * <p>
 * Expiration is handled by a single periodic background task (daemon thread),
 * avoiding the overhead of scheduling one task per entry.
 * An optional expiry callback can be provided to be notified when an entry is removed.
 * </p>
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
public final class ExpiringMap<K, V> {

  private final @NotNull ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
  private final @NotNull ConcurrentHashMap<K, Long> expirations = new ConcurrentHashMap<>();

  private final ScheduledExecutorService scheduler;

  /** Optional callback invoked with (key, value) when an entry expires. */
  private @Nullable BiConsumer<K, V> expiryListener;

  // -------------------------------------------------------------------------
  // Constructors
  // -------------------------------------------------------------------------

  /**
   * Creates an ExpiringMap with a default cleanup interval of 500 ms.
   */
  public ExpiringMap() {
    this(Duration.ofMillis(500), null);
  }

  /**
   * Creates an ExpiringMap with a custom cleanup interval.
   *
   * @param cleanupInterval how often expired entries are purged
   */
  public ExpiringMap(@NotNull Duration cleanupInterval) {
    this(cleanupInterval, null);
  }

  /**
   * Creates an ExpiringMap with a custom cleanup interval and an expiry listener.
   *
   * @param cleanupInterval how often expired entries are purged
   * @param expiryListener  called with (key, value) when an entry expires; may be {@code null}
   */
  public ExpiringMap(@NotNull Duration cleanupInterval, @Nullable BiConsumer<K, V> expiryListener) {
    if (cleanupInterval.isNegative() || cleanupInterval.isZero())
      throw new IllegalArgumentException("Cleanup interval must be positive");

    this.expiryListener = expiryListener;

    // Daemon thread so the JVM can exit without explicitly calling shutdown()
    this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
      final Thread thread = new Thread(r, "ExpiringMap-cleanup");
      thread.setDaemon(true);
      return thread;
    });

    final long intervalMs = cleanupInterval.toMillis();
    this.scheduler.scheduleAtFixedRate(this::cleanupExpiredNow, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
  }

  // -------------------------------------------------------------------------
  // Core operations
  // -------------------------------------------------------------------------

  /**
   * Inserts or replaces the entry for {@code key} with the given {@code value} and {@code duration}.
   *
   * @param key      the key
   * @param value    the value
   * @param duration how long before the entry expires (must be positive)
   */
  public void put(@NotNull K key, @NotNull V value, @NotNull Duration duration) {
    if (duration.isNegative() || duration.isZero())
      throw new IllegalArgumentException("Duration must be positive");

    final long expireAt = System.currentTimeMillis() + duration.toMillis();
    map.put(key, value);
    expirations.put(key, expireAt);
  }

  /**
   * Inserts the entry only if {@code key} is not already present (or has expired).
   *
   * @return the existing (non-expired) value, or {@code null} if the entry was inserted
   */
  public @Nullable V putIfAbsent(@NotNull K key, @NotNull V value, @NotNull Duration duration) {
    final V existing = get(key);
    if (existing != null) return existing;
    put(key, value, duration);
    return null;
  }

  /**
   * Returns the value associated with {@code key}, or {@code null} if absent or expired.
   */
  public @Nullable V get(K key) {
    final Long expireAt = expirations.get(key);
    if (expireAt == null) return null;

    if (expireAt <= System.currentTimeMillis()) {
      evict(key);
      return null;
    }

    return map.get(key);
  }

  /**
   * Removes and returns the value for {@code key}, or {@code null} if absent.
   */
  public @Nullable V remove(K key) {
    expirations.remove(key);
    return map.remove(key);
  }

  /**
   * Resets the expiration timer for an existing entry.
   *
   * @return {@code true} if the entry existed and was renewed; {@code false} otherwise
   */
  public boolean renewExpiry(@NotNull K key, @NotNull Duration duration) {
    if (duration.isNegative() || duration.isZero())
      throw new IllegalArgumentException("Duration must be positive");

    if (!map.containsKey(key)) return false;
    expirations.put(key, System.currentTimeMillis() + duration.toMillis());
    return true;
  }

  // -------------------------------------------------------------------------
  // Query / inspection
  // -------------------------------------------------------------------------

  public boolean containsKey(K key) {
    return get(key) != null;
  }

  public boolean isEmpty() {
    cleanupExpiredNow();
    return map.isEmpty();
  }

  public int size() {
    cleanupExpiredNow();
    return map.size();
  }

  /**
   * Returns the remaining time before {@code key} expires, or {@link Duration#ZERO} if absent/expired.
   */
  public @NotNull Duration getRemaining(K key) {
    final Long expireAt = expirations.get(key);
    if (expireAt == null) return Duration.ZERO;
    final long remaining = expireAt - System.currentTimeMillis();
    return remaining <= 0 ? Duration.ZERO : Duration.ofMillis(remaining);
  }

  // -------------------------------------------------------------------------
  // Bulk / view operations
  // -------------------------------------------------------------------------

  /** Returns an immutable snapshot of all non-expired entries. */
  public @NotNull Map<K, V> snapshot() {
    cleanupExpiredNow();
    return Map.copyOf(map);
  }

  /** Returns an immutable snapshot of all non-expired keys. */
  public @NotNull Set<K> keySet() {
    cleanupExpiredNow();
    return Set.copyOf(map.keySet());
  }

  /** Returns an immutable snapshot of all non-expired values. */
  public @NotNull Collection<V> values() {
    cleanupExpiredNow();
    return java.util.List.copyOf(map.values());
  }

  public void clear() {
    map.clear();
    expirations.clear();
  }

  // -------------------------------------------------------------------------
  // Cleanup
  // -------------------------------------------------------------------------

  /** Immediately removes all entries whose TTL has elapsed. */
  public void cleanupExpiredNow() {
    final long now = System.currentTimeMillis();
    for (final Map.Entry<K, Long> entry : expirations.entrySet()) {
      if (entry.getValue() <= now) evict(entry.getKey());
    }
  }

  /** Evicts a single entry and fires the expiry listener if present. */
  private void evict(K key) {
    final V value = map.remove(key);
    expirations.remove(key);
    if (value != null && expiryListener != null) {
      expiryListener.accept(key, value);
    }
  }

  // -------------------------------------------------------------------------
  // Lifecycle
  // -------------------------------------------------------------------------

  /**
   * Sets (or replaces) the expiry listener.
   * The listener is called with the expired {@code (key, value)} pair when an entry is removed.
   */
  public void setExpiryListener(@Nullable BiConsumer<K, V> expiryListener) {
    this.expiryListener = expiryListener;
  }

  /**
   * Shuts down the background cleanup scheduler.
   * After calling this, no further automatic cleanup will occur.
   * The map itself remains usable.
   */
  public void shutdown() {
    scheduler.shutdownNow();
  }
}
