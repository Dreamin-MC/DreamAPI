package fr.dreamin.dreamapi.api.recipe.model;

import org.jetbrains.annotations.NotNull;

public interface RecipeRegistry {

  void injectIntoServer(final @NotNull CustomRecipe recipe);

  void removeFromServer(final @NotNull String key);

  void clearAll();

}
