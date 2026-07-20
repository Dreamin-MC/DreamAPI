package fr.dreamin.dreamapi.api.navigate.service;

import fr.dreamin.dreamapi.api.navigate.model.EntityMovementTask;
import fr.dreamin.dreamapi.api.navigate.model.PathFindingTask;
import fr.dreamin.dreamapi.api.services.DreamService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

public interface NavigateService extends DreamService {

  // ###############################################################
  // ----------------------- PATH FINDING --------------------------
  // ###############################################################

  /**
   * Computes a path asynchronously between two locations.
   * The callback is always delivered on the main thread.
   * Returns an empty list if no path is found.
   *
   * @return the BukkitTask running the async calculation.
   */
  @NotNull BukkitTask findPathAsync(@NotNull Location start, @NotNull Location end, boolean safeMode,
                                    @NotNull Consumer<List<Location>> callback);

  /** Same as {@link #findPathAsync(Location, Location, boolean, Consumer)} but restricted to specific floor materials. */
  @NotNull BukkitTask findPathAsync(@NotNull Location start, @NotNull Location end, boolean safeMode,
                                    @NotNull Set<Material> allowedMaterials, @NotNull Consumer<List<Location>> callback);

  /** Same, with ignored materials that the algorithm can path through freely. */
  @NotNull BukkitTask findPathAsync(@NotNull Location start, @NotNull Location end, boolean safeMode,
                                    @NotNull Set<Material> allowedMaterials, @NotNull Set<Material> ignoredMaterials,
                                    @NotNull Consumer<List<Location>> callback);

  // ###############################################################
  // ----------------------- DISPLAY PATH --------------------------
  // ###############################################################

  /**
   * Spawns particles along the given path once (not recurring).
   * Use {@code particleData} for extra data (e.g. {@link org.bukkit.Particle.DustOptions}),
   * or {@code null} if the particle needs none.
   */
  void displayPath(@NotNull Player player, @NotNull List<Location> path,
                   @NotNull Particle particle, @Nullable Object particleData);

  /** Convenience overload for colored dust particles. */
  void displayPath(@NotNull Player player, @NotNull List<Location> path,
                   @NotNull Particle.DustOptions dustOptions);

  // ###############################################################
  // -------------------- ACTIVE NAVIGATION ------------------------
  // ###############################################################

  /** Starts a recurring navigation that displays default red dust particles. */
  @Nullable PathFindingTask startNavigation(@NotNull Player player, @NotNull Location end,
                                            boolean safeMode, double recalcDistance);

  /** Same, restricted to specific floor materials. */
  @Nullable PathFindingTask startNavigation(@NotNull Player player, @NotNull Location end,
                                            boolean safeMode, double recalcDistance,
                                            @NotNull Set<Material> allowedMaterials);

  /** Same, with ignored materials that the algorithm can path through freely. */
  @Nullable PathFindingTask startNavigation(@NotNull Player player, @NotNull Location end,
                                            boolean safeMode, double recalcDistance,
                                            @NotNull Set<Material> allowedMaterials,
                                            @NotNull Set<Material> ignoredMaterials);

  /** Starts navigation with custom dust particle color. */
  @Nullable PathFindingTask startNavigation(@NotNull Player player, @NotNull Location end,
                                            boolean safeMode, double recalcDistance,
                                            @NotNull Particle.DustOptions dustOptions);

  /** Custom dust + restricted floor materials. */
  @Nullable PathFindingTask startNavigation(@NotNull Player player, @NotNull Location end,
                                            boolean safeMode, double recalcDistance,
                                            @NotNull Set<Material> allowedMaterials,
                                            @NotNull Particle.DustOptions dustOptions);

  /** Custom dust + restricted floor materials + ignored materials. */
  @Nullable PathFindingTask startNavigation(@NotNull Player player, @NotNull Location end,
                                            boolean safeMode, double recalcDistance,
                                            @NotNull Set<Material> allowedMaterials,
                                            @NotNull Set<Material> ignoredMaterials,
                                            @NotNull Particle.DustOptions dustOptions);

