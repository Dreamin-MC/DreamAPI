package fr.dreamin.dreamapi.api.lang.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.config.Configurations;
import fr.dreamin.dreamapi.api.lang.service.LangService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

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

  public static ItemStack translate(final @NotNull Locale locale, final @NotNull ItemStack item) {
    final var copy = item.clone();
    updateTranslate(locale, copy);
    return copy;
  }

  public static void updateTranslate(final @NotNull Player player, final @NotNull ItemStack item) {
    updateTranslate(player.locale(), item);
  }

  public static void updateTranslate(final @NotNull Locale locale, final @NotNull ItemStack item) {
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
      final var sourceItemName = deserializeComponent(rawItemName);
      final var newItemName = sourceItemName == null ? null : GlobalTranslator.render(sourceItemName, locale);

      if (!Objects.equals(oldItemName, newItemName)) {
        meta.itemName(newItemName);
        changed = true;
      }
    }

    final var rawDisplayName = meta.getPersistentDataContainer().get(DISPLAY_NAME_SOURCE_KEY, PersistentDataType.STRING);
    if (rawDisplayName != null) {
      final var oldDisplayName = meta.hasDisplayName() ? meta.displayName() : null;
      final var sourceDisplayName = deserializeComponent(rawDisplayName);
      final var newDisplayName = sourceDisplayName == null ? null : GlobalTranslator.render(sourceDisplayName, locale);

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
        .map(line -> GlobalTranslator.render(line, locale))
        .toList();

      if (!Objects.equals(oldLore, newLore)) {
        meta.lore(newLore);
        changed = true;
      }
    }

    if (changed)
      item.setItemMeta(meta);

  }

  public static boolean isSimilar(
    final @NotNull Player player,
    final @NotNull ItemStack rawItem,
    final @NotNull ItemStack translatedItem
  ) {
    if (rawItem.getType() != translatedItem.getType())
      return false;

    return translate(player, rawItem).isSimilar(translatedItem);
  }

  public static boolean isSimilar(
    final @NotNull UUID uuid,
    final @NotNull ItemStack rawItem,
    final @NotNull ItemStack translatedItem
  ) {
    if (rawItem.getType() != translatedItem.getType())
      return false;

    final var locale = DreamAPI.getAPI().getService(LangService.class).getLocale(uuid).orElse(null);
    if (locale == null)
      return false;

    return translate(locale, rawItem).isSimilar(translatedItem);
  }

  public static boolean isSimilar(
    final @NotNull ItemStack rawItem,
    final @NotNull ItemStack translatedItem
  ) {
    if (rawItem.getType() != translatedItem.getType())
      return false;

    final var rawMeta = rawItem.getItemMeta();
    final var translatedMeta = translatedItem.getItemMeta();

    if (rawMeta == null || translatedMeta == null)
      return rawMeta == translatedMeta;

    final var rawCopy = rawItem.clone();
    final var translatedCopy = translatedItem.clone();

    final var rawCopyMeta = rawCopy.getItemMeta();
    final var translatedCopyMeta = translatedCopy.getItemMeta();

    if (rawCopyMeta == null || translatedCopyMeta == null)
      return rawCopyMeta == translatedCopyMeta;

    striptranslatedFileds(rawCopyMeta);
    striptranslatedFileds(translatedCopyMeta);

    rawCopy.setItemMeta(rawCopyMeta);
    translatedCopy.setItemMeta(translatedCopyMeta);

    if (!rawCopy.isSimilar(translatedCopy))
      return false;

    return hasSameTranslationSource(rawMeta, translatedMeta);
  }

  public static boolean containsTranslated(
    final @NotNull Player player,
    final @NotNull ItemStack rawItem
  ) {
    return containsTranslated(player.getInventory(), player, rawItem);
  }

  public static boolean containsTranslated(
    final @NotNull PlayerInventory inventory,
    final @NotNull Player player,
    final @NotNull ItemStack rawItem
  ) {
    for (final var content : inventory.getStorageContents()) {
      if (content == null || content.getType().isAir())
        continue;

      if (isSimilar(player, rawItem, content))
        return true;
    }

    return false;
  }

  public static boolean containsTranslated(
    final @NotNull PlayerInventory inventory,
    final @NotNull ItemStack rawItem
  ) {
    for (final var content : inventory.getStorageContents()) {
      if (content == null || content.getType().isAir())
        continue;

      if (isSimilar(rawItem, content))
        return true;
    }
    return false;
  }

  public static boolean containsTranslated(
    final @NotNull PlayerInventory inventory,
    final @NotNull UUID uuid,
    final @NotNull ItemStack rawItem
  ) {
    for (final var content : inventory.getStorageContents()) {
      if (content == null || content.getType().isAir())
        continue;

      if (isSimilar(uuid, rawItem, content))
        return true;
    }

    return false;
  }

  public static boolean removeTranslated(
    final @NotNull Player player,
    final @NotNull ItemStack rawItem
  ) {
    return removeTranslated(player.getInventory(), player, rawItem);
  }

  public static boolean removeTranslated(
    final @NotNull PlayerInventory inventory,
    final @NotNull Player player,
    final @NotNull ItemStack rawItem
  ) {

    if (rawItem.getAmount() <= 0)
      return false;

    var remaining = rawItem.getAmount();

    for (var slot = 0; slot < inventory.getSize(); slot++) {
      final var content = inventory.getItem(slot);
      if (content == null || content.getType().isAir())
        continue;

      if (!isSimilar(player, rawItem, content))
        continue;

      if (content.getAmount() <= remaining) {
        remaining -= content.getAmount();
        inventory.setItem(slot, null);
      } else {
        content.setAmount(content.getAmount() - remaining);
        inventory.setItem(slot, content);
        remaining = 0;
      }

      if (remaining <= 0)
        return true;

    }

    return false;
  }

  public static boolean removeTranslated(
    final @NotNull PlayerInventory inventory,
    final @NotNull UUID uuid,
    final @NotNull ItemStack rawItem
  ) {

    if (rawItem.getAmount() <= 0)
      return false;

    var remaining = rawItem.getAmount();

    for (var slot = 0; slot < inventory.getSize(); slot++) {
      final var content = inventory.getItem(slot);
      if (content == null || content.getType().isAir())
        continue;

      if (!isSimilar(uuid, rawItem, content))
        continue;

      if (content.getAmount() <= remaining) {
        remaining -= content.getAmount();
        inventory.setItem(slot, null);
      } else {
        content.setAmount(content.getAmount() - remaining);
        inventory.setItem(slot, content);
        remaining = 0;
      }

      if (remaining <= 0)
        return true;

    }

    return false;
  }

  public static boolean removeTranslated(
    final @NotNull PlayerInventory inventory,
    final @NotNull ItemStack rawItem
  ) {
    if (rawItem.getAmount() <= 0)
      return false;

    var remaining = rawItem.getAmount();

    for (var slot = 0; slot < inventory.getSize(); slot++) {
      final var content = inventory.getItem(slot);
      if (content == null || content.getType().isAir())
        continue;

      if (!isSimilar(rawItem, content))
        continue;

      if (content.getAmount() <= remaining) {
        remaining -= content.getAmount();
        inventory.setItem(slot, null);
      }
      else {
        content.setAmount(content.getAmount() - remaining);
        inventory.setItem(slot, content);
        remaining = 0;
      }

      if (remaining <= 0)
        return true;

    }

    return false;
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private static void striptranslatedFileds(final @NotNull ItemMeta meta) {
    meta.itemName(null);
    meta.displayName(null);
    meta.lore(null);
  }

  private static boolean hasSameTranslationSource(
    final @NotNull ItemMeta rawMeta,
    final @NotNull ItemMeta translatedMeta
  ) {
    ensureItemNameSource(rawMeta);
    ensureDisplayNameSource(rawMeta);
    ensureLoreSource(rawMeta);

    final var rawPdc = rawMeta.getPersistentDataContainer();
    final var translatedPdc = translatedMeta.getPersistentDataContainer();

    return Objects.equals(
      rawPdc.get(ITEM_NAME_SOURCE_KEY, PersistentDataType.STRING),
      translatedPdc.get(ITEM_NAME_SOURCE_KEY, PersistentDataType.STRING)
    ) && Objects.equals(
      rawPdc.get(DISPLAY_NAME_SOURCE_KEY, PersistentDataType.STRING),
      translatedPdc.get(DISPLAY_NAME_SOURCE_KEY, PersistentDataType.STRING)
    ) && Objects.equals(
      rawPdc.get(LORE_SOURCE_KEY, PersistentDataType.STRING),
      translatedPdc.get(LORE_SOURCE_KEY, PersistentDataType.STRING)
    );
  }

  private static boolean ensureItemNameSource(final @NotNull ItemMeta meta) {
    final var pdc = meta.getPersistentDataContainer();
    if (pdc.has(ITEM_NAME_SOURCE_KEY, PersistentDataType.STRING))
      return false;

    if (!meta.hasItemName())
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

    pdc.set(DISPLAY_NAME_SOURCE_KEY, PersistentDataType.STRING, COMPONENT_SERIALIZER.serialize(Objects.requireNonNull(meta.displayName())));
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
    final @NotNull String raw
  ) {
    try {
      return COMPONENT_SERIALIZER.deserialize(raw);
    } catch (Exception exception) {
      return null;
    }
  }

  private static @NotNull List<Component> deserializeLore(final @NotNull String raw) {
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