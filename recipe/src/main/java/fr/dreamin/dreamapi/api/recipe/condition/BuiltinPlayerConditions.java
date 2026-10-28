package fr.dreamin.dreamapi.api.recipe.condition;

public final class BuiltinPlayerConditions {

  public static void register() {
    RecipeConditionRegistry.register("always", node -> RecipeCondition.always());

    RecipeConditionRegistry.register("permission", node -> RecipeCondition.Default.permission(node.get("perm").asText()));

  }

}
