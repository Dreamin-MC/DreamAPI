package fr.dreamin.dreamapi.api.recipe.condition;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class RecipeConditionRegistry {

  private static final Map<String, RecipeConditionFactory> FACTORIES = new HashMap<>();

  public static void register(final @NotNull String id, final @NotNull RecipeConditionFactory factory) {
    FACTORIES.put(id.toLowerCase(), factory);
  }

  public static RecipeConditionFactory get(final @NotNull String id) {
    return FACTORIES.get(id.toLowerCase());
  }

}
