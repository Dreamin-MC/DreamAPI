package fr.dreamin.dreamapi.api.navigate.model;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.navigate.event.player.PathFindingStopEvent;
import fr.dreamin.dreamapi.api.navigate.event.player.PathFindingFinishEvent;
import fr.dreamin.dreamapi.api.navigate.event.player.PathFindingRecalcEvent;
import fr.dreamin.dreamapi.api.navigate.event.player.PathFindingWaypointReachEvent;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * A recurring Bukkit task that handles active player navigation.
 * Recalculates the A* path when the player moves beyond {@code recalcMinDistance} blocks
 * from the last calculation point, and either displays particles or fires a callback.
 *
 * <p>This task is managed by {@code NavigateService} — do not start/cancel it manually.</p>
 */
@Getter
public final class PathFindingTask extends AbstractNavigateTask {

  private static final Particle.DustOptions DEFAULT_DUST = new Particle.DustOptions(Color.fromRGB(220, 20, 60), 1);

  private final Player player;
  private final double recalcMinDistance;
  private final double displayRadius;

  /** Non-null in display mode: the particle dust to render along the path. */
  @Nullable private final Particle.DustOptions dustOptions;

  @Nullable private final Particle particle;

  /** Non-null in callback mode: called on the main thread after each successful recalculation. */
  @Nullable private final Consumer<List<Location>> onRecalc;

  private Location lastCalcLocation;

  // ###############################################################
  // -------------------------- CANCEL -----------------------------
  // ###############################################################

  @Override
  public void cancel() {
    super.cancel();
    if (this.finished)
      new PathFindingFinishEvent(this).callEvent();
    else
      new PathFindingStopEvent(this).callEvent();
  }

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  /**
   * Display mode — particles are rendered automatically each tick.
   * Pass {@code null} for {@code dustOptions} to use the default red dust.
   */
  public PathFindingTask(final @NotNull Player player, final @NotNull Location targetLocation,
                         final boolean safeMode, final @NotNull Set<Material> allowedMaterials,
                         final @NotNull Set<Material> ignoredMaterials, final double recalcMinDistance, final double displayRadius) {
    super(targetLocation, new AStartPathFinder(safeMode, allowedMaterials, ignoredMaterials));
    this.player = player;
    this.lastCalcLocation = player.getLocation().clone();
    this.recalcMinDistance = recalcMinDistance;
    this.displayRadius = displayRadius;
    this.dustOptions = DEFAULT_DUST;
    this.particle = null;
    this.onRecalc = null;
  }

  /**
   * Display mode — particles are rendered automatically each tick.
   * Pass {@code null} for {@code dustOptions} to use the default red dust.
   */
  public PathFindingTask(final @NotNull Player player, final @NotNull Location targetLocation,
                         final boolean safeMode, final @NotNull Set<Material> allowedMaterials,
                         final @NotNull Set<Material> ignoredMaterials,
                         final double recalcMinDistance, final double displayRadius, final @NotNull Particle.DustOptions dustOptions) {
    super(targetLocation, new AStartPathFinder(safeMode, allowedMaterials, ignoredMaterials));
    this.player = player;
    this.lastCalcLocation = player.getLocation().clone();
    this.recalcMinDistance = recalcMinDistance;
    this.displayRadius = displayRadius;
    this.dustOptions = dustOptions;
    this.particle = null;
    this.onRecalc = null;
  }

  public PathFindingTask(final @NotNull Player player, final @NotNull Location targetLocation,
                         final boolean safeMod, final @NotNull Set<Material> allowedMaterials,
                         final @NotNull Set<Material> ignoredMaterials,
                         final double recalcMinDistance, final double displayRadius, final @NotNull Particle particle) {
    super(targetLocation, new AStartPathFinder(safeMod, allowedMaterials, ignoredMaterials));
    this.player = player;
    this.lastCalcLocation = player.getLocation().clone();
    this.recalcMinDistance = recalcMinDistance;
    this.displayRadius = displayRadius;
    this.dustOptions = null;
    this.particle = particle;
    this.onRecalc = null;
  }

