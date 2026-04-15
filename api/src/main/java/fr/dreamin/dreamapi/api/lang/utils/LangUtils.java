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
    final Logger logger = DreamAPI.getAPI().plugin().getLogger();


    final ItemMeta meta = item.getItemMeta();
    if (meta == null) {
      logger.info("Meta=null -> skip");
      logger.info("----- /LangUtils.updateTranslate -----");
      return;
    }

    boolean changed = false;

    changed |= ensureItemNameSource(meta, logger);
    changed |= ensureDisplayNameSource(meta, logger);
    changed |= ensureLoreSource(meta, logger);

    final var rawItemName = meta.getPersistentDataContainer().get(ITEM_NAME_SOURCE_KEY, PersistentDataType.STRING);
    if (rawItemName != null) {
      final Component oldItemName = meta.hasItemName() ? meta.itemName() : null;
      final Component sourceItemName = deserializeComponent(rawItemName, logger, "itemName");
      final Component newItemName = sourceItemName == null ? null : GlobalTranslator.render(sourceItemName, player.locale());

      logger.info("ItemName source=" + sourceItemName);
      logger.info("ItemName old=" + oldItemName);
      logger.info("ItemName new=" + newItemName);
      logger.info("ItemName changed=" + !Objects.equals(oldItemName, newItemName));

      if (!Objects.equals(oldItemName, newItemName)) {
        meta.itemName(newItemName);
        changed = true;
      }
    } else {
      logger.info("ItemName=none");
    }

    final var rawDisplayName = meta.getPersistentDataContainer().get(DISPLAY_NAME_SOURCE_KEY, PersistentDataType.STRING);
    if (rawDisplayName != null) {
      final Component oldDisplayName = meta.hasDisplayName() ? meta.displayName() : null;
      final Component sourceDisplayName = deserializeComponent(rawDisplayName, logger, "displayName");
      final Component newDisplayName = sourceDisplayName == null ? null : GlobalTranslator.render(sourceDisplayName, player.locale());

      logger.info("DisplayName source=" + sourceDisplayName);
      logger.info("DisplayName old=" + oldDisplayName);
      logger.info("DisplayName new=" + newDisplayName);
      logger.info("DisplayName changed=" + !Objects.equals(oldDisplayName, newDisplayName));

      if (!Objects.equals(oldDisplayName, newDisplayName)) {
        meta.displayName(newDisplayName);
        changed = true;
      }
    } else {
      logger.info("DisplayName=none");
    }

    final var rawLore = meta.getPersistentDataContainer().get(LORE_SOURCE_KEY, PersistentDataType.STRING);
    if (rawLore != null) {
      final List<Component> oldLore = meta.lore();
      final List<Component> sourceLore = deserializeLore(rawLore, logger);
      final List<Component> newLore = sourceLore.stream()
        .map(line -> GlobalTranslator.render(line, player.locale()))
        .toList();

      logger.info("Lore source size=" + sourceLore.size());
      for (int i = 0; i < sourceLore.size(); i++) {
        logger.info("Lore source[" + i + "]=" + sourceLore.get(i));
      }

      if (oldLore == null) {
        logger.info("Lore old=null");
      } else {
        logger.info("Lore old size=" + oldLore.size());
        for (int i = 0; i < oldLore.size(); i++) {
          logger.info("Lore old[" + i + "]=" + oldLore.get(i));
        }
      }

      logger.info("Lore new size=" + newLore.size());
      for (int i = 0; i < newLore.size(); i++) {
        logger.info("Lore new[" + i + "]=" + newLore.get(i));
      }

      logger.info("Lore changed=" + !Objects.equals(oldLore, newLore));

      if (!Objects.equals(oldLore, newLore)) {
        meta.lore(newLore);
        changed = true;
      }
    } else {
      logger.info("Lore=none");
    }

    logger.info("Meta changed=" + changed);

    if (changed) {
      item.setItemMeta(meta);
      logger.info("setItemMeta applied");
    } else {
      logger.info("setItemMeta skipped");
    }

    final ItemMeta appliedMeta = item.getItemMeta();
    logger.info("Applied itemName=" + (appliedMeta != null && appliedMeta.hasItemName() ? appliedMeta.itemName() : "null"));
    logger.info("Applied displayName=" + (appliedMeta != null ? appliedMeta.displayName() : "null"));
    logger.info("Applied lore=" + (appliedMeta != null ? appliedMeta.lore() : "null"));
    logger.info("----- /LangUtils.updateTranslate -----");
  }

  private static boolean ensureItemNameSource(final @NotNull ItemMeta meta, final @NotNull Logger logger) {
    final var pdc = meta.getPersistentDataContainer();
    if (pdc.has(ITEM_NAME_SOURCE_KEY, PersistentDataType.STRING)) {
      return false;
    }

    if (!meta.hasItemName() || meta.itemName() == null) {
      return false;
    }

    pdc.set(ITEM_NAME_SOURCE_KEY, PersistentDataType.STRING, COMPONENT_SERIALIZER.serialize(meta.itemName()));
    logger.info("Stored itemName source into PDC");
    return true;
  }

  private static boolean ensureDisplayNameSource(final @NotNull ItemMeta meta, final @NotNull Logger logger) {
    final var pdc = meta.getPersistentDataContainer();
    if (pdc.has(DISPLAY_NAME_SOURCE_KEY, PersistentDataType.STRING)) {
      return false;
    }

    if (!meta.hasDisplayName() || meta.displayName() == null) {
      return false;
    }

    pdc.set(DISPLAY_NAME_SOURCE_KEY, PersistentDataType.STRING, COMPONENT_SERIALIZER.serialize(meta.displayName()));
    logger.info("Stored displayName source into PDC");
    return true;
  }

  private static boolean ensureLoreSource(final @NotNull ItemMeta meta, final @NotNull Logger logger) {
    final var pdc = meta.getPersistentDataContainer();
    if (pdc.has(LORE_SOURCE_KEY, PersistentDataType.STRING)) {
      return false;
    }

    final List<Component> lore = meta.lore();
    if (lore == null || lore.isEmpty()) {
      return false;
    }

    final List<String> serializedLore = lore.stream()
      .map(COMPONENT_SERIALIZER::serialize)
      .toList();

    try {
      pdc.set(LORE_SOURCE_KEY, PersistentDataType.STRING, Configurations.MAPPER.writeValueAsString(serializedLore));
      logger.info("Stored lore source into PDC");
      return true;
    } catch (Exception exception) {
      logger.warning("Failed to store lore source into PDC: " + exception.getMessage());
      return false;
    }
  }

  private static @Nullable Component deserializeComponent(
    final @NotNull String raw,
    final @NotNull Logger logger,
    final @NotNull String field
  ) {
    try {
      return COMPONENT_SERIALIZER.deserialize(raw);
    } catch (Exception exception) {
      logger.warning("Failed to deserialize " + field + ": " + exception.getMessage());
      return null;
    }
  }

  private static @NotNull List<Component> deserializeLore(
    final @NotNull String raw,
    final @NotNull Logger logger
  ) {
    try {
      final var serializedLore = Configurations.MAPPER.readValue(raw, STRING_LIST_TYPE);
      final var lore = new ArrayList<Component>(serializedLore.size());

      for (String line : serializedLore) {
        lore.add(COMPONENT_SERIALIZER.deserialize(line));
      }

      return lore;
    } catch (Exception exception) {
      logger.warning("Failed to deserialize lore source: " + exception.getMessage());
      return List.of();
    }
  }
}