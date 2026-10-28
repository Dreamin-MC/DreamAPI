package fr.dreamin.dreamapi.core.particle.module;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.dreamin.dreamapi.api.config.Configurations;
import fr.dreamin.dreamapi.core.particle.model.ParticleSequence;
import fr.dreamin.dreamapi.core.particle.model.ParticleSequenceItem;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public final class ParticleSequenceModule extends SimpleModule {

  public ParticleSequenceModule() {
    super("ParticleSequenceModule");

    addSerializer(ParticleSequence.class, new JsonSerializer<ParticleSequence>() {
      @Override
      public void serialize(ParticleSequence value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        final var result = new HashMap<String, Object>();

        if (!Configurations.containModule(ParticleSequenceItemModule.class))
          Configurations.addModule(new ParticleSequenceItemModule());

        result.put("type", "ParticleSequence");
        result.put("items", value.items());
        result.put("loop", value.loop());

        gen.writeObject(result);
      }
    });

    addDeserializer(ParticleSequence.class, new JsonDeserializer<ParticleSequence>() {
      @Override
      public ParticleSequence deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        final var node = (JsonNode) jsonParser.getCodec().readTree(jsonParser);

        if (!Configurations.containModule(ParticleSequenceItemModule.class))
          Configurations.addModule(new ParticleSequenceItemModule());

        final var items = jsonParser.getCodec().readValue(node.get("items").traverse(jsonParser.getCodec()), new TypeReference<List<ParticleSequenceItem>>() {});
        final var loop = node.get("loop").asBoolean();

        final var builder = ParticleSequence.create().loop(loop);
        items.forEach(builder::add);

        return builder.build();
      }
    });

  }

}
