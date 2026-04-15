package fr.dreamin.dreamapi.api.lang.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.config.Configurations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public final class LangUtils {

  private static final NamespacedKey ITEM_NAME_SOURCE_KEY =
    new NamespacedKey(DreamAPI.getAPI().plugin(), "lang_item_name_source");

  private static final NamespacedKey DISPLAY_NAME_SOURCE_KEY =
    new NamespacedKey(DreamAPI.getAPI().plugin(), "lang_display_name_source");

  private static final NamespacedKey LORE_SOURCE_KEY =
    new NamespacedKey(DreamAPI.getAPI().plugin(), "lang_lore_source");

  private static final GsonComponentSerializer COMPONENT_SERIALIZER = GsonComponentSerializer.gson();
  private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

  private LangUtils() {}

  public static Component translate(
    final @NotNull Player player,
    final @NotNull String key,
    final @NotNull Component... args
  ) {
    return GlobalTranslator.render(Component.translatable(key, args), player.locale());
  }

  public static ItemStack translate(final @NotNull Player player, final @NotNull ItemStack item) {
    final var copy = item.clone();
    updateTranslate(player, copy);
    return copy;
  }

  public static void updateTranslate(final @NotNull Player player, final @NotNull ItemStack item) {
    final var meta = item.getItemMeta();
    if (meta == null)
      return;

    var changed = false;

    changed |= ensureItemNameSource(meta);
    changed |= ensureDisplayNameSource(meta);
    changed |= ensureLoreSource(meta);

    final var rawItemName = meta.getPersistentDataContainer().get(ITEM_NAME_SOURCE_KEY, PersistentDataType.STRING);
    if (rawItemName != null) {
      final var oldItemName = meta.hasItemName() ? meta.itemName() : null;
      final var sourceItemName = deserializeComponent(rawItemName, "itemName");
      final var newItemName = sourceItemName == null ? null : GlobalTranslator.render(sourceItemName, player.locale());

      if (!Objects.equals(oldItemName, newItemName)) {
        meta.itemName(newItemName);
        changed = true;
      }
    }

    final var rawDisplayName = meta.getPersistentDataContainer().get(DISPLAY_NAME_SOURCE_KEY, PersistentDataType.STRING);
    if (rawDisplayName != null) {
      final var oldDisplayName = meta.hasDisplayName() ? meta.displayName() : null;
      final var sourceDisplayName = deserializeComponent(rawDisplayName, "displayName");
      final var newDisplayName = sourceDisplayName == null ? null : GlobalTranslator.render(sourceDisplayName, player.locale());

      if (!Objects.equals(oldDisplayName, newDisplayName)) {
        meta.displayName(newDisplayName);
        changed = true;
      }
    }

    final var rawLore = meta.getPersistentDataContainer().get(LORE_SOURCE_KEY, PersistentDataType.STRING);
    if (rawLore != null) {
      final var oldLore = meta.lore();
      final var sourceLore = deserializeLore(rawLore);
      final var newLore = sourceLore.stream()
        .map(line -> GlobalTranslator.render(line, player.locale()))
        .toList();

      if (!Objects.equals(oldLore, newLore)) {
        meta.lore(newLore);
        changed = true;
      }
    }

    if (changed)
      item.setItemMeta(meta);

    final var appliedMeta = item.getItemMeta();
  }

  private static boolean ensureItemNameSource(final @NotNull ItemMeta meta) {
    final var pdc = meta.getPersistentDataContainer();
    if (pdc.has(ITEM_NAME_SOURCE_KEY, PersistentDataType.STRING))
      return false;

    if (!meta.hasItemName() || meta.itemName() == null)
      return false;

    pdc.set(ITEM_NAME_SOURCE_KEY, PersistentDataType.STRING, COMPONENT_SERIALIZER.serialize(meta.itemName()));

    return true;
  }

  private static boolean ensureDisplayNameSource(final @NotNull ItemMeta meta) {
    final var pdc = meta.getPersistentDataContainer();
    if (pdc.has(DISPLAY_NAME_SOURCE_KEY, PersistentDataType.STRING))
      return false;

    if (!meta.hasDisplayName() || meta.displayName() == null)
      return false;

    pdc.set(DISPLAY_NAME_SOURCE_KEY, PersistentDataType.STRING, COMPONENT_SERIALIZER.serialize(meta.displayName()));
    return true;
  }

  private static boolean ensureLoreSource(final @NotNull ItemMeta meta) {
    final var pdc = meta.getPersistentDataContainer();
    if (pdc.has(LORE_SOURCE_KEY, PersistentDataType.STRING))
      return false;

    final List<Component> lore = meta.lore();
    if (lore == null || lore.isEmpty())
      return false;

    final List<String> serializedLore = lore.stream()
      .map(COMPONENT_SERIALIZER::serialize)
      .toList();

    try {
      pdc.set(LORE_SOURCE_KEY, PersistentDataType.STRING, Configurations.MAPPER.writeValueAsString(serializedLore));
      return true;
    } catch (Exception exception) {
      return false;
    }
  }

  private static @Nullable Component deserializeComponent(
    final @NotNull String raw,
    final @NotNull String field
  ) {
    try {
      return COMPONENT_SERIALIZER.deserialize(raw);
    } catch (Exception exception) {
      return null;
    }
  }

  private static @NotNull List<Component> deserializeLore(
    final @NotNull String raw
  ) {
    try {
      final var serializedLore = Configurations.MAPPER.readValue(raw, STRING_LIST_TYPE);
      final var lore = new ArrayList<Component>(serializedLore.size());

      for (String line : serializedLore) {
        lore.add(COMPONENT_SERIALIZER.deserialize(line));
      }

      return lore;
    } catch (Exception exception) {
      return List.of();
    }
  }
}