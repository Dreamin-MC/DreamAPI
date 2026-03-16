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
import org.bukkit.World;

import java.io.IOException;

/**
 * Module for serializing and deserializing Bukkit World objects using Jackson.
 *
 * @author Dreamin
 * @since 1.0.0
 */
public class BukkitWorldModule extends SimpleModule {

  public BukkitWorldModule() {
    super("BukkitLocationModule");

    addSerializer(World.class, new JsonSerializer<>() {
      @Override
      public void serialize(World world, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeString(world.getName());
        gen.writeEndObject();
      }
    });

    addDeserializer(World.class, new JsonDeserializer<>() {
      @Override
      public World deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return Bukkit.getWorld(p.getValueAsString());
      }
    });

  }
}
