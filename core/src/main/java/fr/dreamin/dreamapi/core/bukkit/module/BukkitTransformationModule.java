package fr.dreamin.dreamapi.core.bukkit.module;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;

/**
 * Module for serializing and deserializing Bukkit Transformation objects using Jackson.
 *
 * @author Dreamin
 * @since 1.0.0
 */
public final class BukkitTransformationModule extends SimpleModule {

  public BukkitTransformationModule() {
    super("BukkitTransformationModule");

    addSerializer(Transformation.class, new JsonSerializer<>() {
      @Override
      public void serialize(Transformation value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        gen.writeObjectFieldStart("translation");
        writeVector3f(gen, value.getTranslation());
        gen.writeEndObject();;

        gen.writeObjectFieldStart("scale");
        writeVector3f(gen, value.getScale());

        gen.writeObjectFieldStart("rotation_left");
        writeQuaternionf(gen, value.getLeftRotation());
        gen.writeEndObject();

        gen.writeObjectFieldStart("rotation_right");
        writeQuaternionf(gen, value.getRightRotation());
        gen.writeEndObject();

        gen.writeEndObject();
      }
    });

    addDeserializer(Transformation.class, new JsonDeserializer<>() {
      @Override
      public Transformation deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        ObjectNode node = p.getCodec().readTree(p);

        final var translation = readVector3f(node.get("translation"));
        final var scale = readVector3f(node.get("scale"));

        final var rotationLeft = readQuaternionf(node.get("rotation_left"));
        final var rotationRight = readQuaternionf(node.get("rotation_right"));

        return new Transformation(translation, rotationLeft, scale, rotationRight);
      }
    });
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void writeVector3f(JsonGenerator gen, Vector3f vector) throws IOException {
    gen.writeNumberField("x", vector.x);
    gen.writeNumberField("y", vector.y);
    gen.writeNumberField("z", vector.z);
  }

  private void writeQuaternionf(JsonGenerator gen, Quaternionf quaternion) throws IOException {
    gen.writeNumberField("x", quaternion.x);
    gen.writeNumberField("y", quaternion.y);
    gen.writeNumberField("z", quaternion.z);
    gen.writeNumberField("w", quaternion.w);
  }

  private Vector3f readVector3f(JsonNode vectorNode) {
    return new Vector3f(
      (float) vectorNode.get("x").asDouble(),
      (float) vectorNode.get("y").asDouble(),
      (float) vectorNode.get("z").asDouble()
    );
  }

  private Quaternionf readQuaternionf(JsonNode quaternionNode) {
    return new Quaternionf(
      (float) quaternionNode.get("x").asDouble(),
      (float) quaternionNode.get("y").asDouble(),
      (float) quaternionNode.get("z").asDouble(),
      (float) quaternionNode.get("w").asDouble()
    );
  }

}
