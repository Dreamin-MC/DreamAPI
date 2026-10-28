package fr.dreamin.dreamapi.api.recipe.service;

import fr.dreamin.dreamapi.api.recipe.CustomRecipe;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface RecipeViewerService {

  void openFakeViewer(final @NotNull Player player, final @NotNull CustomRecipe recipe);

  void openVanillaPreview(final @NotNull Player player, final @NotNull CustomRecipe recipe);

}
