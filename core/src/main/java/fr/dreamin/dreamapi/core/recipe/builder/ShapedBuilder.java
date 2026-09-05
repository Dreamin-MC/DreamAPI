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

public final class ShapedBuilder {

  private final String key;
  private final Set<RecipeTag> tags = new HashSet<>();
  private final List<String> rows = new ArrayList<>();
  private final Map<Character, IngredientDefinition> ingredients = new HashMap<>();
  private ItemStack result;
  private RecipeCondition condition;
  private RecipeCraftingType recipeCraftingType = RecipeCraftingType.ALL;
  private List<Component> notPermittedMessage = new ArrayList<>();

  private ShapedBuilder(final @NotNull String key) {
    this.key = key;
  }

  public static ShapedBuilder builder(final @NotNull String key) {
    return new ShapedBuilder(key);
  }

  public ShapedBuilder tag(@NotNull RecipeTag tag) {
    this.tags.add(tag);
    return this;
  }

  public ShapedBuilder tag(@NotNull String namespace, @NotNull String key) {
    this.tags.add(RecipeTag.of(namespace, key));
    return this;
  }

  public ShapedBuilder tag(@NotNull String fullKey) {
    this.tags.add(RecipeTag.of(fullKey));
    return this;
  }

  public ShapedBuilder row(final @NotNull String pattern) {
    this.rows.add(pattern);
    return this;
  }

  public ShapedBuilder shape(final @NotNull String ... pattern) {
    this.rows.clear();
    this.rows.addAll(Arrays.asList(pattern));
    return this;
  }

  public ShapedBuilder ingredient(
    final char symbol,
    final @NotNull IngredientPredicate predicate,
    final @NotNull RecipeChoice choice,
    final @NotNull ItemStack displayItem
  ) {
    this.ingredients.put(symbol, new IngredientDefinition(predicate, choice, displayItem));
    return this;
  }

  public ShapedBuilder ingredient(final char symbol, final @NotNull Material material) {
    return ingredient(symbol, IngredientPredicate.ofMaterial(material), new RecipeChoice.MaterialChoice(material), new ItemStack(material));
  }

  public ShapedBuilder ingredient(final char symbol, final @NotNull ItemStack itemStack) {
    return ingredient(symbol, IngredientPredicate.ofExactItem(itemStack), new RecipeChoice.ExactChoice(itemStack), itemStack);
  }

  public ShapedBuilder ingredient(final char symbol, final @NotNull ItemDefinition itemDefinition) {
    DreamAPI.getAPI().getService(ItemRegistryService.class).register(itemDefinition);

    final var item = itemDefinition.getItem();

    return ingredient(symbol, IngredientPredicate.ofExactItem(item), new RecipeChoice.ExactChoice(item), item);
  }

  public ShapedBuilder result(final @NotNull ItemStack result) {
    this.result = result;
    return this;
  }

  public ShapedBuilder result(final @NotNull ItemDefinition itemDefinition) {
    DreamAPI.getAPI().getService(ItemRegistryService.class).register(itemDefinition);

    this.result = itemDefinition.getItem();
    return this;
  }

  public ShapedBuilder condition(final @NotNull RecipeCondition condition) {
    this.condition = condition;
    return this;
  }

  public ShapedBuilder craftingType(final @NotNull RecipeCraftingType type) {
    this.recipeCraftingType = type;
    return this;
  }

  public ShapedBuilder notPermittedMessage(final @NotNull List<Component> message) {
    this.notPermittedMessage = message;
    return this;
  }

  public ShapedBuilder notPermittedMessage(final @NotNull Component... message) {
    this.notPermittedMessage = List.of(message);
    return this;
  }


  public CustomRecipe build() {
    Objects.requireNonNull(this.key, "key");
    Objects.requireNonNull(this.result, "result");

    if (this.rows.isEmpty())
      throw new IllegalStateException("Shaped recipe requires at least one row");

    final var expectedLength = this.rows.getFirst().length();
    for (final var row : rows) {
      if (row.length() != expectedLength)
        throw new IllegalStateException("All rows must have same length in shaped recipe '" + key + "'");
    }

    final var shaped = new ArrayList<List<IngredientDefinition>>();

    for (final var row : this.rows) {
      final var defRow = new ArrayList<IngredientDefinition>();

      for (final var c : row.toCharArray()) {

        if (c == ' ') {
          defRow.add(null);
          continue;
        }

        final var def = this.ingredients.get(c);

        if (def == null)
          throw new IllegalStateException("Missing ingredient mapping for symbol '" + c + "'");

        defRow.add(def);
      }
      shaped.add(Collections.unmodifiableList(defRow));
    }

    return new CustomRecipeImpl(
      this.key,
      RecipeType.SHAPED,
      this.tags,
      Collections.unmodifiableList(shaped),
      null,
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
