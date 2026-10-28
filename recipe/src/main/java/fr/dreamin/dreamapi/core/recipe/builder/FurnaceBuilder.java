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

public final class FurnaceBuilder {

  private final String key;
  private final Set<RecipeTag> tags = new HashSet<>();
  private IngredientDefinition input;
  private ItemStack result;
  private float exp = 0.0f;
  private int cookTIme = 200;
  private RecipeCondition condition;
  private List<Component> notPermittedMessage = new ArrayList<>();

  private FurnaceBuilder(final @NotNull String key) {
    this.key = key;
  }

  public static FurnaceBuilder builder(final @NotNull String key) {
    return new FurnaceBuilder(key);
  }

  public FurnaceBuilder tag(@NotNull RecipeTag tag) {
    this.tags.add(tag);
    return this;
  }

  public FurnaceBuilder tag(@NotNull String namespace, @NotNull String key) {
    this.tags.add(RecipeTag.of(namespace, key));
    return this;
  }

  public FurnaceBuilder tag(@NotNull String fullKey) {
    this.tags.add(RecipeTag.of(fullKey));
    return this;
  }

  public FurnaceBuilder input(final @NotNull Material material) {
    this.input = new IngredientDefinition(
      IngredientPredicate.ofMaterial(material),
      new RecipeChoice.MaterialChoice(material),
      new ItemStack(material)
    );

    return this;
  }

  public FurnaceBuilder input(final @NotNull ItemStack item) {
    this.input = new IngredientDefinition(
      IngredientPredicate.ofExactItem(item),
      new RecipeChoice.ExactChoice(item),
      item
    );

    return this;
  }

  public FurnaceBuilder input(final @NotNull ItemDefinition itemDefinition) {
    DreamAPI.getAPI().getService(ItemRegistryService.class).register(itemDefinition);

    final var item = itemDefinition.getItem();

    this.input = new IngredientDefinition(
      IngredientPredicate.ofExactItem(item),
      new RecipeChoice.ExactChoice(item),
      item
    );

    return this;
  }

  public FurnaceBuilder result(final @NotNull ItemStack result) {
    this.result = result;
    return this;
  }

  public FurnaceBuilder result(final @NotNull ItemDefinition itemDefinition) {
    DreamAPI.getAPI().getService(ItemRegistryService.class).register(itemDefinition);

    this.result = itemDefinition.getItem();
    return this;
  }

  public FurnaceBuilder experience(final float exp) {
    this.exp = exp;
    return this;
  }

  public FurnaceBuilder cookTime(final int ticks) {
    this.cookTIme = ticks;
    return this;
  }

  public FurnaceBuilder condition(final @NotNull RecipeCondition condition) {
    this.condition = condition;
    return this;
  }

  public FurnaceBuilder notPermittedMessage(final @NotNull List<Component> message) {
    this.notPermittedMessage = message;
    return this;
  }

  public FurnaceBuilder notPermittedMessage(final @NotNull Component... message) {
    this.notPermittedMessage = List.of(message);
    return this;
  }

  public CustomRecipe build() {
    Objects.requireNonNull(this.key, "key");
    Objects.requireNonNull(this.input, "input");
    Objects.requireNonNull(this.result, "result");

    return new CustomRecipeImpl(
      this.key,
      RecipeType.FURNACE,
      this.tags,
      null,
      null,
      this.result,
      this.condition,
      null,
      this.input,
      this.exp,
      this.cookTIme,
      null,
      null,
      null
    );
  }

}
