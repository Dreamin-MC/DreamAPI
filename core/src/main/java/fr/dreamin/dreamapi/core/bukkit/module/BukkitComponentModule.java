package fr.dreamin.dreamapi.core.bukkit.module;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.IOException;

public final class BukkitComponentModule extends SimpleModule {

  public BukkitComponentModule() {
    super("BukkitComponentModule");

    addSerializer(Component.class, new JsonSerializer<>() {

      @Override
      public void serialize(Component component, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        gen.writeString(MiniMessage.miniMessage().serialize(component));
      }

    });

    addDeserializer(Component.class, new JsonDeserializer<>() {

      @Override
      public Component deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return MiniMessage.miniMessage().deserialize(jsonParser.getText());
      }

    });

  }

}
