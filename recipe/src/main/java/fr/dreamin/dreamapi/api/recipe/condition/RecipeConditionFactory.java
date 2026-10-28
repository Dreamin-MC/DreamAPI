package fr.dreamin.dreamapi.api.recipe.condition;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;

public interface RecipeConditionFactory {
  RecipeCondition create(final @NotNull JsonNode node);
}
