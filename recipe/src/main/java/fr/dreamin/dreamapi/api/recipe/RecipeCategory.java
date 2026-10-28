package fr.dreamin.dreamapi.api.recipe;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface RecipeCategory {

  @NotNull String getId();

  @NotNull Component getDisplayName();

  @NotNull Material getIcon();

  @NotNull Set<RecipeTag> getAssociatedTags();

  default int getPriority() {
    return 0;
  }

}
