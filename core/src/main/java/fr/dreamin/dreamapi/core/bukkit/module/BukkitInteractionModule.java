package fr.dreamin.dreamapi.core.bukkit.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Interaction;

import java.io.IOException;

/**
 * Module for serializing and deserializing Bukkit Interaction objects using Jackson.
 *
 * @author Dreamin
 * @since 1.0.0
 */
public class BukkitInteractionModule extends SimpleModule {

  public BukkitInteractionModule() {
    super("BukkitLocationModule");


    addSerializer(Interaction.class, new JsonSerializer<>() {
      @Override
      public void serialize(Interaction interaction, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("world", interaction.getWorld().getName());
        gen.writeNumberField("x", interaction.getX());
        gen.writeNumberField("y", interaction.getY());
        gen.writeNumberField("z", interaction.getZ());
        gen.writeNumberField("height", interaction.getInteractionHeight());
        gen.writeNumberField("width", interaction.getInteractionWidth());
        gen.writeEndObject();
      }
    });

    addDeserializer(Interaction.class, new JsonDeserializer<>() {
      @Override
      public Interaction deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectNode node = p.getCodec().readTree(p);

        final var worldName = node.get("world").asText();
        final var world = Bukkit.getWorld(worldName);

        final var x = node.get("x").asDouble();
        final var y = node.get("y").asDouble();
        final var z = node.get("z").asDouble();

        final var height = node.get("height").asDouble();
        final var width = node.get("width").asDouble();

        if (world == null)
          return null;

        return world.spawn(new Location(world, x, y ,z ), Interaction.class, interact -> {
          interact.setInteractionWidth((float) width);
          interact.setInteractionHeight((float) height);
        });

      }
    });
  }
}
