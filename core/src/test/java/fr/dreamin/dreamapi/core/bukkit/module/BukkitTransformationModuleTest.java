package fr.dreamin.dreamapi.core.bukkit.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BukkitTransformationModuleTest {

  private static final float EPSILON = 1.0e-6f;

  private final ObjectMapper mapper = new ObjectMapper()
    .registerModule(new BukkitTransformationModule());

  @Test
  void shouldDeserializeEmptyObjectWithSafeDefaults() throws Exception {
    final var transformation = mapper.readValue("{}", Transformation.class);

    assertVectorEquals(new Vector3f(0.0f, 0.0f, 0.0f), transformation.getTranslation());
    assertVectorEquals(new Vector3f(1.0f, 1.0f, 1.0f), transformation.getScale());
    assertQuaternionEquals(new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f), transformation.getLeftRotation());
    assertQuaternionEquals(new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f), transformation.getRightRotation());
  }

  @Test
  void shouldDeserializeLegacyNestedRotationsInsideScale() throws Exception {
    final String legacyJson = """
      {
        "translation": {"x": 1.0, "y": 2.0, "z": 3.0},
        "scale": {
          "x": 4.0,
          "y": 5.0,
          "z": 6.0,
          "rotation_left": {"x": 0.1, "y": 0.2, "z": 0.3, "w": 0.4},
          "rotation_right": {"x": 0.5, "y": 0.6, "z": 0.7, "w": 0.8}
        }
      }
      """;

    final var transformation = mapper.readValue(legacyJson, Transformation.class);

    assertVectorEquals(new Vector3f(1.0f, 2.0f, 3.0f), transformation.getTranslation());
    assertVectorEquals(new Vector3f(4.0f, 5.0f, 6.0f), transformation.getScale());
    assertQuaternionEquals(new Quaternionf(0.1f, 0.2f, 0.3f, 0.4f), transformation.getLeftRotation());
    assertQuaternionEquals(new Quaternionf(0.5f, 0.6f, 0.7f, 0.8f), transformation.getRightRotation());
  }

  @Test
  void shouldDeserializeCamelCaseRotationAliases() throws Exception {
    final String json = """
      {
        "translation": {"x": 7.0, "y": 8.0, "z": 9.0},
        "scale": {"x": 2.0, "y": 2.0, "z": 2.0},
        "rotationLeft": {"x": 0.0, "y": 0.0, "z": 0.0, "w": 1.0},
        "rotationRight": {"x": 0.2, "y": 0.3, "z": 0.4, "w": 0.5}
      }
      """;

    final var transformation = mapper.readValue(json, Transformation.class);

    assertVectorEquals(new Vector3f(7.0f, 8.0f, 9.0f), transformation.getTranslation());
    assertVectorEquals(new Vector3f(2.0f, 2.0f, 2.0f), transformation.getScale());
    assertQuaternionEquals(new Quaternionf(0.0f, 0.0f, 0.0f, 1.0f), transformation.getLeftRotation());
    assertQuaternionEquals(new Quaternionf(0.2f, 0.3f, 0.4f, 0.5f), transformation.getRightRotation());
  }

  @Test
  void shouldRoundTripTransformation() throws Exception {
    final var initial = new Transformation(
      new Vector3f(1.5f, -2.5f, 3.5f),
      new Quaternionf(0.1f, 0.2f, 0.3f, 0.9f),
      new Vector3f(2.0f, 3.0f, 4.0f),
      new Quaternionf(0.7f, 0.1f, 0.0f, 0.7f)
    );

    final var serialized = mapper.writeValueAsString(initial);
    final var restored = mapper.readValue(serialized, Transformation.class);

    assertVectorEquals(initial.getTranslation(), restored.getTranslation());
    assertVectorEquals(initial.getScale(), restored.getScale());
    assertQuaternionEquals(initial.getLeftRotation(), restored.getLeftRotation());
    assertQuaternionEquals(initial.getRightRotation(), restored.getRightRotation());
  }

  private static void assertVectorEquals(Vector3f expected, Vector3f actual) {
    assertEquals(expected.x, actual.x, EPSILON);
    assertEquals(expected.y, actual.y, EPSILON);
    assertEquals(expected.z, actual.z, EPSILON);
  }

  private static void assertQuaternionEquals(Quaternionf expected, Quaternionf actual) {
    assertEquals(expected.x, actual.x, EPSILON);
    assertEquals(expected.y, actual.y, EPSILON);
    assertEquals(expected.z, actual.z, EPSILON);
    assertEquals(expected.w, actual.w, EPSILON);
  }
}

