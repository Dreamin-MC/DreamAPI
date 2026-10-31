package fr.dreamin.dreamapi.core.bukkit.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Map;

/**
 * Module for serializing and deserializing Bukkit ItemStack objects using Jackson.
 * <p>
 * Serializes {@link ItemStack} instances as compact binary byte arrays using Paper 26.x
 * {@link ItemStack#serializeAsBytes()} and deserializes via {@link ItemStack#deserializeBytes(byte[])}.
 * Seamlessly falls back to legacy Map deserialization when encountering legacy JSON objects
 * ({@link JsonToken#START_OBJECT}).
 * </p>
 *
 * @author Dreamin
 * @since 1.0.0
 */
public class BukkitItemStackModule extends SimpleModule {

  public BukkitItemStackModule() {
    super("BukkitItemStackModule");

    addSerializer(ItemStack.class, new JsonSerializer<>() {
      @Override
      public void serialize(final ItemStack value, final JsonGenerator gen, final SerializerProvider serializerProvider) throws IOException {
        if (value == null) {
          gen.writeNull();
          return;
        }
        gen.writeBinary(value.serializeAsBytes());
      }
    });

    addDeserializer(ItemStack.class, new JsonDeserializer<>() {
      @Override
      public ItemStack deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
        var token = jsonParser.currentToken();
        if (token == null)
          token = jsonParser.nextToken();

        if (token == JsonToken.VALUE_NULL)
          return null;

        // Legacy fallback: JSON Object map serialization
        if (token == JsonToken.START_OBJECT) {
          final var mapper = (ObjectMapper) jsonParser.getCodec();
          final var typeFactory = mapper.getTypeFactory();
          final var mapType = typeFactory.constructMapType(Map.class, String.class, Object.class);

          return ItemStack.deserialize(mapper.readValue(jsonParser, mapType));
        }

        // Modern Paper 26.x binary serialization
        final var bytes = jsonParser.getBinaryValue();
        if (bytes == null || bytes.length == 0)
          return null;

        return ItemStack.deserializeBytes(bytes);
      }

      @Override
      public ItemStack getNullValue(final DeserializationContext ctxt) {
        return null;
      }
    });
  }

}
