package fr.dreamin.dreamapi.api.util.math;

import fr.dreamin.dreamapi.api.DreamAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * High-performance utility class for smooth mathematical interpolation of scalar values,
 * vectors, and locations.
 * Supports multiple easing curves, fluent builder pipelines, and async execution.
 *
 * @author Dreamin
 * @since 1.0.0
 */
public final class Interpolation {

  private Interpolation() {}

  // ###############################################################
  // ------------------- GENERIC INTERPOLATION ---------------------
  // ###############################################################

  /**
   * Linear interpolation between two double values.
   */
  public static double lerp(final double a, final double b, final double t) {
    return a + (b - a) * t;
  }

  /**
   * Linear interpolation between two float values.
   */
  public static float lerp(final float a, final float b, final double t) {
    return (float) (a + (b - a) * t);
  }

  /**
   * Linear interpolation between two vectors.
   */
  public static @NotNull Vector lerp(final @NotNull Vector a, final @NotNull Vector b, final double t) {
    return new Vector(
      lerp(a.getX(), b.getX(), t),
      lerp(a.getY(), b.getY(), t),
      lerp(a.getZ(), b.getZ(), t)
    );
  }

  /**
   * Linear interpolation between two locations (position + rotation).
   */
  public static @NotNull Location lerp(final @NotNull Location a, final @NotNull Location b, final double t) {
    final var x = lerp(a.getX(), b.getX(), t);
    final var y = lerp(a.getY(), b.getY(), t);
    final var z = lerp(a.getZ(), b.getZ(), t);
    final var yaw = lerp(a.getYaw(), b.getYaw(), t);
    final var pitch = lerp(a.getPitch(), b.getPitch(), t);
    return new Location(a.getWorld(), x, y, z, yaw, pitch);
  }

  // ###############################################################
  // ------------------- CURVE (EASING) HANDLING -------------------
  // ###############################################################

  /**
   * Applies the chosen easing function to a normalized progress t (0.0 to 1.0).
   */
  public static double applyEasing(final double t, final @NotNull InterpolationType type) {
    return switch (type) {
      case LINEAR -> t;
      case EASE_IN -> t * t;
      case EASE_OUT -> t * (2 - t);
      case EASE_IN_OUT -> t < 0.5
        ? 4 * t * t * t
        : 1 - Math.pow(-2 * t + 2, 3) / 2;
    };
  }

  /**
   * Returns the easing function as a {@link Function} of {@code Double} to {@code Double}.
   */
  public static @NotNull Function<Double, Double> getEasing(final @NotNull InterpolationType type) {
    return t -> applyEasing(t, type);
  }

  // ###############################################################
  // ---------------- DIRECT INTERPOLATE CONTRACT ------------------
  // ###############################################################

  /**
   * Interpolates a scalar value between start and end using the specified easing curve and progress.
   *
   * @param start starting value
   * @param end ending value
   * @param progress progress from 0.0 to 1.0
   * @param type easing curve
   * @return interpolated scalar
   */
  public static double interpolate(final double start, final double end, final double progress, final @NotNull InterpolationType type) {
    return lerp(start, end, applyEasing(progress, type));
  }

  /**
   * Interpolates a Vector between start and end using the specified easing curve and progress.
   */
  public static @NotNull Vector interpolate(final @NotNull Vector start, final @NotNull Vector end, final double progress, final @NotNull InterpolationType type) {
    return lerp(start, end, applyEasing(progress, type));
  }

  /**
   * Interpolates a Location between start and end using the specified easing curve and progress.
   */
  public static @NotNull Location interpolate(final @NotNull Location start, final @NotNull Location end, final double progress, final @NotNull InterpolationType type) {
    return lerp(start, end, applyEasing(progress, type));
  }

  // ###############################################################
  // ------------------- SEQUENCE GENERATION -----------------------
  // ###############################################################

  /**
   * Generates interpolated scalar values over time.
   */
  public static @NotNull List<Double> generate(final double start, final double end, final int steps, final @NotNull InterpolationType type) {
    final var list = new ArrayList<Double>(steps + 1);
    final var easing = getEasing(type);

    for (var i = 0; i <= steps; i++) {
      final var t = easing.apply((double) i / steps);
      list.add(lerp(start, end, t));
    }
    return list;
  }

