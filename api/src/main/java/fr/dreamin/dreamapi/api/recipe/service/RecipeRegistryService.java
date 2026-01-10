package fr.dreamin.dreamapi.api.recipe.service;

import fr.dreamin.dreamapi.api.recipe.CustomRecipe;
import fr.dreamin.dreamapi.api.recipe.RecipeTag;
import fr.dreamin.dreamapi.api.recipe.storage.RecipeStorageService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface RecipeRegistryService {

  RecipeStorageService getStorage();

  void loadStorage();

  void registerRecipe(final @NotNull CustomRecipe recipe);

  boolean unregisterRecipe(final @NotNull String key);

  @Nullable CustomRecipe getRecipe(final @NotNull String key);

  @NotNull List<CustomRecipe> getRecipesByTag(final @NotNull RecipeTag tag);

  @NotNull Collection<CustomRecipe> getAllRecipes();

  void reload();

}
