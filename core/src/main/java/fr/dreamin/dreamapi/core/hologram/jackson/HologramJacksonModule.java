package fr.dreamin.dreamapi.core.hologram.jackson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.dreamin.dreamapi.api.hologram.model.DreamHologram;
import fr.dreamin.dreamapi.api.hologram.model.Hologram;
import fr.dreamin.dreamapi.api.hologram.model.HologramConfig;
import fr.dreamin.dreamapi.api.hologram.model.line.HologramLine;
import org.bukkit.Location;

import java.io.IOException;
import java.util.List;

public final class HologramJacksonModule extends SimpleModule {

  public HologramJacksonModule() {
    super("DreamHologramModule");

    addSerializer(Hologram.class, new JsonSerializer<>() {

      @Override
      public void serialize(Hologram hologram, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("id", hologram.getId());
        gen.writeObjectField("config", hologram.getConfig());
        gen.writeObjectField("location", hologram.getLocation());
        gen.writeObjectField("lines", hologram.getLines());
        gen.writeEndObject();
      }

    });

    addDeserializer(Hologram.class, new JsonDeserializer<>() {

      @Override
      public Hologram deserialize(JsonParser p, DeserializationContext ctx) throws IOException, JacksonException {
        final ObjectMapper mapper = (ObjectMapper) p.getCodec();
        final JsonNode root = mapper.readTree(p);

        final var id = root.get("id").asText();
        final var cfg = mapper.treeToValue(root.get("config"), HologramConfig.class);
        final var location = root.has("location") &&
          !root.get("location").isNull()
          ? mapper.treeToValue(root.get("location"), Location.class)
          : null;
        final var lines = mapper.convertValue(
          root.get("lines"),
          new TypeReference<List<HologramLine>>() {}
        );

        return new DreamHologram(id, cfg, location, lines);
      }

    });

  }

}
