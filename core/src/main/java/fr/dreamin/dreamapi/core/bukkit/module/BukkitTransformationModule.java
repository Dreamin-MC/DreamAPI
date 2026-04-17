package fr.dreamin.dreamapi.core.bukkit.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
        gen.writeEndObject();

        gen.writeObjectFieldStart("scale");
        writeVector3f(gen, value.getScale());
        gen.writeEndObject();

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
      public Transformation deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        final JsonNode node = p.getCodec().readTree(p);
        if (node == null || node.isNull())
          return defaultTransformation();

        final JsonNode scaleNode = firstPresent(node, "scale");

        final var translation = readVector3f(
          firstPresent(node, "translation"),
          new Vector3f(0.0f, 0.0f, 0.0f)
        );
        final var scale = readVector3f(
          scaleNode,
          new Vector3f(1.0f, 1.0f, 1.0f)
        );

        final var rotationLeft = readQuaternionf(
          firstPresent(node, "rotation_left", "rotationLeft", "left_rotation", "leftRotation"),
          firstPresent(scaleNode, "rotation_left", "rotationLeft", "left_rotation", "leftRotation"),
          new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)
        );
        final var rotationRight = readQuaternionf(
          firstPresent(node, "rotation_right", "rotationRight", "right_rotation", "rightRotation"),
          firstPresent(scaleNode, "rotation_right", "rotationRight", "right_rotation", "rightRotation"),
          new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)
        );

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

  private Transformation defaultTransformation() {
    return new Transformation(
      new Vector3f(0.0f, 0.0f, 0.0f),
      new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f),
      new Vector3f(1.0f, 1.0f, 1.0f),
      new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)
    );
  }

  private JsonNode firstPresent(JsonNode source, String... keys) {
    if (source == null || source.isNull()) {
      return null;
    }

    for (final var key : keys) {
      final var value = source.get(key);
      if (value != null && !value.isNull()) {
        return value;
      }
    }

    return null;
  }

  private Vector3f readVector3f(JsonNode vectorNode, Vector3f fallback) {
    if (vectorNode == null || vectorNode.isNull()) {
      return new Vector3f(fallback);
    }

    return new Vector3f(
      (float) vectorNode.path("x").asDouble(fallback.x),
      (float) vectorNode.path("y").asDouble(fallback.y),
      (float) vectorNode.path("z").asDouble(fallback.z)
    );
  }

  private Quaternionf readQuaternionf(JsonNode primaryNode, JsonNode secondaryNode, Quaternionf fallback) {
    final var quaternionNode = primaryNode != null && !primaryNode.isNull() ? primaryNode : secondaryNode;
    if (quaternionNode == null || quaternionNode.isNull()) {
      return new Quaternionf(fallback);
    }

    return new Quaternionf(
      (float) quaternionNode.path("x").asDouble(fallback.x),
      (float) quaternionNode.path("y").asDouble(fallback.y),
      (float) quaternionNode.path("z").asDouble(fallback.z),
      (float) quaternionNode.path("w").asDouble(fallback.w)
    );
  }

}
