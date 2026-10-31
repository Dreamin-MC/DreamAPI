package fr.dreamin.dreamapi.api.util.math;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Interpolation & Math Consolidation Tests (Feature F6)")
class InterpolationTest {

  private static final double EPSILON = 1e-6;

  @Test
  @DisplayName("Scalar and vector lerp calculations at boundaries and midpoint")
  void testLerp() {
    assertEquals(0.0, Interpolation.lerp(0.0, 10.0, 0.0), EPSILON);
    assertEquals(5.0, Interpolation.lerp(0.0, 10.0, 0.5), EPSILON);
    assertEquals(10.0, Interpolation.lerp(0.0, 10.0, 1.0), EPSILON);

    assertEquals(2.0f, Interpolation.lerp(0.0f, 4.0f, 0.5), (float) EPSILON);

    final var v1 = new Vector(0, 0, 0);
    final var v2 = new Vector(10, 20, 30);
    final var midV = Interpolation.lerp(v1, v2, 0.5);
    assertEquals(5.0, midV.getX(), EPSILON);
    assertEquals(10.0, midV.getY(), EPSILON);
    assertEquals(15.0, midV.getZ(), EPSILON);

    final var loc1 = new Location(null, 0, 0, 0, 0, 0);
    final var loc2 = new Location(null, 10, 20, 30, 90, 45);
    final var midLoc = Interpolation.lerp(loc1, loc2, 0.5);
    assertEquals(5.0, midLoc.getX(), EPSILON);
    assertEquals(10.0, midLoc.getY(), EPSILON);
    assertEquals(15.0, midLoc.getZ(), EPSILON);
    assertEquals(45.0, midLoc.getYaw(), EPSILON);
    assertEquals(22.5, midLoc.getPitch(), EPSILON);
  }

  @Test
  @DisplayName("Easing mathematical curves at midpoint and bounds")
  void testEasingCurves() {
    for (final var type : InterpolationType.values()) {
      assertEquals(0.0, Interpolation.applyEasing(0.0, type), EPSILON);
      assertEquals(1.0, Interpolation.applyEasing(1.0, type), EPSILON);
    }

    assertEquals(0.5, Interpolation.applyEasing(0.5, InterpolationType.LINEAR), EPSILON);
    assertEquals(0.25, Interpolation.applyEasing(0.5, InterpolationType.EASE_IN), EPSILON);
    assertEquals(0.75, Interpolation.applyEasing(0.5, InterpolationType.EASE_OUT), EPSILON);
    assertEquals(0.5, Interpolation.applyEasing(0.5, InterpolationType.EASE_IN_OUT), EPSILON);
  }

  @Test
  @DisplayName("Direct interpolate contract compliance for double, Vector, and Location")
  void testInterpolateContract() {
    final var d = Interpolation.interpolate(0.0, 100.0, 0.5, InterpolationType.EASE_IN);
    assertEquals(25.0, d, EPSILON);

    final var vStart = new Vector(0, 0, 0);
    final var vEnd = new Vector(100, 100, 100);
    final var vRes = Interpolation.interpolate(vStart, vEnd, 0.5, InterpolationType.EASE_OUT);
    assertEquals(75.0, vRes.getX(), EPSILON);
    assertEquals(75.0, vRes.getY(), EPSILON);
    assertEquals(75.0, vRes.getZ(), EPSILON);

    final var locStart = new Location(null, 0, 0, 0);
    final var locEnd = new Location(null, 100, 100, 100);
    final var locRes = Interpolation.interpolate(locStart, locEnd, 0.5, InterpolationType.LINEAR);
    assertEquals(50.0, locRes.getX(), EPSILON);
  }

  @Test
  @DisplayName("Sequence generation produces steps + 1 items with valid endpoints")
  void testSequenceGeneration() {
    final var steps = 10;
    final var doubles = Interpolation.generate(0.0, 10.0, steps, InterpolationType.LINEAR);
    assertEquals(steps + 1, doubles.size());
    assertEquals(0.0, doubles.get(0), EPSILON);
    assertEquals(10.0, doubles.get(steps), EPSILON);

    final var vectors = Interpolation.generate(new Vector(0, 0, 0), new Vector(10, 10, 10), steps, InterpolationType.LINEAR);
    assertEquals(steps + 1, vectors.size());
    assertEquals(0.0, vectors.get(0).getX(), EPSILON);
    assertEquals(10.0, vectors.get(steps).getX(), EPSILON);
  }

  @Test
  @DisplayName("Fluent builder API produces correct sequences and types")
  void testFluentBuilder() throws Exception {
    final var doubles = Interpolation.between(0.0, 50.0)
      .over(5)
      .ease(InterpolationType.LINEAR)
      .asDoubles();
    assertEquals(6, doubles.size());
    assertEquals(0.0, doubles.get(0), EPSILON);
    assertEquals(50.0, doubles.get(5), EPSILON);

    final var asyncDoubles = Interpolation.between(0.0, 100.0)
      .over(10)
      .ease(InterpolationType.LINEAR)
      .asDoublesAsync()
      .get();
    assertEquals(11, asyncDoubles.size());
    assertEquals(100.0, asyncDoubles.get(10), EPSILON);
  }
}
