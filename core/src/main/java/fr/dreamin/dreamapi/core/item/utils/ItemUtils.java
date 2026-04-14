package fr.dreamin.dreamapi.core.item.utils;

import fr.dreamin.dreamapi.api.DreamAPI;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Utility class for handling ItemStack tags using PersistentDataContainer.
 * Provides methods to check for the existence of tags and retrieve their values.
 * @see ItemStack
 * @see PersistentDataType
 * @see NamespacedKey
 * @see DreamAPI
 *
 * @author Dreamin
 * @since 1.0.0
 */
public final class ItemUtils {

  public static <T, Z> boolean hasTag(@NotNull ItemStack item, @NotNull String key, @NotNull PersistentDataType<T, Z> type) {
    if (!item.hasItemMeta()) return false;
    final var meta = item.getItemMeta();
    if (meta == null) return false;
    final var namespacedKey = new NamespacedKey(DreamAPI.getAPI().plugin(), key);
    return meta.getPersistentDataContainer().has(namespacedKey, type);
  }

  public static <T, Z> boolean hasTag(@NotNull ItemStack item, final @NotNull NamespacedKey key, final @NotNull PersistentDataType<T, Z> type) {
    if (!item.hasItemMeta()) return false;
    final var meta = item.getItemMeta();
    if (meta == null) return false;
    return meta.getPersistentDataContainer().has(key, type);
  }

  public static <T, Z> Z getTag(@NotNull ItemStack item, @NotNull String key, @NotNull PersistentDataType<T, Z> type) {
    if (!item.hasItemMeta()) return null;
    final var meta = item.getItemMeta();
    if (meta == null) return null;
    final var namespacedKey = new NamespacedKey(DreamAPI.getAPI().plugin(), key);
    return meta.getPersistentDataContainer().get(namespacedKey, type);
  }

  public static <T, Z> Z getTag(@NotNull ItemStack item, final @NotNull NamespacedKey key, final @NotNull PersistentDataType<T, Z> type) {
    if (!item.hasItemMeta()) return null;
    final var meta = item.getItemMeta();
    if (meta == null) return null;
    return meta.getPersistentDataContainer().get(key, type);
  }

  public static Optional<String> getStringTag(@NotNull ItemStack item, @NotNull String key) {
    if (!item.hasItemMeta()) return Optional.empty();
    final var meta = item.getItemMeta();
    if (meta == null) return Optional.empty();
    final var namespacedKey = new NamespacedKey(DreamAPI.getAPI().plugin(), key);
    return Optional.ofNullable(meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING));
  }

  public static Optional<String> getStringTag(@NotNull ItemStack item, @NotNull NamespacedKey key) {
    if (!item.hasItemMeta()) return Optional.empty();
    final var meta = item.getItemMeta();
    if (meta == null) return Optional.empty();
    return Optional.ofNullable(meta.getPersistentDataContainer().get(key, PersistentDataType.STRING));
  }

  public static boolean getBooleanTag(@NotNull ItemStack item, @NotNull String key) {
    if (!item.hasItemMeta()) return false;
    final var meta = item.getItemMeta();
    if (meta == null) return false;
    final var namespacedKey = new NamespacedKey(DreamAPI.getAPI().plugin(), key);
    final var result = meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.BOOLEAN);
    return result != null && result;
  }

  public static boolean getBooleanTag(@NotNull ItemStack item, @NotNull NamespacedKey key) {
    if (!item.hasItemMeta()) return false;
    final var meta = item.getItemMeta();
    if (meta == null) return false;
    final var result = meta.getPersistentDataContainer().get(key, PersistentDataType.BOOLEAN);
    return result != null && result;
  }

  public static Optional<Integer> getIntTag(@NotNull ItemStack item, @NotNull String key) {
    if (!item.hasItemMeta()) return Optional.empty();
    final var meta = item.getItemMeta();
    if (meta == null) return Optional.empty();
    var namespacedKey = new NamespacedKey(DreamAPI.getAPI().plugin(), key);
    final var result = meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.INTEGER);
    return result != null ? Optional.of(result) : Optional.empty();
  }

  public static Optional<Integer> getIntTag(@NotNull ItemStack item, @NotNull NamespacedKey key) {
    if (!item.hasItemMeta()) return Optional.empty();
    final var meta = item.getItemMeta();
    if (meta == null) return Optional.empty();
    final var result = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
    return result != null ? Optional.of(result) : Optional.empty();
  }

  public static Optional<Double> getDoubleTag(@NotNull ItemStack item, @NotNull String key) {
    if (!item.hasItemMeta()) return Optional.empty();
    final var meta = item.getItemMeta();
    if (meta == null) return Optional.empty();
    final var namespacedKey = new NamespacedKey(DreamAPI.getAPI().plugin(), key);
    final var result = meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.DOUBLE);
    return result != null ? Optional.of(result) : Optional.empty();
  }

  public static Optional<Double> getDoubleTag(@NotNull ItemStack item, @NotNull NamespacedKey key) {
    if (!item.hasItemMeta()) return Optional.empty();
    final var meta = item.getItemMeta();
    if (meta == null) return Optional.empty();
    final var result = meta.getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
    return result != null ? Optional.of(result) : Optional.empty();
  }

}
