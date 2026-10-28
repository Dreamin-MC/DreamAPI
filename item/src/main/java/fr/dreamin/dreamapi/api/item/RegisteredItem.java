package fr.dreamin.dreamapi.api.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface RegisteredItem {

  String id();

  ItemStack item();

  Set<ItemTag> tags();

  boolean hasTag(final @NotNull ItemTag tag);

  void addHandler(final @NotNull ItemAction action, final @NotNull ItemHandler handler);

  void execute(final @NotNull ItemAction action, final @NotNull ItemContext ctx);

}
