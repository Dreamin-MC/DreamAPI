package fr.dreamin.dreamapi.api.map;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;

/**
 * A thread-safe map whose entries automatically expire after a given duration.
 * <p>
 * Backed by a Caffeine W-TinyLFU cache using a shared background daemon scheduler,
 * avoiding the overhead of creating one thread per map instance.
 * An optional expiry callback can be provided to be notified when an entry is removed.
 * </p>
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
public final class ExpiringMap<K, V> {

  private record ExpiringEntry<V>(@NotNull V value, @NotNull Duration duration, long expireAtMillis) {}

  private static final ScheduledExecutorService SHARED_SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
    final var thread = new Thread(r, "DreamAPI-ExpiringMap-Cleanup");
    thread.setDaemon(true);
    return thread;
  });

  private final Cache<K, ExpiringEntry<V>> cache;
  private final ConcurrentHashMap<K, ExpiringEntry<V>> trackedEntries = new ConcurrentHashMap<>();

  /** Optional callback invoked with (key, value) when an entry expires. */
  private volatile @Nullable BiConsumer<K, V> expiryListener;

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
    this.cache = Caffeine.newBuilder()
      .scheduler(Scheduler.forScheduledExecutorService(SHARED_SCHEDULER))
      .executor(Runnable::run)
      .expireAfter(new Expiry<K, ExpiringEntry<V>>() {
        @Override
        public long expireAfterCreate(final @NotNull K key, final @NotNull ExpiringEntry<V> value, final long currentTime) {
          return value.duration().toNanos();
        }

        @Override
        public long expireAfterUpdate(final @NotNull K key, final @NotNull ExpiringEntry<V> value, final long currentTime, final long currentDuration) {
          return value.duration().toNanos();
        }

        @Override
        public long expireAfterRead(final @NotNull K key, final @NotNull ExpiringEntry<V> value, final long currentTime, final long currentDuration) {
          return currentDuration;
        }
      })
      .removalListener((final @Nullable K key, final @Nullable ExpiringEntry<V> value, final @NotNull RemovalCause cause) -> {
        if (cause == RemovalCause.EXPIRED && key != null && value != null) {
          final var tracked = this.trackedEntries.remove(key);
          if (tracked != null) {
            final var listener = this.expiryListener;
            if (listener != null)
              listener.accept(key, value.value());
          }
        }
      })
      .build();
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

    final var expireAt = System.currentTimeMillis() + duration.toMillis();
    final var entry = new ExpiringEntry<>(value, duration, expireAt);
    this.trackedEntries.put(key, entry);
    this.cache.put(key, entry);
  }

  /**
   * Inserts the entry only if {@code key} is not already present (or has expired).
   *
   * @return the existing (non-expired) value, or {@code null} if the entry was inserted
   */
  public @Nullable V putIfAbsent(@NotNull K key, @NotNull V value, @NotNull Duration duration) {
    final var existing = get(key);
    if (existing != null)
      return existing;

    put(key, value, duration);
    return null;
  }

  /**
   * Returns the value associated with {@code key}, or {@code null} if absent or expired.
   */
  public @Nullable V get(K key) {
    if (key == null)
      return null;

    final var now = System.currentTimeMillis();
    final var entry = this.cache.getIfPresent(key);
    if (entry == null) {
      final var tracked = this.trackedEntries.get(key);
      if (tracked != null && tracked.expireAtMillis() <= now) {
        if (this.trackedEntries.remove(key, tracked)) {
          this.cache.asMap().remove(key);
          final var listener = this.expiryListener;
          if (listener != null)
            listener.accept(key, tracked.value());
        }
      }
      return null;
    }

    if (entry.expireAtMillis() <= now) {
      if (this.trackedEntries.remove(key, entry)) {
        this.cache.asMap().remove(key);
        final var listener = this.expiryListener;
        if (listener != null)
          listener.accept(key, entry.value());
      }
      return null;
    }

    return entry.value();
  }

  /**
   * Removes and returns the value for {@code key}, or {@code null} if absent.
   */
  public @Nullable V remove(K key) {
    if (key == null)
      return null;

    final var tracked = this.trackedEntries.remove(key);
    this.cache.asMap().remove(key);
    if (tracked == null || tracked.expireAtMillis() <= System.currentTimeMillis())
      return null;

    return tracked.value();
  }

  /**
   * Resets the expiration timer for an existing entry.
   *
   * @return {@code true} if the entry existed and was renewed; {@code false} otherwise
   */
  public boolean renewExpiry(@NotNull K key, @NotNull Duration duration) {
    if (duration.isNegative() || duration.isZero())
      throw new IllegalArgumentException("Duration must be positive");

    final var now = System.currentTimeMillis();
    final var updated = this.trackedEntries.computeIfPresent(key, (_, current) -> {
      if (current.expireAtMillis() <= now)
        return null;
      return new ExpiringEntry<>(current.value(), duration, now + duration.toMillis());
    });
    if (updated != null) {
      this.cache.put(key, updated);
      return true;
    }

    this.cache.asMap().remove(key);
    return false;
  }

  // -------------------------------------------------------------------------
  // Query / inspection
  // -------------------------------------------------------------------------

  public boolean containsKey(K key) {
    return get(key) != null;
  }

  public boolean isEmpty() {
    cleanupExpiredNow();
    return this.trackedEntries.isEmpty();
  }

  public int size() {
    cleanupExpiredNow();
    return this.trackedEntries.size();
  }

  /**
   * Returns the remaining time before {@code key} expires, or {@link Duration#ZERO} if absent/expired.
   */
  public @NotNull Duration getRemaining(K key) {
    if (key == null)
      return Duration.ZERO;

    final var entry = this.trackedEntries.get(key);
    if (entry == null)
      return Duration.ZERO;

    final var remaining = entry.expireAtMillis() - System.currentTimeMillis();
    if (remaining <= 0) {
      cleanupExpiredNow();
      return Duration.ZERO;
    }

    return Duration.ofMillis(remaining);
  }

  // -------------------------------------------------------------------------
  // Bulk / view operations
  // -------------------------------------------------------------------------

  /** Returns an immutable snapshot of all non-expired entries. */
  public @NotNull Map<K, V> snapshot() {
    cleanupExpiredNow();
    final var now = System.currentTimeMillis();
    final var result = new HashMap<K, V>();
    for (final var entry : this.trackedEntries.entrySet())
      if (entry.getValue().expireAtMillis() > now)
        result.put(entry.getKey(), entry.getValue().value());

    return Map.copyOf(result);
  }

  /** Returns an immutable snapshot of all non-expired keys. */
  public @NotNull Set<K> keySet() {
    return snapshot().keySet();
  }

  /** Returns an immutable snapshot of all non-expired values. */
  public @NotNull Collection<V> values() {
    return snapshot().values();
  }

  public void clear() {
    this.trackedEntries.clear();
    this.cache.invalidateAll();
  }

  // -------------------------------------------------------------------------
  // Cleanup
  // -------------------------------------------------------------------------

  /** Immediately removes all entries whose TTL has elapsed. */
  public void cleanupExpiredNow() {
    this.cache.cleanUp();
    final var now = System.currentTimeMillis();
    for (final var entry : this.trackedEntries.entrySet()) {
      if (entry.getValue().expireAtMillis() <= now) {
        if (this.trackedEntries.remove(entry.getKey(), entry.getValue())) {
          this.cache.asMap().remove(entry.getKey());
          final var listener = this.expiryListener;
          if (listener != null)
            listener.accept(entry.getKey(), entry.getValue().value());
        }
      }
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
   * Shuts down the local cache entries and performs immediate maintenance.
   * The shared background daemon continues running for remaining maps.
   */
  public void shutdown() {
    this.trackedEntries.clear();
    this.cache.invalidateAll();
    this.cache.cleanUp();
  }
}
