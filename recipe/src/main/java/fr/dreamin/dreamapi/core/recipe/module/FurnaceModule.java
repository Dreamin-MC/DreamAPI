package fr.dreamin.dreamapi.core.recipe.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.dreamin.dreamapi.api.recipe.model.IngredientDefinition;
import fr.dreamin.dreamapi.core.recipe.data.FurnaceData;

import java.io.IOException;
import java.util.HashMap;

public final class FurnaceModule extends SimpleModule {

  public FurnaceModule() {
    super("FurnaceModule");

    addSerializer(FurnaceData.class, new JsonSerializer<>() {
      @Override
      public void serialize(FurnaceData data, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        final var out = new HashMap<String, Object>();
        out.put("input", data.input());
        out.put("experience", data.experience());
        out.put("cookTime", data.cookTime());
        gen.writeObject(out);
      }
    });

    addDeserializer(FurnaceData.class, new JsonDeserializer<>() {
      @Override
      public FurnaceData deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        final var n = (JsonNode) p.getCodec().readTree(p);

        final var input = p.getCodec().treeToValue(n.get("input"), IngredientDefinition.class);
        final var xp = (float) n.get("experience").asDouble();
        final var time = n.get("cookTime").asInt();

        return new FurnaceData(input, xp, time);
      }
    });

  }

}
