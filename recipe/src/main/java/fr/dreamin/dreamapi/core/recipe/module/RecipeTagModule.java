package fr.dreamin.dreamapi.core.recipe.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.dreamin.dreamapi.api.recipe.RecipeTag;

import java.io.IOException;

public final class RecipeTagModule extends SimpleModule {

  public RecipeTagModule() {
    super("RecipeTagModule");

    addSerializer(RecipeTag.class, new JsonSerializer<>() {
      @Override
      public void serialize(RecipeTag tag, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(tag.toString());
      }
    });

    addDeserializer(RecipeTag.class, new JsonDeserializer<>() {
      @Override
      public RecipeTag deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        return RecipeTag.of(p.getValueAsString());
      }
    });

  }

}
