package fr.dreamin.dreamapi.api.recipe;

import fr.dreamin.dreamapi.api.recipe.condition.RecipeCondition;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CustomRecipe {

  @NotNull String getKey();

  @NotNull RecipeType getType();

  @NotNull Set<RecipeTag> getTags();

  // ----- CRAFTING -----
  @NotNull List<List<IngredientDefinition>> getShape();
  @NotNull List<IngredientDefinition> getIngredients();

  // ----- RESULT -----
  @NotNull ItemStack getResult();

  // ----- PLAYER CONDITION -----
  @NotNull Optional<RecipeCondition> getPlayerCondition();

  @Nullable RecipeCraftingType getCraftingType();

  boolean isCraftingTypeAllowed(final @NotNull RecipeCraftingType type);

  // ----- FURNACE RECIPE -----
  @Nullable IngredientDefinition getFurnaceInput();
  float getFurnaceExperience();
  int getFurnaceCookTime();

  // ----- SMITHING RECIPE -----
  @Nullable IngredientDefinition getSmithingTemplate();
  @Nullable IngredientDefinition getSmithingBase();
  @Nullable IngredientDefinition getSmithingAddition();

  /** Convert logical recipe into a Bukkit recipe for registry. */
  @NotNull Recipe toBukkitRecipe(final @NotNull NamespacedKey key);

}
