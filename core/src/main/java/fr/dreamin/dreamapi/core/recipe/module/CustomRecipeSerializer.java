package fr.dreamin.dreamapi.core.recipe.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import fr.dreamin.dreamapi.api.recipe.CustomRecipe;
import fr.dreamin.dreamapi.api.recipe.RecipeTag;
import fr.dreamin.dreamapi.api.recipe.condition.RecipeCondition;
import fr.dreamin.dreamapi.core.recipe.data.FurnaceData;
import fr.dreamin.dreamapi.core.recipe.data.SmithingData;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class CustomRecipeSerializer extends JsonSerializer<CustomRecipe> {


  @Override
  public void serialize(CustomRecipe r, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    Map<String, Object> out = new LinkedHashMap<>();

    out.put("key", r.getKey());
    out.put("type", r.getType().name());
    out.put("tags", r.getTags().stream().map(RecipeTag::toString).toList());
    out.put("result", r.getResult());

    out.put("condition", new PlayerConditionModule.TypedCondition(
      "permission",
      Map.of("perm", "dreamapi.test"),
      r.getPlayerCondition().orElse(RecipeCondition.always())
    ));

    switch (r.getType()) {
      case SHAPED -> out.put("shape", r.getShape());
      case SHAPELESS -> out.put("ingredients", r.getIngredients());
      case FURNACE -> out.put("furnace", new FurnaceData(
        Objects.requireNonNull(r.getFurnaceInput()),
        r.getFurnaceExperience(),
        r.getFurnaceCookTime()
      ));
      case SMITHING -> out.put("smithing", new SmithingData(
        Objects.requireNonNull(r.getSmithingTemplate()),
        Objects.requireNonNull(r.getSmithingBase()),
        Objects.requireNonNull(r.getSmithingAddition())
      ));
    }

    gen.writeObject(out);

  }
}
