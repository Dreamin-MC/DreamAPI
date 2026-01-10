package fr.dreamin.dreamapi.core.recipe;

import fr.dreamin.dreamapi.api.recipe.RecipeCategory;
import fr.dreamin.dreamapi.api.recipe.RecipeTag;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Builder
@RequiredArgsConstructor
public final class SimpleRecipeCategory implements RecipeCategory {

  private final @NotNull String id;
  private final @NotNull Component name;
  private final @NotNull Material icon;
  private final @NotNull Set<RecipeTag> tags;
  private final int priority;

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public @NotNull String getId() {
    return this.id;
  }

  @Override
  public @NotNull Component getDisplayName() {
    return this.name;
  }

  @Override
  public @NotNull Material getIcon() {
    return this.icon;
  }

  @Override
  public @NotNull Set<RecipeTag> getAssociatedTags() {
    return this.tags;
  }

  @Override
  public int getPriority() {
    return priority;
  }
}
