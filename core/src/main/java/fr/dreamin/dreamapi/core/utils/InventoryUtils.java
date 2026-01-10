package fr.dreamin.dreamapi.core.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Predicate;

public final class InventoryUtils {

  public enum CompareIgnoreAttribute { META, AMOUNT, TYPE, ENCHANTMENT, LABEL, DATACONTAINER }


  // ###############################################################
  // ---------------------------- BASE -----------------------------
  // ###############################################################

  public static void playerClear(final @NotNull Player player) {
    player.setItemOnCursor(null);
    player.getInventory().clear();
  }

  // ###############################################################
  // ---------------------------- COPY -----------------------------
  // ###############################################################

  public static void copyTo(final @NotNull Inventory origin, final @NotNull Inventory goal, final @Nullable Location outLocation) {
    for (var item : origin.getContents()) {
      if (item == null) continue;
      if (goal.firstEmpty() == -1) {
        if (outLocation != null)
          outLocation.getWorld().dropItemNaturally(outLocation, item.clone());
      }
      else
        goal.addItem(item.clone());

    }
  }

  public static void copyTo(final @NotNull Inventory origin, final @NotNull Inventory goal) {
    copyTo(origin, goal, null);
  }

  public static void dropTo(final @NotNull Inventory inventory, final @NotNull Location location, final boolean clear) {
    for (var item : inventory.getContents()) {
      if (item != null) location.getWorld().dropItemNaturally(location, item.clone());
    }
    if (clear) inventory.clear();
  }

  // ###############################################################
  // ------------------------ MANIPULATION -------------------------
  // ###############################################################

  public static void remove(final @NotNull Inventory inv, final @NotNull ItemStack item, final int quantity) {
    var remaining = quantity;
    for (var slot : inv.getContents()) {
      if (slot == null) continue;
      if (equals(slot, item, CompareIgnoreAttribute.AMOUNT)) {
        final var remove = Math.min(slot.getAmount(), remaining);
        slot.setAmount(slot.getAmount() - remove);
        remaining -= remove;
        if (slot.getAmount() <= 0) inv.removeItem(slot);
        if (remaining <= 0) return;
      }
    }
  }

  public static void remove(final @NotNull Player player, final @NotNull ItemStack item, final int quantity) {
    remove(player.getInventory(), item, quantity);
  }

  public static void replace(final @NotNull Inventory inv, final @NotNull ItemStack newest, final @NotNull ItemStack replaced, final boolean addIfNotExist) {
    final var slot = inv.first(replaced);
    if (slot == -1) {
      if (addIfNotExist) inv.addItem(newest.clone());
      return;
    }
    inv.setItem(slot, newest.clone());
  }

  public static void replace(final @NotNull Player player, final @NotNull ItemStack newest, final @NotNull ItemStack replaced, final boolean addIfNotExist) {
    if (player.getItemOnCursor().isEmpty() && player.getItemOnCursor().equals(replaced)) {
      player.setItemOnCursor(newest.clone());
      return;
    }
    replace(player.getInventory(), newest, replaced, addIfNotExist);
  }

  // ###############################################################
  // ------------------------ NEW METHODS --------------------------
  // ###############################################################

  public static boolean has(final @NotNull Inventory inv, final @NotNull ItemStack item, final int amount) {
    return count(inv, item) >= amount;
  }

  public static int findSlot(final @NotNull Inventory inv, final @NotNull ItemStack item) {
    for (var i = 0; i < inv.getSize(); i++) {
      final var slot = inv.getItem(i);
      if (slot != null && equals(slot, item)) return i;
    }
    return -1;
  }

  public static int count(final @NotNull Inventory inv, final @NotNull ItemStack item) {
    var total = 0;
    for (var slot : inv.getContents()) {
      if (slot == null) continue;
      if (equals(slot, item, CompareIgnoreAttribute.AMOUNT))
        total += slot.getAmount();
    }
    return total;
  }

  public static void merge(final @NotNull Inventory from, final @NotNull Inventory to, final @NotNull Predicate<@NotNull ItemStack> filter) {
    for (var item : from.getContents()) {
      if (item == null) continue;
      if (!filter.test(item)) continue;

      if (to.firstEmpty() != -1)
        to.addItem(item.clone());
      else
        if (to.getHolder() instanceof Player player)
          player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
    }
  }

  public static void giveOrDrop(final @NotNull Player player, final @NotNull ItemStack item) {
    final var inv = player.getInventory();
    if (inv.firstEmpty() != -1) inv.addItem(item.clone());
    else player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
  }

  public static void purge(final @NotNull Inventory inv, final @NotNull Predicate<@NotNull ItemStack> filter) {
    for (var i = 0; i < inv.getSize(); i++) {
      final var item = inv.getItem(i);
      if (item == null) continue;
      if (filter.test(item)) inv.setItem(i, null);
    }
  }

  public static boolean equals(final @NotNull ItemStack item1, final @NotNull ItemStack item2, final CompareIgnoreAttribute... ignoreAttributes) {
    final var ignored = EnumSet.noneOf(CompareIgnoreAttribute.class);
    if (ignoreAttributes != null) ignored.addAll(Arrays.asList(ignoreAttributes));

    if (!ignored.contains(CompareIgnoreAttribute.TYPE) && item1.getType() != item2.getType()) return false;
    if (!ignored.contains(CompareIgnoreAttribute.AMOUNT) && item1.getAmount() != item2.getAmount()) return false;

    final var meta1 = item1.getItemMeta();
    final var meta2 = item2.getItemMeta();

    if (!ignored.contains(CompareIgnoreAttribute.META)) {
      if (meta1 == null || meta2 == null) return meta1 == meta2;
      if (!meta1.equals(meta2)) return false;
    }

    if (!ignored.contains(CompareIgnoreAttribute.ENCHANTMENT) && !item1.getEnchantments().equals(item2.getEnchantments()))
      return false;

    if (!ignored.contains(CompareIgnoreAttribute.LABEL)
      && meta1 != null && meta2 != null
      && !meta1.itemName().equals(meta2.itemName()))
      return false;

    if (!ignored.contains(CompareIgnoreAttribute.DATACONTAINER)) {
      if (meta1 == null || meta2 == null) return meta1 == meta2;
      if (!meta1.getPersistentDataContainer().equals(meta2.getPersistentDataContainer()))
        return false;
    }

    return true;

  }

}
