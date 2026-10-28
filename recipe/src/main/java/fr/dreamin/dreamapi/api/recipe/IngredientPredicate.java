package fr.dreamin.dreamapi.api.recipe;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface IngredientPredicate {

  boolean matches(final @NotNull ItemStack item);

  static IngredientPredicate ofMaterial(final @NotNull Material mat) {
    return i -> i.getType() == mat;
  }

  static IngredientPredicate ofCustomModel(final @NotNull NamespacedKey model) {
    return i -> i.hasItemMeta() && i.getItemMeta().getItemModel() != null &&
      i.getItemMeta().getItemModel().equals(model);
  }

  static IngredientPredicate ofExactItem(final @NotNull ItemStack exact) {
    return i -> i.isSimilar(exact);
  }

  static IngredientPredicate ofTag(final @NotNull Tag<Material> tag) {
    return i -> tag.isTagged(i.getType());
  }

  static IngredientPredicate any() {
    return i -> true;
  }

}
