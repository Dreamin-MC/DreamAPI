package fr.dreamin.dreamapi.core.bukkit.module;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;

import java.io.IOException;

public final class BukkitBlockDataModule extends SimpleModule {

  public BukkitBlockDataModule() {
    super("BukkitBlockDataModule");

    addSerializer(BlockData.class, new JsonSerializer<>() {

      @Override
      public void serialize(BlockData blockData, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        gen.writeString(blockData.getAsString());
      }

    });

    addDeserializer(BlockData.class, new JsonDeserializer<>() {

      @Override
      public BlockData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return Bukkit.createBlockData(jsonParser.getText());
      }

    });

  }

}
