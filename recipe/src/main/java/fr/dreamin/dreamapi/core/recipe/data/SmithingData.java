package fr.dreamin.dreamapi.core.recipe.data;

import fr.dreamin.dreamapi.api.recipe.IngredientDefinition;
import org.jetbrains.annotations.NotNull;

public record SmithingData(@NotNull IngredientDefinition template, @NotNull IngredientDefinition base, @NotNull IngredientDefinition addition) { }
