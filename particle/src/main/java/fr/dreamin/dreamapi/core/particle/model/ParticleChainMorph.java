package fr.dreamin.dreamapi.core.particle.model;

import fr.dreamin.dreamapi.api.util.math.Interpolation;
import fr.dreamin.dreamapi.api.util.math.InterpolationType;
import fr.dreamin.dreamapi.core.time.TickTask;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * ParticleChainMorph - Animates the progressive transformation (morphing) through a chain of shapes (A -> B -> C...).
 * <p>
 * This class handles the animation of a shape morphing sequentially through a list of shapes,
 * while moving along a ParticlePath, with support for looping, reversing, and progressive drawing.
 */
@Getter
@Accessors(fluent = true)
public final class ParticleChainMorph extends TickTask<ParticleChainMorph> {

  private final Particle particle;
  private final List<ParticleShape> shapes;
  private final ParticlePath path;
  private final int duration;
  private final double speed;
  private final boolean loop;
  private final boolean reverse;
  private final boolean progressiveDraw;
  private boolean turn;
  private Vector rotationAnglePerTick;
  private Vector rotationOrigin;
  private Vector currentRotationAngle;
  private final InterpolationType interpolationType;
  private final ParticleShape.ParticleOptions options;
  private final List<Integer> morphDurations;

  private final List<List<Vector>> morphFrames;

  private List<Location> pathFrames;
  private int index = 0;
  private boolean isReversed = false;

  private ParticleChainMorph(Builder builder) {
    super(builder);
    this.particle = builder.particle;
    this.shapes = builder.shapes;
    this.path = builder.path;
    this.duration = builder.duration;
    this.speed = builder.speed;
    this.loop = builder.loop;
    this.reverse = builder.reverse;
    this.progressiveDraw = builder.progressiveDraw;
    this.interpolationType = builder.interpolationType;
    this.options = builder.options;
    this.morphDurations = builder.morphDurations;

    this.turn = builder.turn;
    this.rotationAnglePerTick = builder.rotationAnglePerTick;
    this.rotationOrigin = builder.rotationOrigin;
    this.currentRotationAngle = new Vector(0, 0, 0);

    this.morphFrames = generateMorphFrames();
    // Le path est déjà configuré avec son propre easing.
    this.pathFrames = path.generateFrames(duration);
    // Si pas de path, on s'assure d'avoir au moins une frame à la location par défaut
    if (path.equals(ParticlePath.none()) && this.pathFrames.isEmpty()) {
      this.pathFrames = List.of(new Location(null, 0, 0, 0));
    }
  }

  // ###############################################################
  // -------------------- FRAME GENERATION -------------------------
  // ###############################################################

  private List<List<Vector>> generateMorphFrames() {
    if (shapes.size() < 2) {
      final var singleFrame = shapes.isEmpty() ? Collections.<Vector>emptyList() : shapes.get(0).sample();
      final var frames = new ArrayList<List<Vector>>(duration + 1);
      for (int i = 0; i <= duration; i++) frames.add(singleFrame);
      return frames;
    }

    final var allFrames = new ArrayList<List<Vector>>();

    // Normalisation des points
    int maxPoints = shapes.stream().mapToInt(s -> s.sample().size()).max().orElse(1);
    final var normalizedShapes = new ArrayList<List<Vector>>(shapes.size());
    for (final var shape : shapes)
      normalizedShapes.add(normalizePointCount(shape.sample(), maxPoints));

    // Pour chaque morph entre deux formes successives
    for (int i = 0; i < shapes.size() - 1; i++) {
      final var fromPoints = normalizedShapes.get(i);
      final var toPoints = normalizedShapes.get(i + 1);

      // On récupère la durée de cette transition
      var steps = (i < morphDurations.size()) ? morphDurations.get(i) : duration / (shapes.size() - 1);
      steps = Math.max(1, steps);

      for (int step = 0; step < steps; step++) {
        final var t = Interpolation.applyEasing((double) step / steps, interpolationType);
        final var frame = new ArrayList<Vector>(maxPoints);
        for (int j = 0; j < maxPoints; j++)
          frame.add(Interpolation.lerp(fromPoints.get(j), toPoints.get(j), t));
        allFrames.add(frame);
      }
    }

    allFrames.add(normalizedShapes.get(shapes.size() - 1));
    return allFrames;
  }

  private List<Vector> normalizePointCount(List<Vector> points, int targetCount) {
    if (points.isEmpty()) {
      var normalized = new ArrayList<Vector>(targetCount);
      for (int i = 0; i < targetCount; i++)
        normalized.add(new Vector(0, 0, 0));
      return normalized;
    }

    if (points.size() == targetCount)
      return new ArrayList<>(points);

    final var normalized = new ArrayList<Vector>(targetCount);

    if (points.size() < targetCount) {
      for (int i = 0; i < targetCount; i++) {
        var sourceIndex = (i * points.size()) / targetCount;
        normalized.add(points.get(sourceIndex).clone());
      }
    } else {
      for (int i = 0; i < targetCount; i++) {
        final var sourceIndex = (i * points.size()) / targetCount;
        normalized.add(points.get(sourceIndex).clone());
      }
    }

    return normalized;
  }

