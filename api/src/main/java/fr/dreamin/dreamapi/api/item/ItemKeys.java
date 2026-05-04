package fr.dreamin.dreamapi.api.item;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class ItemKeys {

  public static NamespacedKey ITEM_ID;
  public static NamespacedKey PROJECTILE_ITEM_ID;

  public static void init(final @NotNull Plugin plugin) {
    ITEM_ID = new NamespacedKey(plugin, "dreamapi_item_id");
    PROJECTILE_ITEM_ID = new NamespacedKey(plugin, "dreamapi_projectile_item_id");
  }

}
