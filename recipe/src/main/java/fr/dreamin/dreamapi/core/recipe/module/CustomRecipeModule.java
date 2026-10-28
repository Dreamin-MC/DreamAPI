package fr.dreamin.dreamapi.core.recipe.module;

import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.dreamin.dreamapi.api.config.Configurations;
import fr.dreamin.dreamapi.api.recipe.model.CustomRecipe;

public final class CustomRecipeModule extends SimpleModule {

  public CustomRecipeModule() {
    super("CustomRecipeModule");

    Configurations.addModule(new IngredientDefinitionModule());
    Configurations.addModule(new RecipeTagModule());
    Configurations.addModule(new FurnaceModule());
    Configurations.addModule(new SmithingModule());
    Configurations.addModule(new PlayerConditionModule());

    addDeserializer(CustomRecipe.class, new CustomRecipeDeserializer());
    addSerializer(CustomRecipe.class, new CustomRecipeSerializer());

  }
}
