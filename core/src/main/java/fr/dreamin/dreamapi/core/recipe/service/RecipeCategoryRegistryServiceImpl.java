package fr.dreamin.dreamapi.core.recipe.service;

import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.api.recipe.service.RecipeCategoryRegistryService;
import fr.dreamin.dreamapi.core.recipe.DefaultRecipeCategories;
import fr.dreamin.dreamapi.api.recipe.RecipeCategory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@DreamAutoService(RecipeCategoryRegistryService.class)
public final class RecipeCategoryRegistryServiceImpl implements RecipeCategoryRegistryService, DreamService {

  private final Map<String, RecipeCategory> categories = new HashMap<>();

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void onLoad(@NotNull Plugin plugin) {
    DefaultRecipeCategories.registerDefaults(this);
  }

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public void registerCategory(@NotNull RecipeCategory category) {
    this.categories.put(category.getId().toLowerCase(), category);
  }

  @Override
  public @Nullable RecipeCategory getCategory(@NotNull String id) {
    return this.categories.get(id.toLowerCase());
  }

  @Override
  public @NotNull Collection<RecipeCategory> getAllCategories() {
    return Collections.unmodifiableCollection(this.categories.values());
  }
}
