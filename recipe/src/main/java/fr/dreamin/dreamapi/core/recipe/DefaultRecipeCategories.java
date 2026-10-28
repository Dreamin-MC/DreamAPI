package fr.dreamin.dreamapi.core.recipe;

import fr.dreamin.dreamapi.api.recipe.service.RecipeCategoryRegistryService;
import fr.dreamin.dreamapi.api.recipe.RecipeTag;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.Set;

public final class DefaultRecipeCategories {

  public static void registerDefaults(RecipeCategoryRegistryService registryService) {
    registryService.registerCategory(
      SimpleRecipeCategory.builder()
        .id("shaped")
        .name(Component.text("Crafts"))
        .icon(Material.CRAFTING_TABLE)
        .tags(Set.of(RecipeTag.of("dreamapi:shaped")))
        .build()
    );

    registryService.registerCategory(
      SimpleRecipeCategory.builder()
        .id("shapeless")
        .name(Component.text("Crafts not formated"))
        .icon(Material.PAPER)
        .tags(Set.of(RecipeTag.of("dreamapi:shapeless")))
        .build()
    );

    registryService.registerCategory(
      SimpleRecipeCategory.builder()
        .id("furnace")
        .name(Component.text("Furnace"))
        .icon(Material.FURNACE)
        .tags(Set.of(RecipeTag.of("dreamapi:furnace")))
        .build()
    );

    registryService.registerCategory(
      SimpleRecipeCategory.builder()
        .id("smithing")
        .name(Component.text("Smithing"))
        .icon(Material.SMITHING_TABLE)
        .tags(Set.of(RecipeTag.of("dreamapi:smithing")))
        .build()
    );

  }

}
