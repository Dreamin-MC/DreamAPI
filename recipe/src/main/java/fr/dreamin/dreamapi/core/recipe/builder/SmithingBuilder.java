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

public final class SmithingBuilder {

  private final String key;
  private final Set<RecipeTag> tags = new HashSet<>();
  private IngredientDefinition template, base, addition;
  private ItemStack result;
  private RecipeCondition condition;
  private List<Component> notPermittedMessage = new ArrayList<>();

  private SmithingBuilder(final @NotNull String key) {
    this.key = key;
  }

  public static SmithingBuilder builder(final @NotNull String key) {
    return new SmithingBuilder(key);
  }

  public SmithingBuilder tag(@NotNull RecipeTag tag) {
    this.tags.add(tag);
    return this;
  }

  public SmithingBuilder tag(@NotNull String namespace, @NotNull String key) {
    this.tags.add(RecipeTag.of(namespace, key));
    return this;
  }

  public SmithingBuilder tag(@NotNull String fullKey) {
    this.tags.add(RecipeTag.of(fullKey));
    return this;
  }

  public SmithingBuilder template(final @NotNull Material material) {
    this.template = new IngredientDefinition(
      IngredientPredicate.ofMaterial(material),
      new RecipeChoice.MaterialChoice(material),
      new ItemStack(material)
    );

    return this;
  }

  public SmithingBuilder base(final @NotNull Material material) {
    this.base = new IngredientDefinition(
      IngredientPredicate.ofMaterial(material),
      new RecipeChoice.MaterialChoice(material),
      new ItemStack(material)
    );

    return this;
  }

  public SmithingBuilder addition(final @NotNull Material material) {
    this.addition = new IngredientDefinition(
      IngredientPredicate.ofMaterial(material),
      new RecipeChoice.MaterialChoice(material),
      new ItemStack(material)
    );

    return this;
  }

  public SmithingBuilder template(final @NotNull ItemStack item) {
    this.template = new IngredientDefinition(
      IngredientPredicate.ofExactItem(item),
      new RecipeChoice.ExactChoice(item),
      item
    );

    return this;
  }

  public SmithingBuilder base(final @NotNull ItemStack item) {
    this.base = new IngredientDefinition(
      IngredientPredicate.ofExactItem(item),
      new RecipeChoice.ExactChoice(item),
      item
    );

    return this;
  }

  public SmithingBuilder addition(final @NotNull ItemStack item) {
    this.addition = new IngredientDefinition(
      IngredientPredicate.ofExactItem(item),
      new RecipeChoice.ExactChoice(item),
      item
    );

    return this;
  }

  public SmithingBuilder addition(final @NotNull ItemDefinition itemDefinition) {
    final var item = itemDefinition.getItem();

    this.addition = new IngredientDefinition(
      IngredientPredicate.ofExactItem(item),
      new RecipeChoice.ExactChoice(item),
      item
    );

    return this;
  }

  public SmithingBuilder result(final @NotNull ItemStack result) {
    this.result = result;
    return this;
  }

  public SmithingBuilder result(final @NotNull ItemDefinition itemDefinition) {
    DreamAPI.getAPI().getService(ItemRegistryService.class).register(itemDefinition);

    this.result = itemDefinition.getItem();
    return this;
  }

  public SmithingBuilder condition(final @NotNull RecipeCondition condition) {
    this.condition = condition;
    return this;
  }

  public SmithingBuilder notPermittedMessage(final @NotNull List<Component> message) {
    this.notPermittedMessage = message;
    return this;
  }

  public SmithingBuilder notPermittedMessage(final @NotNull Component... message) {
    this.notPermittedMessage = List.of(message);
    return this;
  }


  public CustomRecipe build() {
    Objects.requireNonNull(this.key, "key");
    Objects.requireNonNull(this.template, "template");
    Objects.requireNonNull(this.base, "base");
    Objects.requireNonNull(this.addition, "addition");
    Objects.requireNonNull(this.result, "result");

    return new CustomRecipeImpl(
      this.key,
      RecipeType.SMITHING,
      this.tags,
      null,
      null,
      this.result,
      this.condition,
      null,
      null,
      0.0f,
      0,
      this.template,
      this.base,
      this.addition
    );

  }

}
