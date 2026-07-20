package fr.dreamin.dreamapi.core.navigate.service;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.navigate.model.AStartPathFinder;
import fr.dreamin.dreamapi.api.navigate.model.EntityMovementTask;
import fr.dreamin.dreamapi.api.navigate.model.PathFindingTask;
import fr.dreamin.dreamapi.api.navigate.service.NavigateService;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import org.bukkit.scheduler.BukkitTask;

@DreamAutoService(NavigateService.class)
public final class NavigateServiceImpl implements DreamService, NavigateService {

  private static final Particle.DustOptions DEFAULT_DUST = new Particle.DustOptions(Color.fromRGB(220, 20, 60), 1);

  private final Map<UUID, Set<PathFindingTask>> playerNavigations = new HashMap<>();
  private final Map<UUID, EntityMovementTask> entityMovements = new HashMap<>();

  // ###############################################################
  // ----------------------- LIFECYCLE METHODS ---------------------
  // ###############################################################

  @Override
  public void onClose() {
    stopAllNavigations();
    new ArrayList<>(this.entityMovements.values()).forEach(task -> {
      if (!task.isCancelled()) task.cancel();
    });
    this.entityMovements.clear();
  }

  // ###############################################################
  // ----------------------- PATH FINDING --------------------------
  // ###############################################################

  @Override
  public @NotNull BukkitTask findPathAsync(final @NotNull Location start, final @NotNull Location end,
                                           final boolean safeMode, final @NotNull Consumer<List<Location>> callback) {
    return findPathAsync(start, end, safeMode, Set.of(), callback);
  }

  @Override
  public @NotNull BukkitTask findPathAsync(final @NotNull Location start, final @NotNull Location end,
                                           final boolean safeMode, final @NotNull Set<Material> allowedMaterials,
                                           final @NotNull Consumer<List<Location>> callback) {
    // Capture block locations on main thread before going async (Bukkit API is not thread-safe)
    final var startBlock = start.getBlock().getLocation();
    final var endBlock = end.getBlock().getLocation();
    final var finder = new AStartPathFinder(safeMode, allowedMaterials);

    return Bukkit.getScheduler().runTaskAsynchronously(DreamAPI.getAPI().plugin(), () -> {
      final var path = finder.findPath(startBlock, endBlock);
      // Deliver result on the main thread
      Bukkit.getScheduler().runTask(DreamAPI.getAPI().plugin(), () -> callback.accept(path));
    });
  }

  // ###############################################################
  // ----------------------- DISPLAY PATH --------------------------
  // ###############################################################

  @Override
  public void displayPath(final @NotNull Player player, final @NotNull List<Location> path,
                          final @NotNull Particle particle, final @Nullable Object particleData) {
    for (final var loc : path) {
      player.spawnParticle(particle, loc.clone().add(0.5, 0.5, 0.5), 3, 0, 0, 0, 0, particleData);
    }
  }

  @Override
  public void displayPath(final @NotNull Player player, final @NotNull List<Location> path,
                          final @NotNull Particle.DustOptions dustOptions) {
    displayPath(player, path, Particle.DUST, dustOptions);
  }

  // ###############################################################
  // -------------------- ACTIVE NAVIGATION ------------------------
  // ###############################################################

  @Override
  public @NotNull PathFindingTask startNavigation(final @NotNull Player player, final @NotNull Location end,
                                                  final boolean safeMode, final double recalcDistance) {
    return startNavigation(player, end, safeMode, recalcDistance, Set.of(), DEFAULT_DUST);
  }

  @Override
  public @NotNull PathFindingTask startNavigation(final @NotNull Player player, final @NotNull Location end,
                                                  final boolean safeMode, final double recalcDistance,
                                                  final @NotNull Set<Material> allowedMaterials) {
    return startNavigation(player, end, safeMode, recalcDistance, allowedMaterials, DEFAULT_DUST);
  }

  @Override
  public @NotNull PathFindingTask startNavigation(final @NotNull Player player, final @NotNull Location end,
                                                  final boolean safeMode, final double recalcDistance,
                                                  final @NotNull Particle.DustOptions dustOptions) {
    return startNavigation(player, end, safeMode, recalcDistance, Set.of(), dustOptions);
  }

