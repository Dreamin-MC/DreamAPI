package fr.dreamin.dreamapi.api.recipe.condition;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record RecipeConditionResult(boolean allowed, List<Component> message) {

  public static RecipeConditionResult allow() {
    return new RecipeConditionResult(true, List.of());
  }

  public static RecipeConditionResult deny(final @NotNull Component... message) {
    return new RecipeConditionResult(false, List.of(message));
  }

  public static RecipeConditionResult deny(final @NotNull List<Component> message) {
    return new RecipeConditionResult(false, message);
  }

  public static RecipeConditionResult deny() {
    return new RecipeConditionResult(false, List.of());
  }

}
