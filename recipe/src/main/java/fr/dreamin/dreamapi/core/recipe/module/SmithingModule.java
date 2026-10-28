package fr.dreamin.dreamapi.core.recipe.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.dreamin.dreamapi.api.recipe.IngredientDefinition;
import fr.dreamin.dreamapi.core.recipe.data.SmithingData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class SmithingModule extends SimpleModule {

  public SmithingModule() {
    super("SmithingModule");

    addSerializer(SmithingData.class, new JsonSerializer<>() {
      @Override
      public void serialize(SmithingData data, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        final var out = new HashMap<String, Object>();
        out.put("template", data.template());
        out.put("base", data.base());
        out.put("addition", data.addition());
        gen.writeObject(out);
      }
    });

    addDeserializer(SmithingData.class, new JsonDeserializer<>() {
      @Override
      public SmithingData deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        final var n = (JsonNode) p.getCodec().readTree(p);
        return new SmithingData(
          p.getCodec().treeToValue(n.get("template"), IngredientDefinition.class),
          p.getCodec().treeToValue(n.get("base"), IngredientDefinition.class),
          p.getCodec().treeToValue(n.get("addition"), IngredientDefinition.class)
        );
      }
    });

  }

}
