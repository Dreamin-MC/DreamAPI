package fr.dreamin.dreamapi.core.recipe.data;

import fr.dreamin.dreamapi.api.recipe.IngredientDefinition;
import org.jetbrains.annotations.NotNull;

public record FurnaceData(@NotNull IngredientDefinition input, float experience, int cookTime) { }
