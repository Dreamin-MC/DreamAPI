package fr.dreamin.dreamapi.api.recipe;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.jetbrains.annotations.NotNull;

public record IngredientDefinition(IngredientPredicate predicate, RecipeChoice choice, ItemStack display) {

  public IngredientDefinition(final @NotNull IngredientPredicate predicate, final @NotNull RecipeChoice choice, final @NotNull ItemStack display) {
    this.predicate = predicate;
    this.choice = choice;
    this.display = display.clone();
  }

  // ###############################################################
  // ----------------------- STATIC METHODS ------------------------
  // ###############################################################

  public static IngredientDefinition of(final @NotNull Material material) {
    return new IngredientDefinition(
      IngredientPredicate.ofMaterial(material),
      new RecipeChoice.MaterialChoice(material),
      new ItemStack(material)
    );
  }

  public static IngredientDefinition of(final @NotNull ItemStack item) {
    return new IngredientDefinition(
      IngredientPredicate.ofExactItem(item),
      new RecipeChoice.ExactChoice(item),
      item
    );
  }

}
