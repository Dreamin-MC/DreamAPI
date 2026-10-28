package fr.dreamin.dreamapi.core.recipe.builder;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.item.ItemDefinition;
import fr.dreamin.dreamapi.api.item.ItemRegistryService;
import fr.dreamin.dreamapi.api.recipe.*;
import fr.dreamin.dreamapi.api.recipe.condition.RecipeCondition;
import fr.dreamin.dreamapi.core.recipe.impl.CustomRecipeImpl;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ShapelessBuilder {

  private final String key;
  private final Set<RecipeTag> tags = new HashSet<>();
  private final List<IngredientDefinition> ingredients = new ArrayList<>();
  private ItemStack result;
  private RecipeCondition condition;
  private RecipeCraftingType recipeCraftingType = RecipeCraftingType.ALL;
  private List<Component> notPermittedMessage = new ArrayList<>();

  private ShapelessBuilder(final @NotNull String key) {
    this.key = key;
  }

  public static ShapelessBuilder builder(final @NotNull String key) {
    return new ShapelessBuilder(key);
  }

  public ShapelessBuilder tag(@NotNull RecipeTag tag) {
    this.tags.add(tag);
    return this;
  }

  public ShapelessBuilder tag(@NotNull String namespace, @NotNull String key) {
    this.tags.add(RecipeTag.of(namespace, key));
    return this;
  }

  public ShapelessBuilder tag(@NotNull String fullKey) {
    this.tags.add(RecipeTag.of(fullKey));
    return this;
  }

  public ShapelessBuilder add(final @NotNull IngredientPredicate predicate, final @NotNull RecipeChoice choice, final @NotNull ItemStack itemStack) {
    this.ingredients.add(new IngredientDefinition(predicate, choice, itemStack));
    return this;
  }

  public ShapelessBuilder add(final @NotNull Material material) {
    return add(IngredientPredicate.ofMaterial(material), new RecipeChoice.MaterialChoice(material), new ItemStack(material));
  }

  public ShapelessBuilder add(final @NotNull ItemStack itemStack) {
    return add(IngredientPredicate.ofExactItem(itemStack), new RecipeChoice.ExactChoice(itemStack), itemStack);
  }

  public ShapelessBuilder add(final @NotNull ItemDefinition itemDefinition) {
    DreamAPI.getAPI().getService(ItemRegistryService.class).register(itemDefinition);

    final var item = itemDefinition.getItem();

    return add(IngredientPredicate.ofExactItem(item), new RecipeChoice.ExactChoice(item), item);
  }

  public ShapelessBuilder result(final @NotNull ItemStack result) {
    this.result = result;
    return this;
  }

  public ShapelessBuilder result(final @NotNull ItemDefinition itemDefinition) {
    DreamAPI.getAPI().getService(ItemRegistryService.class).register(itemDefinition);

    this.result = itemDefinition.getItem();
    return this;
  }


  public ShapelessBuilder condition(final @NotNull RecipeCondition condition) {
    this.condition = condition;
    return this;
  }

  public ShapelessBuilder craftingType(final @NotNull RecipeCraftingType type) {
    this.recipeCraftingType = type;
    return this;
  }

  public ShapelessBuilder notPermittedMessage(final @NotNull List<Component> message) {
    this.notPermittedMessage = message;
    return this;
  }

  public ShapelessBuilder notPermittedMessage(final @NotNull Component... message) {
    this.notPermittedMessage = List.of(message);
    return this;
  }


  public CustomRecipe build() {
    Objects.requireNonNull(this.key, "key");
    Objects.requireNonNull(this.result, "result");

    if (this.ingredients.isEmpty())
      throw new IllegalStateException("Shapeless recipe requires at least one ingredient");

    return new CustomRecipeImpl(
      this.key,
      RecipeType.SHAPELESS,
      this.tags,
      null,
      List.copyOf(this.ingredients),
      this.result,
      this.condition,
      this.recipeCraftingType,
      null,
      0.0f,
      0,
      null,
      null,
       null
    );

  }

}
