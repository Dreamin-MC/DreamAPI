package fr.dreamin.dreamapi.core.recipe.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.dreamin.dreamapi.api.recipe.condition.RecipeCondition;
import fr.dreamin.dreamapi.api.recipe.condition.RecipeConditionContext;
import fr.dreamin.dreamapi.api.recipe.condition.RecipeConditionRegistry;
import fr.dreamin.dreamapi.api.recipe.condition.RecipeConditionResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PlayerConditionModule extends SimpleModule {

  public PlayerConditionModule() {
    super("PlayerConditionModule");

    addSerializer(RecipeCondition.class, new JsonSerializer<>() {
      @Override
      public void serialize(RecipeCondition cond, JsonGenerator gen, SerializerProvider serializers) throws IOException {

        final var json = new LinkedHashMap<String, Object>();

        // -------------------------------------------
        // Trick: we store the type inside the PlayerCondition
        // via a wrapper class to identify built-in ones
        // -------------------------------------------

        if (cond instanceof TypedCondition typed) {
          json.put("type", typed.type());
          json.putAll(typed.data());
        } else
          json.put("type", "custom");

        gen.writeObject(json);
      }
    });

    addDeserializer(RecipeCondition.class, new JsonDeserializer<>() {
      @Override
      public RecipeCondition deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        final var root = (JsonNode) p.getCodec().readTree(p);

        final var type = root.get("type").asText();

        final var factory = RecipeConditionRegistry.get(type);
        if (factory == null)
          throw new IOException("Unknown PlayerCondition type: " + type);

        return factory.create(root);
      }
    });

  }

  // ###############################################################
  // ------------------------ RECORD CLASS -------------------------
  // ###############################################################

  public record TypedCondition(
    String type,
    Map<String, Object> data,
    RecipeCondition delegate
  ) implements RecipeCondition {

    @Override
    public @NotNull RecipeConditionResult test(@NotNull RecipeConditionContext context) {
      return RecipeConditionResult.allow();
    }
  }

}
