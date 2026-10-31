package fr.dreamin.dreamapi.test.particle;

import fr.dreamin.dreamapi.api.interpolation.InterpolationType;
import fr.dreamin.dreamapi.core.interpolation.Interpolation;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Particle Interpolation Backward Compatibility Delegate Tests (Feature F6)")
class ParticleInterpolationDelegateTest {

  @Test
  @DisplayName("Verify enum mapping between particle InterpolationType and core InterpolationType")
  void testEnumConversionToAndFromCore() {
    for (final var type : InterpolationType.values()) {
      final var coreType = type.toCore();
      assertNotNull(coreType);
      assertEquals(type.name(), coreType.name());

      final var roundTrip = InterpolationType.fromCore(coreType);
      assertEquals(type, roundTrip);
    }
  }

  @Test
  @DisplayName("Verify scalar lerp delegation")
  void testScalarLerpDelegation() {
    final var lerpDouble = Interpolation.lerp(0.0, 10.0, 0.5);
    assertEquals(5.0, lerpDouble, 1e-6);

    final var lerpFloat = Interpolation.lerp(0.0f, 20.0f, 0.25);
    assertEquals(5.0f, lerpFloat, 1e-6f);
  }

  @Test
  @DisplayName("Verify Vector and Location lerp delegation")
  void testVectorAndLocationLerpDelegation() {
    final var v1 = new Vector(0, 0, 0);
    final var v2 = new Vector(10, 20, 30);
    final var vMid = Interpolation.lerp(v1, v2, 0.5);

    assertEquals(5.0, vMid.getX(), 1e-6);
    assertEquals(10.0, vMid.getY(), 1e-6);
    assertEquals(15.0, vMid.getZ(), 1e-6);

    final var loc1 = new Location(null, 0, 10, 20, 0.0f, 0.0f);
    final var loc2 = new Location(null, 100, 20, 40, 90.0f, 45.0f);
    final var locInterp = Interpolation.lerp(loc1, loc2, 0.5);

    assertEquals(50.0, locInterp.getX(), 1e-6);
    assertEquals(15.0, locInterp.getY(), 1e-6);
    assertEquals(30.0, locInterp.getZ(), 1e-6);
    assertEquals(45.0f, locInterp.getYaw(), 1e-6f);
    assertEquals(22.5f, locInterp.getPitch(), 1e-6f);
  }

  @Test
  @DisplayName("Verify easing curve delegation with legacy InterpolationType")
  void testEasingCurves() {
    for (final var type : InterpolationType.values()) {
      final var start = Interpolation.applyEasing(0.0, type);
      final var end = Interpolation.applyEasing(1.0, type);
      assertEquals(0.0, start, 1e-6);
      assertEquals(1.0, end, 1e-6);

      final var fn = Interpolation.getEasing(type);
      assertNotNull(fn);
      assertEquals(start, fn.apply(0.0), 1e-6);
      assertEquals(end, fn.apply(1.0), 1e-6);
    }
  }

  @Test
  @DisplayName("Verify sequence generation with legacy InterpolationType")
  void testGenerateSequences() {
    final var doubles = Interpolation.generate(0.0, 100.0, 10, InterpolationType.LINEAR);
    assertEquals(11, doubles.size());
    assertEquals(0.0, doubles.get(0), 1e-6);
    assertEquals(50.0, doubles.get(5), 1e-6);
    assertEquals(100.0, doubles.get(10), 1e-6);

    final var vectors = Interpolation.generate(new Vector(0, 0, 0), new Vector(10, 10, 10), 5, InterpolationType.EASE_IN);
    assertEquals(6, vectors.size());
    assertEquals(0.0, vectors.get(0).getX(), 1e-6);
    assertEquals(10.0, vectors.get(5).getX(), 1e-6);

    final var locations = Interpolation.generate(new Location(null, 0, 0, 0), new Location(null, 20, 20, 20), 4, InterpolationType.EASE_OUT);
    assertEquals(5, locations.size());
    assertEquals(0.0, locations.get(0).getX(), 1e-6);
    assertEquals(20.0, locations.get(4).getX(), 1e-6);
  }

  @Test
  @DisplayName("Verify legacy fluent Interpolator builder and async methods")
  void testFluentBuilder() throws Exception {
    final var list = Interpolation.between(0.0, 50.0)
      .over(5)
      .ease(InterpolationType.LINEAR)
      .asDoubles();

    assertEquals(6, list.size());
    assertEquals(0.0, list.get(0), 1e-6);
    assertEquals(50.0, list.get(5), 1e-6);

    final var future = Interpolation.between(new Vector(0, 0, 0), new Vector(10, 20, 30))
      .over(4)
      .ease(InterpolationType.EASE_IN_OUT)
      .asVectorsAsync();

    final var asyncVectors = future.get(5, TimeUnit.SECONDS);
    assertEquals(5, asyncVectors.size());
    assertEquals(10.0, asyncVectors.get(4).getX(), 1e-6);
  }
}
