package fr.dreamin.dreamapi.core.recipe.impl;

import fr.dreamin.dreamapi.api.recipe.*;
import fr.dreamin.dreamapi.api.recipe.condition.RecipeCondition;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class CustomRecipeImpl implements CustomRecipe {

  private final String key;
  private final RecipeType type;
  private final Set<RecipeTag> tags;
  private final List<List<IngredientDefinition>> shaped;
  private final List<IngredientDefinition> shapeless;
  private final ItemStack result;
  private final RecipeCondition recipeCondition;

  private final RecipeCraftingType craftingType;

  private final IngredientDefinition furnaceInput;
  private final float furnaceExp;
  private final int furnaceCookTime;

  private final IngredientDefinition smithTemplate, smithBase, smithAddition;

  public CustomRecipeImpl(
    final @NotNull String key,
    final @NotNull RecipeType type,
    final @NotNull Set<RecipeTag> tags,
    final @Nullable List<List<IngredientDefinition>> shaped,
    final @Nullable List<IngredientDefinition> shapeless,
    final @NotNull ItemStack result,
    final @Nullable RecipeCondition recipeCondition,
    final @Nullable RecipeCraftingType craftingType,
    final @Nullable IngredientDefinition furnaceInput,
    final float furnaceExp,
    final int furnaceCookTime,
    final @Nullable IngredientDefinition smithTemplate,
    final @Nullable IngredientDefinition smithBase,
    final @Nullable IngredientDefinition smithAddition
  ) {
    this.key = key;
    this.type = type;
    this.tags = tags;
    this.shaped = shaped != null ? Collections.unmodifiableList(shaped) : null;
    this.shapeless = shapeless != null ? Collections.unmodifiableList(shapeless) : null;
    this.result = result.clone();
    this.recipeCondition = recipeCondition != null ? recipeCondition : RecipeCondition.always();
    this.craftingType = craftingType;
    this.furnaceInput = furnaceInput;
    this.furnaceExp = furnaceExp;
    this.furnaceCookTime = furnaceCookTime;
    this.smithTemplate = smithTemplate;
    this.smithBase = smithBase;
    this.smithAddition = smithAddition;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public @NotNull String getKey() {
    return this.key;
  }

  @Override
  public @NotNull RecipeType getType() {
    return this.type;
  }

  @Override
  public @NotNull Set<RecipeTag> getTags() {
    return this.tags;
  }

  @Override
  public @NotNull List<List<IngredientDefinition>> getShape() {
    return Collections.unmodifiableList(this.shaped);
  }

  @Override
  public @NotNull List<IngredientDefinition> getIngredients() {
    return Collections.unmodifiableList(this.shapeless);
  }

  @Override
  public @NotNull ItemStack getResult() {
    return this.result.clone();
  }

  @Override
  public @NotNull Optional<RecipeCondition> getPlayerCondition() {
    return Optional.ofNullable(this.recipeCondition);
  }

  @Override
  public @Nullable RecipeCraftingType getCraftingType() {
    return this.craftingType;
  }

  @Override
  public boolean isCraftingTypeAllowed(@NotNull RecipeCraftingType type) {
    return this.craftingType == RecipeCraftingType.ALL || this.craftingType == type;
  }

  @Override
  public @Nullable IngredientDefinition getFurnaceInput() {
    return this.furnaceInput;
  }

  @Override
  public float getFurnaceExperience() {
    return this.furnaceExp;
  }

  @Override
  public int getFurnaceCookTime() {
    return this.furnaceCookTime;
  }

  @Override
  public @Nullable IngredientDefinition getSmithingTemplate() {
    return this.smithTemplate;
  }

  @Override
  public @Nullable IngredientDefinition getSmithingBase() {
    return this.smithBase;
  }

  @Override
  public @Nullable IngredientDefinition getSmithingAddition() {
    return this.smithAddition;
  }

  @Override
  public @NotNull Recipe toBukkitRecipe(@NotNull NamespacedKey key) {
    return switch (this.type) {
      case SHAPED -> toShapedRecipe(key);
      case SHAPELESS -> toShapelessRecipe(key);
      case FURNACE -> toFurnaceRecipe(key);
      case SMITHING -> toSmithingRecipe(key);
    };
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private Recipe toShapedRecipe(final @NotNull NamespacedKey key) {
    if (this.shaped == null)
      throw new IllegalStateException("Shaped recipe without shape: " + this.key);

    final var sr = new ShapedRecipe(key, result.clone());

    final var shapeRows = new String[this.shaped.size()];
    var nextChar = 'A';
    final var choices = new HashMap<Character, RecipeChoice>();

    for (var r = 0; r < this.shaped.size(); r++) {
      final var row = this.shaped.get(r);
      final var sb = new StringBuilder();

      for (var c = 0; c < row.size(); c++) {
        final var def = row.get(c);
        final var ch = nextChar++;
        sb.append(ch);
        choices.put(ch, def.choice());
      }
      shapeRows[r] = sb.toString();
    }

    sr.shape(shapeRows);

    for (final var entry : choices.entrySet()) {
      sr.setIngredient(entry.getKey(), entry.getValue());
    }
    return sr;
  }

  private Recipe toShapelessRecipe(final @NotNull NamespacedKey key) {
    if (this.shapeless == null)
      throw new IllegalStateException("Shapeless recipe without ingredients: " + this.key);

    final var sl = new ShapelessRecipe(key, this.result.clone());
    for (final var def : this.shapeless) {
      sl.addIngredient(def.choice());
    }
    return sl;
  }

  private Recipe toFurnaceRecipe(final @NotNull NamespacedKey key) {
    if (this.furnaceInput == null)
      throw new IllegalStateException("Furnace recipe without input: " + this.key);

    return new FurnaceRecipe(key, result.clone(), furnaceInput.choice(), furnaceExp, furnaceCookTime);
  }

  private Recipe toSmithingRecipe(final @NotNull NamespacedKey key) {
    if (this.smithTemplate == null || this.smithBase == null || this.smithAddition == null)
      throw new IllegalStateException("Smithing recipe without all parts: " + this.key);

    return new SmithingTransformRecipe(
      key,
      result.clone(),
      this.smithTemplate.choice(),
      this.smithBase.choice(),
      this.smithAddition.choice()
    );
  }

}
