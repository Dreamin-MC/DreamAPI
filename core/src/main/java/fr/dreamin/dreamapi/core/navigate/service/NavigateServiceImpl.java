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

@DreamAutoService(NavigateService.class)
public final class NavigateServiceImpl implements DreamService, NavigateService {

  private static final Particle.DustOptions DEFAULT_DUST = new Particle.DustOptions(Color.fromRGB(220, 20, 60), 1);

  private final Map<UUID, PathFindingTask> playerNavigations = new HashMap<>();
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
  public void findPathAsync(final @NotNull Location start, final @NotNull Location end,
                            final boolean safeMode, final @NotNull Consumer<List<Location>> callback) {
    findPathAsync(start, end, safeMode, Set.of(), callback);
  }

  @Override
  public void findPathAsync(final @NotNull Location start, final @NotNull Location end,
                            final boolean safeMode, final @NotNull Set<Material> allowedMaterials,
                            final @NotNull Consumer<List<Location>> callback) {
    // Capture block locations on main thread before going async (Bukkit API is not thread-safe)
    final var startBlock = start.getBlock().getLocation();
    final var endBlock = end.getBlock().getLocation();
    final var finder = new AStartPathFinder(safeMode, allowedMaterials);

    Bukkit.getScheduler().runTaskAsynchronously(DreamAPI.getAPI().plugin(), () -> {
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
  public void startNavigation(final @NotNull Player player, final @NotNull Location end,
                              final boolean safeMode, final double recalcDistance) {
    startNavigation(player, end, safeMode, recalcDistance, Set.of(), DEFAULT_DUST);
  }

  @Override
  public void startNavigation(final @NotNull Player player, final @NotNull Location end,
                              final boolean safeMode, final double recalcDistance,
                              final @NotNull Set<Material> allowedMaterials) {
    startNavigation(player, end, safeMode, recalcDistance, allowedMaterials, DEFAULT_DUST);
  }

  @Override
  public void startNavigation(final @NotNull Player player, final @NotNull Location end,
                              final boolean safeMode, final double recalcDistance,
                              final @NotNull Particle.DustOptions dustOptions) {
    startNavigation(player, end, safeMode, recalcDistance, Set.of(), dustOptions);
  }

  @Override
  public void startNavigation(final @NotNull Player player, final @NotNull Location end,
                              final boolean safeMode, final double recalcDistance,
                              final @NotNull Set<Material> allowedMaterials,
                              final @NotNull Particle.DustOptions dustOptions) {
    stopNavigation(player);
    final var task = new PathFindingTask(player, end, safeMode, allowedMaterials, recalcDistance, dustOptions);
    task.runTaskTimer(DreamAPI.getAPI().plugin(), 0L, 10L);
    this.playerNavigations.put(player.getUniqueId(), task);
  }

  @Override
  public void startNavigation(final @NotNull Player player, final @NotNull Location end,
                              final boolean safeMode, final double recalcDistance,
                              final @NotNull Consumer<List<Location>> onRecalc) {
    startNavigation(player, end, safeMode, recalcDistance, Set.of(), onRecalc);
  }

  @Override
  public void startNavigation(final @NotNull Player player, final @NotNull Location end,
                              final boolean safeMode, final double recalcDistance,
                              final @NotNull Set<Material> allowedMaterials,
                              final @NotNull Consumer<List<Location>> onRecalc) {
    stopNavigation(player);
    final var task = new PathFindingTask(player, end, safeMode, allowedMaterials, recalcDistance, onRecalc);
    task.runTaskTimer(DreamAPI.getAPI().plugin(), 0L, 10L);
    this.playerNavigations.put(player.getUniqueId(), task);
  }

  @Override
  public void stopNavigation(final @NotNull Player player) {
    final var task = this.playerNavigations.remove(player.getUniqueId());
    if (task != null && !task.isCancelled()) task.cancel();
  }

  @Override
  public void stopAllNavigations() {
    new ArrayList<>(this.playerNavigations.values()).forEach(task -> {
      if (!task.isCancelled()) task.cancel();
    });
    this.playerNavigations.clear();
  }

  @Override
  public boolean isNavigating(final @NotNull Player player) {
    return this.playerNavigations.containsKey(player.getUniqueId());
  }

  @Override
  public @NotNull Optional<PathFindingTask> getNavigationTask(final @NotNull Player player) {
    return Optional.ofNullable(this.playerNavigations.get(player.getUniqueId()));
  }

  // ###############################################################
  // -------------------- ENTITY MOVEMENT --------------------------
  // ###############################################################

  @Override
  public void moveEntityTo(final @NotNull Entity entity, final @NotNull Location end,
                           final boolean safeMode, final double speed) {
    moveEntityTo(entity, end, safeMode, speed, Set.of());
  }

  @Override
  public void moveEntityTo(final @NotNull Entity entity, final @NotNull Location end,
                           final boolean safeMode, final double speed,
                           final @NotNull Set<Material> allowedMaterials) {
    stopEntityMovement(entity);
    final var task = new EntityMovementTask(entity, end, safeMode, allowedMaterials, speed);
    task.runTaskTimer(DreamAPI.getAPI().plugin(), 0L, 2L);
    this.entityMovements.put(entity.getUniqueId(), task);
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