  // ###############################################################
  // -------------------------- SETTERS ----------------------------
  // ###############################################################

  public ParticleChainMorph turn(boolean turn) {
    this.turn = turn;
    return this;
  }

  public ParticleChainMorph rotationAnglePerTick(Vector rotationAnglePerTick) {
    this.rotationAnglePerTick = rotationAnglePerTick;
    return this;
  }

  public ParticleChainMorph rotationOrigin(Vector rotationOrigin) {
    this.rotationOrigin = rotationOrigin;
    return this;
  }

  public ParticleChainMorph currentRotationAngle(Vector currentRotationAngle) {
    this.currentRotationAngle = currentRotationAngle;
    return this;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void onStart() {
    this.index = 0;
    this.isReversed = false;
    this.currentRotationAngle = new Vector(0, 0, 0); // Réinitialiser l'angle de rotation au début
    if (morphFrames.isEmpty() || pathFrames.isEmpty())
      stop();
  }

  @Override
  public void onTick() {
    if (index >= morphFrames.size() || index >= pathFrames.size()) {
      if (this.loop) {
        index = 0;
        if (reverse) swap();
      } else {
        stop();
        return;
      }
    }

    if (index < morphFrames.size() && index < pathFrames.size()) {
      var currentFrame = morphFrames.get(index);
      final var baseLocation = pathFrames.get(index);

      // Logique de rotation
      if (turn) {
        currentRotationAngle.add(rotationAnglePerTick);
        currentFrame = rotateFrame(currentFrame, currentRotationAngle, rotationOrigin);
      }

      var pointsToDraw = currentFrame.size();
      if (progressiveDraw) {

        var currentCycleIndex = index;
        if (reverse && isReversed)
          currentCycleIndex = morphFrames.size() - 1 - index;

        final var progress = (double) currentCycleIndex / (morphFrames.size() - 1);
        pointsToDraw = (int) Math.ceil((double) currentFrame.size() * progress);

        if (currentCycleIndex == morphFrames.size() - 1) {
          pointsToDraw = currentFrame.size();
        }
      }

      for (int i = 0; i < pointsToDraw; i++) {
        final var offset = currentFrame.get(i);
        final var particleLocation = baseLocation.clone().add(offset);

        if (particle == Particle.DUST && options.color() != null) {
          Particle.DustOptions dustOptions = new Particle.DustOptions(
            Color.fromRGB(
              options.color().getRed(),
              options.color().getGreen(),
              options.color().getBlue()
            ),
            options.size()
          );
          baseLocation.getWorld().spawnParticle(
            particle,
            particleLocation,
            options.count(),
            options.offset().getX(),
            options.offset().getY(),
            options.offset().getZ(),
            0,
            dustOptions
          );
        } else
          baseLocation.getWorld().spawnParticle(
            particle,
            particleLocation,
            options.count(),
            options.offset().getX(),
            options.offset().getY(),
            options.offset().getZ(),
            0
          );
      }

      index++;
    }
  }

  /**
   * Starts the chain morphing animation.
   * <p>
   * The animation will be visible to all players by default.
   */
  public void play() {
    this.start();
  }

  /**
   * Starts the chain morphing animation for a specific player.
   * @param player The player who will see the particles.
   */
  public void play(@NotNull Player player) {
    shapes.forEach(shape -> shape.viewer(player));
    this.start();
  }

  /**
   * Starts the chain morphing animation for a collection of players.
   * @param players The collection of players who will see the particles.
   */
  public void play(@NotNull Collection<Player> players) {
    shapes.forEach(shape -> players.forEach(shape::viewer));
    this.start();
  }

  /**
   * Reverses the order of the morph and path frames for the reverse effect.
   */
  private void swap() {
    if (morphFrames == null || morphFrames.isEmpty()) return;
    if (pathFrames == null || pathFrames.isEmpty()) return;

    Collections.reverse(morphFrames);
    Collections.reverse(pathFrames);
    index = 0;
    isReversed = !isReversed;
  }

  /**
   * Rotates a list of vectors (offsets) around a given origin vector.
   * The rotation is applied on the XZ plane (around the Y axis).
   *
   * @param frame The list of vectors to rotate.
   * @param angle The angle of rotation in radians.
   * @param origin The point of origin for the rotation.
   * @return A new list of rotated vectors.
   */
  /**
   * Rotates a list of vectors (offsets) around a given origin vector using Euler angles (XYZ order).
   *
   * @param frame The list of vectors to rotate.
   * @param angles The Euler angles (X, Y, Z) in radians.
   * @param origin The point of origin for the rotation.
   * @return A new list of rotated vectors.
   */
  private List<Vector> rotateFrame(List<Vector> frame, Vector angles, Vector origin) {
    if (angles.getX() == 0.0 && angles.getY() == 0.0 && angles.getZ() == 0.0) return frame;

    final var angleX = angles.getX();
    final var angleY = angles.getY();
    final var angleZ = angles.getZ();

    final var cosX = Math.cos(angleX);
    final var sinX = Math.sin(angleX);
    final var cosY = Math.cos(angleY);
    final var sinY = Math.sin(angleY);
    final var cosZ = Math.cos(angleZ);
    final var sinZ = Math.sin(angleZ);

    final var rotatedFrame = new ArrayList<Vector>(frame.size());

    for (final var offset : frame) {
      // 1. Translate point to origin
      final var x = offset.getX() - origin.getX();
      final var y = offset.getY() - origin.getY();
      final var z = offset.getZ() - origin.getZ();

      // 2. Apply rotation (XYZ Euler order)
      // Rotation around X
      final var y1 = y * cosX - z * sinX;
      final var z1 = y * sinX + z * cosX;

      // Rotation around Y
      final var x2 = x * cosY + z1 * sinY;
      final var z2 = z1 * cosY - x * sinY;

      // Rotation around Z
      final var x3 = x2 * cosZ - y1 * sinZ;
      final var y3 = x2 * sinZ + y1 * cosZ;

      // 3. Translate point back
      Vector rotated = new Vector(
        x3 + origin.getX(),
        y3 + origin.getY(),
        z2 + origin.getZ()
      );
      rotatedFrame.add(rotated);
    }

    return rotatedFrame;
  }

  // ###############################################################
  // --------------------------- BUILDER ---------------------------
  // ###############################################################

  public static Builder create() {
    return new Builder();
  }



  @Getter
  @Accessors(fluent = true)
  public static class Builder extends TickTask.Builder<ParticleChainMorph, Builder> {
    private Particle particle = Particle.FLAME;
    private final List<ParticleShape> shapes = new ArrayList<>();
    private boolean turn = false;
    private Vector rotationAnglePerTick = new Vector(0, 0, 0);
    private Vector rotationOrigin = new Vector(0, 0, 0);
    private final List<Integer> morphDurations = new ArrayList<>();
    private ParticlePath path = ParticlePath.none();
    private ParticleShape.ParticleOptions options = ParticleShape.ParticleOptions.defaultOptions();
    private int duration = 40;
    private double speed = 1;
    private boolean loop = false;
    private boolean reverse = false;
    private boolean progressiveDraw = false;
    private InterpolationType interpolationType = InterpolationType.LINEAR;

    public Builder particle(Particle particle) {
      this.particle = particle;
      return this;
    }

    public Builder addShape(ParticleShape shape) {
      this.shapes.add(shape);
      return this;
    }

    /** Adds a shape with a custom transition duration (in ticks) before the next shape. */
    public Builder addShape(ParticleShape shape, int transitionTicks) {
      this.shapes.add(shape);
      // Chaque appel définit la durée de morph entre cette shape et la suivante
      if (this.shapes.size() > 1) {
        this.morphDurations.add(transitionTicks);
      }
      return this;
    }


    public Builder shapes(Collection<ParticleShape> shapes) {
      this.shapes.addAll(shapes);
      return this;
    }

    public Builder path(ParticlePath path) {
      this.path = path;
      return this;
    }

    public Builder options(ParticleShape.ParticleOptions options) {
      this.options = options;
      return this;
    }

    public Builder duration(int ticks) {
      this.duration = ticks;
      return this;
    }

    public Builder speed(double speed) {
      this.speed = speed;
      return this;
    }

    public Builder loop(boolean loop) {
      this.loop = loop;
      return this;
    }

    public Builder reverse(boolean reverse) {
      this.reverse = reverse;
      return this;
    }

    public Builder progressiveDraw(boolean progressiveDraw) {
      this.progressiveDraw = progressiveDraw;
      return this;
    }

    public Builder turn(boolean turn) {
      this.turn = turn;
      return this;
    }

    public Builder rotationAnglePerTick(Vector rotationAnglePerTick) {
      this.rotationAnglePerTick = rotationAnglePerTick;
      return this;
    }

    public Builder rotationOrigin(Vector rotationOrigin) {
      this.rotationOrigin = rotationOrigin;
      return this;
    }

    public Builder interpolation(InterpolationType type) {
      this.interpolationType = type;
      return this;
    }

    @Override
    public ParticleChainMorph build() {
      var tickInterval = (long) (1 / this.speed);
      if (tickInterval < 1) tickInterval = 1;
      this.every(tickInterval);

      if (!this.loop) {
        this.limit(this.duration);
        this.autoStop(true);
      }

      return new ParticleChainMorph(this);
    }
  }
}