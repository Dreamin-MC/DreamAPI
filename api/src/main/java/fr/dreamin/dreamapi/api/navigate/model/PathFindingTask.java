package fr.dreamin.dreamapi.api.navigate.model;

import fr.dreamin.dreamapi.api.DreamAPI;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
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
public final class PathFindingTask extends BukkitRunnable {

  private static final Particle.DustOptions DEFAULT_DUST = new Particle.DustOptions(Color.fromRGB(220, 20, 60), 1);

  private final Player player;
  private final Location targetLocation;
  private final AStartPathFinder pathFinder;
  private final double recalcMinDistance;

  /** Non-null in display mode: the particle dust to render along the path. */
  @Nullable private final Particle.DustOptions dustOptions;

  /** Non-null in callback mode: called on the main thread after each successful recalculation. */
  @Nullable private final Consumer<List<Location>> onRecalc;

  /** The latest computed path. May be null before the first computation completes. */
  private List<Location> currentPath;

  /**
   * Index of the closest waypoint to the player in {@link #currentPath}.
   * Particles and movement are rendered from this index onward (remaining path only).
   */
  private int currentPathIndex = 0;

  private Location lastCalcLocation;
  private boolean recalculating = false;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  /**
   * Display mode — particles are rendered automatically each tick.
   * Pass {@code null} for {@code dustOptions} to use the default red dust.
   */
  public PathFindingTask(final @NotNull Player player, final @NotNull Location targetLocation,
                         final boolean safeMode, final @NotNull Set<Material> allowedMaterials,
                         final double recalcMinDistance, final @Nullable Particle.DustOptions dustOptions) {
    this.player = player;
    this.targetLocation = targetLocation;
    this.pathFinder = new AStartPathFinder(safeMode, allowedMaterials);
    this.lastCalcLocation = player.getLocation().clone();
    this.recalcMinDistance = recalcMinDistance;
    this.dustOptions = dustOptions != null ? dustOptions : DEFAULT_DUST;
    this.onRecalc = null;
  }

  /**
   * Callback mode — no automatic particle display.
   * {@code onRecalc} is fired on the main thread with the new path after each recalculation.
   */
  public PathFindingTask(final @NotNull Player player, final @NotNull Location targetLocation,
                         final boolean safeMode, final @NotNull Set<Material> allowedMaterials,
                         final double recalcMinDistance, final @NotNull Consumer<List<Location>> onRecalc) {
    this.player = player;
    this.targetLocation = targetLocation;
    this.pathFinder = new AStartPathFinder(safeMode, allowedMaterials);
    this.lastCalcLocation = player.getLocation().clone();
    this.recalcMinDistance = recalcMinDistance;
    this.dustOptions = null;
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
      cancel();
      return;
    }

    final var playerLoc = this.player.getLocation();

    // Keep the current path index up to date with the player's position
    updateCurrentIndex(playerLoc);

    // Always render the remaining path — never blank during recalculation
    if (this.dustOptions != null)
      displayPathParticles();

    // Trigger a recalculation only when the player has moved far enough
    final var distMoved = playerLoc.distanceSquared(this.lastCalcLocation);
    if (distMoved < (this.recalcMinDistance * this.recalcMinDistance))
      return;

    this.lastCalcLocation = playerLoc.clone();

    if (this.recalculating) return;
    this.recalculating = true;

    // Capture block locations on the main thread BEFORE going async (Bukkit API is not thread-safe)
    final var playerBlockLoc = playerLoc.getBlock().getLocation();
    final var targetBlockLoc = this.targetLocation.getBlock().getLocation();

    Bukkit.getScheduler().runTaskAsynchronously(DreamAPI.getAPI().plugin(), () -> {
      final var newPath = this.pathFinder.findPath(playerBlockLoc, targetBlockLoc);
      // Bring result back to main thread before touching shared state
      Bukkit.getScheduler().runTask(DreamAPI.getAPI().plugin(), () -> {
        if (newPath != null && !newPath.isEmpty()) {
          this.currentPath = newPath;
          this.currentPathIndex = 0;
          if (this.onRecalc != null)
            this.onRecalc.accept(List.copyOf(newPath));
        }
        this.recalculating = false;
      });
    });
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  /**
   * Updates {@link #currentPathIndex} to the index of the waypoint
   * closest to {@code playerLoc}, scanning forward from the current index only
   * to prevent backtracking.
   */
  private void updateCurrentIndex(final @NotNull Location playerLoc) {
    if (this.currentPath == null || this.currentPath.isEmpty()) return;

    double minDist = Double.MAX_VALUE;
    int bestIndex = this.currentPathIndex;

    for (int i = this.currentPathIndex; i < this.currentPath.size(); i++) {
      final var dist = playerLoc.distanceSquared(this.currentPath.get(i));
      if (dist < minDist) {
        minDist = dist;
        bestIndex = i;
      } else if (dist > minDist + 9) {
        // Stop scanning once distance starts growing significantly (3 blocks ahead)
        break;
      }
    }

    this.currentPathIndex = bestIndex;
  }

  /** Renders only the remaining path ahead of the player (from {@link #currentPathIndex}). */
  private void displayPathParticles() {
    if (this.currentPath == null || this.currentPath.isEmpty()) return;

    for (int i = this.currentPathIndex; i < this.currentPath.size(); i++) {
      this.player.spawnParticle(Particle.DUST, this.currentPath.get(i).clone().add(0.5, 0.5, 0.5),
        3, 0, 0, 0, 0, this.dustOptions);
    }
  }

}
