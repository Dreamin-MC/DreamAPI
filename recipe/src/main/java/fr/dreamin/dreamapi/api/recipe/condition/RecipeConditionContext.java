package fr.dreamin.dreamapi.api.recipe.condition;

import fr.dreamin.dreamapi.api.recipe.RecipeCraftingType;
import fr.dreamin.dreamapi.api.recipe.RecipeType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record RecipeConditionContext(
  @NotNull Player player,
  @Nullable InventoryView view,
  @NotNull RecipeType recipeType,
  @NotNull RecipeCraftingType craftingType
  ) {
}
