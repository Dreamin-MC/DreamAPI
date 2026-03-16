package fr.dreamin.dreamapi.core.cuboid.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.dreamin.dreamapi.api.config.Configurations;
import fr.dreamin.dreamapi.api.cuboid.MovableCuboid;
import fr.dreamin.dreamapi.core.bukkit.module.BukkitLocationModule;
import org.bukkit.Location;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Jackson module for serializing and deserializing MovableCuboid objects.
 *
 * @author Dreamin
 * @since 1.0.0
 */
public final class MovableCuboidModule extends SimpleModule {

  public MovableCuboidModule() {
    super ("MovableCuboidModule");

    addSerializer(MovableCuboid.class, new JsonSerializer<>() {
      @Override
      public void serialize(final MovableCuboid value, final JsonGenerator gen, final SerializerProvider serializerProvider) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("locA", value.getLocA());
        gen.writeObjectField("locB", value.getLocB());
        gen.writeEndObject();
      }
    });

    addDeserializer(MovableCuboid.class, new JsonDeserializer<>() {
      @Override
      public MovableCuboid deserialize(final JsonParser p, final DeserializationContext ctx) throws IOException {
        ObjectNode node = p.getCodec().readTree(p);

        final var locA = ctx.readTreeAsValue(node.get("locA"), Location.class);
        final var locB = ctx.readTreeAsValue(node.get("locB"), Location.class);

        return new MovableCuboid(locA, locB);
      }
    });
  }

}