  /**
   * Starts navigation in callback mode — no automatic particle display.
   * {@code onRecalc} is called on the main thread each time the path is recalculated,
   * with the new path as argument. Useful for custom waypoint rendering, titles, etc.
   */
  @Nullable PathFindingTask startNavigation(@NotNull Player player, @NotNull Location end,
                                            boolean safeMode, double recalcDistance,
                                            @NotNull Consumer<List<Location>> onRecalc);

  /** Callback mode + restricted floor materials. */
  @Nullable PathFindingTask startNavigation(@NotNull Player player, @NotNull Location end,
                                            boolean safeMode, double recalcDistance,
                                            @NotNull Set<Material> allowedMaterials,
                                            @NotNull Consumer<List<Location>> onRecalc);

  /** Callback mode + restricted floor materials + ignored materials. */
  @Nullable PathFindingTask startNavigation(@NotNull Player player, @NotNull Location end,
                                            boolean safeMode, double recalcDistance,
                                            @NotNull Set<Material> allowedMaterials,
                                            @NotNull Set<Material> ignoredMaterials,
                                            @NotNull Consumer<List<Location>> onRecalc);

  @Nullable PathFindingTask startNavigation(@NotNull Player player, @NotNull Location end, boolean safeMode,
                                            double recalcDistance, @NotNull Particle particle);

  @Nullable PathFindingTask startNavigation(@NotNull Player player, @NotNull Location end, boolean safeMode,
                                            double recalcDistance, @NotNull Set<Material> allowedMaterials, @NotNull Particle particle);

  @Nullable PathFindingTask startNavigation(@NotNull Player player, @NotNull Location end, boolean safeMode,
                                            double recalcDistance, @NotNull Set<Material> allowedMaterials, 
                                            @NotNull Set<Material> ignoredMaterials, @NotNull Particle particle);

  /** Stops all active navigations for this player. */
  void stopNavigation(@NotNull Player player);

  /** Stops a specific navigation task and removes it from the active list. */
  void stopNavigation(@NotNull PathFindingTask task);

  /** Stops all active player navigations (e.g. on plugin shutdown). */
  void stopAllNavigations();

  /** Returns {@code true} if the player has at least one active navigation task. */
  boolean isNavigating(@NotNull Player player);

  /**
   * Returns all active {@link PathFindingTask}s for this player.
   * Useful to inspect the current path, path index, destination, etc.
   */
  @NotNull Set<PathFindingTask> getNavigationTasks(@NotNull Player player);

  // ###############################################################
  // -------------------- ENTITY MOVEMENT --------------------------
  // ###############################################################

  /**
   * Moves an entity to the target location using the custom A* pathfinder.
   * The entity is pushed along the path via velocity each tick.
   * Note: disable the entity's AI before calling this for mob entities
   * to avoid conflicting navigation.
   *
   * @param speed Movement speed in blocks per tick (e.g. 0.2 for a slow walk).
   */
  @Nullable EntityMovementTask moveEntityTo(@NotNull Entity entity, @NotNull Location end,
                                            boolean safeMode, double speed);

  /** Same, restricted to specific floor materials. */
  @Nullable EntityMovementTask moveEntityTo(@NotNull Entity entity, @NotNull Location end,
                                            boolean safeMode, double speed,
                                            @NotNull Set<Material> allowedMaterials);

  /** Same, restricted to specific floor materials and ignored materials. */
  @Nullable EntityMovementTask moveEntityTo(@NotNull Entity entity, @NotNull Location end,
                                            boolean safeMode, double speed,
                                            @NotNull Set<Material> allowedMaterials,
                                            @NotNull Set<Material> ignoredMaterials);

  /** Stops the movement task for this entity, if any. */
  void stopEntityMovement(@NotNull Entity entity);

  /** Stops a specific movement task. */
  void stopEntityMovement(@NotNull EntityMovementTask task);

  /** Returns {@code true} if the entity has an active movement task. */
  boolean isEntityMoving(@NotNull Entity entity);

  /**
   * Returns the active {@link EntityMovementTask} for this entity.
   * Useful to inspect the current path, current index in the path, destination, etc.
   */
  @NotNull Optional<EntityMovementTask> getEntityMovementTask(@NotNull Entity entity);

}
