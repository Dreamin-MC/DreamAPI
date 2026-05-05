package fr.dreamin.dreamapi.api.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ItemRegistryService {

  void registers(final @NotNull Collection<ItemDefinition> definitions);

  void register(final @NotNull ItemDefinition definition);

  void unregister(final @NotNull ItemDefinition definition);

  void unregister(final @NotNull String id);

  void addHandler(final @NotNull String id, final @NotNull ItemAction action, final @NotNull ItemHandler handler);

  void addHandler(final @NotNull ItemTag tag, final @NotNull ItemAction action, final @NotNull ItemHandler handler);

  Collection<RegisteredItem> getAllRegisteredItems();

  Collection<ItemStack> getAllRegisteredItemStacks();

  @Nullable
  RegisteredItem get(final @NotNull String id);

  @Nullable
  RegisteredItem get(final @NotNull ItemStack item);

  Collection<RegisteredItem> getAllRegisteredItems(final @NotNull ItemTag tag);

  Collection<ItemStack> getAllRegisteredItemStacks(final @NotNull ItemTag tag);

  @Nullable
  ItemStack getItem(final @NotNull String id);

  boolean isRegistered(final @NotNull String id);

  boolean isRegistered(final @NotNull ItemStack item);


}