  /**
   * Callback mode — no automatic particle display.
   * {@code onRecalc} is fired on the main thread with the new path after each recalculation.
   */
  public PathFindingTask(final @NotNull Player player, final @NotNull Location targetLocation,
                         final boolean safeMode, final @NotNull Set<Material> allowedMaterials,
                         final @NotNull Set<Material> ignoredMaterials,
                         final double recalcMinDistance, final double displayRadius, final @NotNull Consumer<List<Location>> onRecalc) {
    super(targetLocation, new AStartPathFinder(safeMode, allowedMaterials, ignoredMaterials));
    this.player = player;
    this.lastCalcLocation = player.getLocation().clone();
    this.recalcMinDistance = recalcMinDistance;
    this.displayRadius = displayRadius;
    this.dustOptions = null;
    this.particle = null;
    this.onRecalc = onRecalc;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void run() {
    if (!this.player.isOnline()) {
      cancel();
      return;
    }

    if (this.player.getLocation().distanceSquared(this.targetLocation) < 4) {
      this.finished = true;
      cancel();
      return;
    }

    final var playerLoc = this.player.getLocation();

    // Keep the current path index up to date with the player's position (bidirectional)
    final double closestDistSq = updateCurrentIndex(playerLoc);

    // Always render the remaining path — never blank during recalculation
    if (this.dustOptions != null || this.particle != null)
      displayPathParticles();

    // Trigger recalculation ONLY if:
    // 1) Path is not yet computed (initial calculation)
    // 2) Player has deviated from the path (> recalcMinDistance)
    final double maxDeviationSq = this.recalcMinDistance * this.recalcMinDistance;
    final boolean needsRecalc = (this.currentPath == null || this.currentPath.isEmpty() || closestDistSq > maxDeviationSq);

    if (needsRecalc) {
      triggerRecalc();
    }
  }

  /**
   * Triggers an asynchronous path recalculation immediately.
   * Can be called on startup or when deviation is detected.
   */
  public void triggerRecalc() {
    if (this.recalculating || !this.player.isOnline()) return;
    this.recalculating = true;

    // Capture block locations on the main thread BEFORE going async (Bukkit API is not thread-safe)
    final var playerBlockLoc = this.player.getLocation().getBlock().getLocation();
    final var targetBlockLoc = this.targetLocation.getBlock().getLocation();

    Bukkit.getScheduler().runTaskAsynchronously(DreamAPI.getAPI().plugin(), () -> {
      final var newPath = this.pathFinder.findPath(playerBlockLoc, targetBlockLoc);
      // Bring result back to main thread before touching shared state
      Bukkit.getScheduler().runTask(DreamAPI.getAPI().plugin(), () -> {
        if (!newPath.isEmpty()) {
          this.currentPath = newPath;
          this.currentPathIndex = 0;
          new PathFindingRecalcEvent(this, List.copyOf(newPath)).callEvent();
          if (this.onRecalc != null)
            this.onRecalc.accept(List.copyOf(newPath));
          if (this.dustOptions != null || this.particle != null)
            displayPathParticles();
        }
        this.recalculating = false;
      });
    });
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  /**
   * Updates {@link #currentPathIndex} to the index of the waypoint closest to {@code playerLoc},
   * scanning bidirectionally to support both advancing and backtracking.
   *
   * @return the squared distance to the closest waypoint, or Double.MAX_VALUE if no path.
   */
  private double updateCurrentIndex(final @NotNull Location playerLoc) {
    if (this.currentPath == null || this.currentPath.isEmpty()) return Double.MAX_VALUE;

    double minDist = Double.MAX_VALUE;
    int bestIndex = this.currentPathIndex;

    // 1. Search in a local window around currentPathIndex (prevents jumping across walls/hairpins)
    final int windowStart = Math.max(0, this.currentPathIndex - 6);
    final int windowEnd = Math.min(this.currentPath.size() - 1, this.currentPathIndex + 6);

    for (int i = windowStart; i <= windowEnd; i++) {
      final var dist = playerLoc.distanceSquared(this.currentPath.get(i));
      if (dist < minDist) {
        minDist = dist;
        bestIndex = i;
      }
    }

    // 2. If player is further than deviation distance in the local window, scan the full path
    final double maxDeviationSq = this.recalcMinDistance * this.recalcMinDistance;
    if (minDist > maxDeviationSq) {
      for (int i = 0; i < this.currentPath.size(); i++) {
        final var dist = playerLoc.distanceSquared(this.currentPath.get(i));
        if (dist < minDist) {
          minDist = dist;
          bestIndex = i;
        }
      }
    }

    if (bestIndex != this.currentPathIndex) {
      final int prevIndex = this.currentPathIndex;
      this.currentPathIndex = bestIndex;
      if (bestIndex > prevIndex) {
        new PathFindingWaypointReachEvent(this, this.currentPath.get(bestIndex), bestIndex).callEvent();
      }
    }

    return minDist;
  }

  /** Renders only the remaining path ahead of the player (from {@link #currentPathIndex}). */
  private void displayPathParticles() {
    if (this.currentPath == null || this.currentPath.isEmpty()) return;

    final var playerLoc = this.player.getLocation();
    final var displayRadiusSq = this.displayRadius > 0 ? this.displayRadius * this.displayRadius : Double.MAX_VALUE;

    for (int i = this.currentPathIndex; i < this.currentPath.size(); i++) {
      final var loc = this.currentPath.get(i);
      
      if (this.displayRadius > 0 && loc.distanceSquared(playerLoc) > displayRadiusSq)
        continue;

      if (this.dustOptions != null)
        this.player.spawnParticle(Particle.DUST, loc.clone().add(0.5, 0.5, 0.5),
        3, 0, 0, 0, 0, this.dustOptions);
      else if (this.particle != null)
        this.player.spawnParticle(this.particle, loc.clone().add(0.5, 0.5, 0.5),
          3, 0, 0, 0, 0);
    }
  }

}
