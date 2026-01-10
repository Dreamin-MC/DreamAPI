package fr.dreamin.dreamapi.api.recipe.service;

import fr.dreamin.dreamapi.api.recipe.RecipeCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface RecipeCategoryRegistryService {

  void registerCategory(final @NotNull RecipeCategory category);

  @Nullable RecipeCategory getCategory(final @NotNull String id);

  @NotNull Collection<RecipeCategory> getAllCategories();

}