  @Override
  public @NotNull PathFindingTask startNavigation(final @NotNull Player player, final @NotNull Location end,
                                                  final boolean safeMode, final double recalcDistance,
                                                  final @NotNull Set<Material> allowedMaterials,
                                                  final @NotNull Particle.DustOptions dustOptions) {
    final var task = new PathFindingTask(player, end, safeMode, allowedMaterials, recalcDistance, dustOptions);
    task.runTaskTimer(DreamAPI.getAPI().plugin(), 0L, 10L);
    this.playerNavigations.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(task);
    return task;
  }

  @Override
  public @NotNull PathFindingTask startNavigation(final @NotNull Player player, final @NotNull Location end,
                                                  final boolean safeMode, final double recalcDistance,
                                                  final @NotNull Consumer<List<Location>> onRecalc) {
    return startNavigation(player, end, safeMode, recalcDistance, Set.of(), onRecalc);
  }

  @Override
  public @NotNull PathFindingTask startNavigation(final @NotNull Player player, final @NotNull Location end,
                                                  final boolean safeMode, final double recalcDistance,
                                                  final @NotNull Set<Material> allowedMaterials,
                                                  final @NotNull Consumer<List<Location>> onRecalc) {
    final var task = new PathFindingTask(player, end, safeMode, allowedMaterials, recalcDistance, onRecalc);
    task.runTaskTimer(DreamAPI.getAPI().plugin(), 0L, 10L);
    this.playerNavigations.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(task);
    return task;
  }

  @Override
  public void stopNavigation(final @NotNull Player player) {
    final var tasks = this.playerNavigations.remove(player.getUniqueId());
    if (tasks != null) {
      tasks.forEach(task -> {
        if (!task.isCancelled()) task.cancel();
      });
    }
  }

  @Override
  public void stopAllNavigations() {
    this.playerNavigations.values().forEach(tasks -> tasks.forEach(task -> {
      if (!task.isCancelled()) task.cancel();
    }));
    this.playerNavigations.clear();
  }

  @Override
  public boolean isNavigating(final @NotNull Player player) {
    final var tasks = this.playerNavigations.get(player.getUniqueId());
    if (tasks == null) return false;
    tasks.removeIf(PathFindingTask::isCancelled);
    if (tasks.isEmpty()) {
      this.playerNavigations.remove(player.getUniqueId());
      return false;
    }
    return true;
  }

  @Override
  public @NotNull Set<PathFindingTask> getNavigationTasks(final @NotNull Player player) {
    final var tasks = this.playerNavigations.getOrDefault(player.getUniqueId(), new HashSet<>());
    tasks.removeIf(PathFindingTask::isCancelled);
    if (tasks.isEmpty()) {
      this.playerNavigations.remove(player.getUniqueId());
    }
    return Set.copyOf(tasks);
  }

  // ###############################################################
  // -------------------- ENTITY MOVEMENT --------------------------
  // ###############################################################

  @Override
  public @NotNull EntityMovementTask moveEntityTo(final @NotNull Entity entity, final @NotNull Location end,
                                                  final boolean safeMode, final double speed) {
    return moveEntityTo(entity, end, safeMode, speed, Set.of());
  }

  @Override
  public @NotNull EntityMovementTask moveEntityTo(final @NotNull Entity entity, final @NotNull Location end,
                                                  final boolean safeMode, final double speed,
                                                  final @NotNull Set<Material> allowedMaterials) {
    stopEntityMovement(entity);
    final var task = new EntityMovementTask(entity, end, safeMode, allowedMaterials, speed);
    task.runTaskTimer(DreamAPI.getAPI().plugin(), 0L, 2L);
    this.entityMovements.put(entity.getUniqueId(), task);
    return task;
  }

  @Override
  public void stopEntityMovement(final @NotNull Entity entity) {
    final var task = this.entityMovements.remove(entity.getUniqueId());
    if (task != null && !task.isCancelled()) task.cancel();
  }

  @Override
  public boolean isEntityMoving(final @NotNull Entity entity) {
    return this.entityMovements.containsKey(entity.getUniqueId());
  }

  @Override
  public @NotNull Optional<EntityMovementTask> getEntityMovementTask(final @NotNull Entity entity) {
    return Optional.ofNullable(this.entityMovements.get(entity.getUniqueId()));
  }

}