  /**
   * Generates interpolated vectors over time.
   */
  public static @NotNull List<Vector> generate(final @NotNull Vector start, final @NotNull Vector end, final int steps, final @NotNull InterpolationType type) {
    final var list = new ArrayList<Vector>(steps + 1);
    final var easing = getEasing(type);

    for (var i = 0; i <= steps; i++) {
      final var t = easing.apply((double) i / steps);
      list.add(lerp(start, end, t));
    }
    return list;
  }

  /**
   * Generates interpolated locations over time.
   */
  public static @NotNull List<Location> generate(final @NotNull Location start, final @NotNull Location end, final int steps, final @NotNull InterpolationType type) {
    final var list = new ArrayList<Location>(steps + 1);
    final var easing = getEasing(type);

    for (var i = 0; i <= steps; i++) {
      final var t = easing.apply((double) i / steps);
      list.add(lerp(start, end, t));
    }
    return list;
  }

  // ###############################################################
  // ----------------------- FLUENT BUILDER ------------------------
  // ###############################################################

  public static @NotNull Interpolator between(final double start, final double end) {
    return new Interpolator().from(start).to(end);
  }

  public static @NotNull Interpolator between(final @NotNull Vector start, final @NotNull Vector end) {
    return new Interpolator().from(start).to(end);
  }

  public static @NotNull Interpolator between(final @NotNull Location start, final @NotNull Location end) {
    return new Interpolator().from(start).to(end);
  }

  // ###############################################################
  // ------------------------ INNER BUILDER ------------------------
  // ###############################################################

  /**
   * Fluent builder for generating interpolations.
   */
  public static final class Interpolator {
    private Object start;
    private Object end;
    private int steps = 20;
    private InterpolationType type = InterpolationType.LINEAR;

    public Interpolator from(final double start) { this.start = start; return this; }
    public Interpolator to(final double end) { this.end = end; return this; }

    public Interpolator from(final @NotNull Vector start) { this.start = start; return this; }
    public Interpolator to(final @NotNull Vector end) { this.end = end; return this; }

    public Interpolator from(final @NotNull Location start) { this.start = start; return this; }
    public Interpolator to(final @NotNull Location end) { this.end = end; return this; }

    public Interpolator over(final int steps) { this.steps = steps; return this; }
    public Interpolator ease(final @NotNull InterpolationType type) { this.type = type; return this; }

    @SuppressWarnings("unchecked")
    public @NotNull List<?> build() {
      if (start instanceof Double a && end instanceof Double b)
        return generate(a, b, steps, type);
      if (start instanceof Vector a && end instanceof Vector b)
        return generate(a, b, steps, type);
      if (start instanceof Location a && end instanceof Location b)
        return generate(a, b, steps, type);
      throw new IllegalArgumentException("Unsupported interpolation types: " + start + " -> " + end);
    }

    public @NotNull List<Double> asDoubles() { return (List<Double>) build(); }
    public @NotNull List<Vector> asVectors() { return (List<Vector>) build(); }
    public @NotNull List<Location> asLocations() { return (List<Location>) build(); }

    public @NotNull CompletableFuture<List<?>> buildAsync() {
      return CompletableFuture.supplyAsync(this::build);
    }

    public @NotNull CompletableFuture<List<Double>> asDoublesAsync() {
      return CompletableFuture.supplyAsync(this::asDoubles);
    }

    public @NotNull CompletableFuture<List<Vector>> asVectorsAsync() {
      return CompletableFuture.supplyAsync(this::asVectors);
    }

    public @NotNull CompletableFuture<List<Location>> asLocationsAsync() {
      return CompletableFuture.supplyAsync(this::asLocations);
    }

    public <T> CompletableFuture<Void> thenSync(
      final @NotNull CompletableFuture<List<T>> future,
      final @NotNull Consumer<List<T>> consumer
    ) {
      return future.thenAcceptAsync(list -> {
        final var plugin = DreamAPI.isInitialized() ? DreamAPI.getAPI().plugin() : null;
        if (plugin != null)
          Bukkit.getScheduler().runTask(plugin, () -> consumer.accept(list));
        else
          consumer.accept(list);
      });
    }

    public <T> CompletableFuture<Void> thenSync(
      final @NotNull Plugin plugin,
      final @NotNull CompletableFuture<List<T>> future,
      final @NotNull Consumer<List<T>> consumer
    ) {
      return future.thenAcceptAsync(list -> {
        Bukkit.getScheduler().runTask(plugin, () -> consumer.accept(list));
      });
    }
  }
}
