package fr.dreamin.dreamapi.core.navigate.service;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.navigate.event.player.PathFindingStartEvent;
import fr.dreamin.dreamapi.api.navigate.model.*;
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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import org.bukkit.scheduler.BukkitTask;

import org.bukkit.entity.ItemDisplay;


@DreamAutoService(NavigateService.class)
public final class NavigateServiceImpl implements DreamService, NavigateService {

  private static final Particle.DustOptions DEFAULT_DUST = new Particle.DustOptions(Color.fromRGB(220, 20, 60), 1);

  private final Map<UUID, Set<AbstractNavigateTask>> playerNavigations = new HashMap<>();
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
    return findPathAsync(start, end, safeMode, allowedMaterials, Set.of(), callback);
  }

  @Override
  public @NotNull BukkitTask findPathAsync(final @NotNull Location start, final @NotNull Location end,
                                           final boolean safeMode, final @NotNull Set<Material> allowedMaterials,
                                           final @NotNull Set<Material> ignoredMaterials,
                                           final @NotNull Consumer<List<Location>> callback) {
    // Capture block locations on main thread before going async (Bukkit API is not thread-safe)
    final var startBlock = start.getBlock().getLocation();
    final var endBlock = end.getBlock().getLocation();
    final var finder = new AStartPathFinder(safeMode, allowedMaterials, ignoredMaterials);

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
  public @Nullable PathFindingTask startNavigation(final @NotNull Player player, final @NotNull Location end,
                                                   final boolean safeMode, final double recalcDistance) {
    return startNavigation(player, end, safeMode, recalcDistance, Set.of(), DEFAULT_DUST);
  }

  @Override
  public @Nullable PathFindingTask startNavigation(final @NotNull Player player, final @NotNull Location end,
                                                   final boolean safeMode, final double recalcDistance,
                                                   final @NotNull Set<Material> allowedMaterials) {
    return startNavigation(player, end, safeMode, recalcDistance, allowedMaterials, Set.of(), DEFAULT_DUST);
  }

  @Override
  public @Nullable PathFindingTask startNavigation(final @NotNull Player player, final @NotNull Location end,
                                                   final boolean safeMode, final double recalcDistance,
                                                   final @NotNull Set<Material> allowedMaterials,
                                                   final @NotNull Set<Material> ignoredMaterials) {
    return startNavigation(player, end, safeMode, recalcDistance, allowedMaterials, ignoredMaterials, DEFAULT_DUST);
  }

  @Override
  public @Nullable PathFindingTask startNavigation(final @NotNull Player player, final @NotNull Location end,
                                                   final boolean safeMode, final double recalcDistance,
                                                   final @NotNull Particle.DustOptions dustOptions) {
    return startNavigation(player, end, safeMode, recalcDistance, Set.of(), Set.of(), dustOptions);
  }

  @Override
  public @Nullable PathFindingTask startNavigation(final @NotNull Player player, final @NotNull Location end,
                                                   final boolean safeMode, final double recalcDistance,
                                                   final @NotNull Set<Material> allowedMaterials,
                                                   final @NotNull Particle.DustOptions dustOptions) {
    return startNavigation(player, end, safeMode, recalcDistance, allowedMaterials, Set.of(), dustOptions);
  }

  @Override
  public @Nullable PathFindingTask startNavigation(final @NotNull Player player, final @NotNull Location end,
                                                   final boolean safeMode, final double recalcDistance,
                                                   final @NotNull Set<Material> allowedMaterials,
                                                   final @NotNull Set<Material> ignoredMaterials,
                                                   final @NotNull Particle.DustOptions dustOptions) {
    final var task = new PathFindingTask(player, end, safeMode, allowedMaterials, ignoredMaterials, recalcDistance, -1, dustOptions);
    
    if (!new PathFindingStartEvent(task).callEvent())
      return null;
    
    task.runTaskTimer(DreamAPI.getAPI().plugin(), 0L, 10L);
    task.triggerRecalc();
    this.playerNavigations.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(task);
    return task;
  }

  @Override
  public @Nullable PathFindingTask startNavigation(final @NotNull Player player, final @NotNull Location end,
                                                   final boolean safeMode, final double recalcDistance,
                                                   final @NotNull Consumer<List<Location>> onRecalc) {
    return startNavigation(player, end, safeMode, recalcDistance, Set.of(), Set.of(), onRecalc);
  }

  @Override
  public @Nullable PathFindingTask startNavigation(final @NotNull Player player, final @NotNull Location end,
                                                   final boolean safeMode, final double recalcDistance,
                                                   final @NotNull Set<Material> allowedMaterials,
                                                   final @NotNull Consumer<List<Location>> onRecalc) {
    return startNavigation(player, end, safeMode, recalcDistance, allowedMaterials, Set.of(), onRecalc);
  }

  @Override
  public @Nullable PathFindingTask startNavigation(final @NotNull Player player, final @NotNull Location end,
                                                   final boolean safeMode, final double recalcDistance,
                                                   final @NotNull Set<Material> allowedMaterials,
                                                   final @NotNull Set<Material> ignoredMaterials,
                                                   final @NotNull Consumer<List<Location>> onRecalc) {
    final var task = new PathFindingTask(player, end, safeMode, allowedMaterials, ignoredMaterials, recalcDistance, -1, onRecalc);
    
    if (!new PathFindingStartEvent(task).callEvent())
      return null;
    
    task.runTaskTimer(DreamAPI.getAPI().plugin(), 0L, 10L);
    task.triggerRecalc();
    this.playerNavigations.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(task);
    return task;
  }

  @Override
  public @Nullable PathFindingTask startNavigation(@NotNull Player player, @NotNull Location end, boolean safeMode, double recalcDistance, @NotNull Particle particle) {
    return startNavigation(player, end, safeMode, recalcDistance, Set.of(), Set.of(), particle);
  }

  @Override
  public @Nullable PathFindingTask startNavigation(@NotNull Player player, @NotNull Location end, boolean safeMode, double recalcDistance, @NotNull Set<Material> allowedMaterials, @NotNull Particle particle) {
    return startNavigation(player, end, safeMode, recalcDistance, allowedMaterials, Set.of(), particle);
  }

  @Override
  public @Nullable PathFindingTask startNavigation(@NotNull Player player, @NotNull Location end, boolean safeMode, double recalcDistance, @NotNull Set<Material> allowedMaterials, @NotNull Set<Material> ignoredMaterials, @NotNull Particle particle) {
    final var task = new PathFindingTask(player, end, safeMode, allowedMaterials, ignoredMaterials, recalcDistance, -1, particle);
    
    if (!new PathFindingStartEvent(task).callEvent())
      return null;
      
    task.runTaskTimer(DreamAPI.getAPI().plugin(), 0L, 10L);
    task.triggerRecalc();
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

  // ###############################################################
  // ---------------- ITEM DISPLAY NAVIGATION ----------------------
  // ###############################################################

  @Override
  public @Nullable ItemDisplayNavigationTask startItemDisplayNavigation(
      final @NotNull Player player, final @NotNull Location end,
      final boolean safeMode, final double recalcDistance,
      final @Nullable ItemStack item) {
    return startItemDisplayNavigation(player, end, safeMode, recalcDistance, -1, Set.of(), Set.of(), null, item, null, null);
  }

  @Override
  public @Nullable ItemDisplayNavigationTask startItemDisplayNavigation(
      final @NotNull Player player, final @NotNull Location end,
      final boolean safeMode, final double recalcDistance,
      final double displayRadius, final @Nullable ItemStack item) {
    return startItemDisplayNavigation(player, end, safeMode, recalcDistance, displayRadius, Set.of(), Set.of(), null, item, null, null);
  }

  @Override
  public @Nullable ItemDisplayNavigationTask startItemDisplayNavigation(
      final @NotNull Player player, final @NotNull Location end,
      final boolean safeMode, final double recalcDistance,
      final double displayRadius,
      final @Nullable ItemStack startItem, final @Nullable ItemStack middleItem, final @Nullable ItemStack endItem) {
    return startItemDisplayNavigation(player, end, safeMode, recalcDistance, displayRadius, Set.of(), Set.of(), startItem, middleItem, endItem, null);
  }

  @Override
  public @Nullable ItemDisplayNavigationTask startItemDisplayNavigation(
      final @NotNull Player player, final @NotNull Location end,
      final boolean safeMode, final double recalcDistance,
      final double displayRadius,
      final @Nullable ItemStack startItem, final @Nullable ItemStack middleItem, final @Nullable ItemStack endItem,
      final @Nullable Consumer<ItemDisplay> animator) {
    return startItemDisplayNavigation(player, end, safeMode, recalcDistance, displayRadius, Set.of(), Set.of(), startItem, middleItem, endItem, animator);
  }

  @Override
  public @Nullable ItemDisplayNavigationTask startItemDisplayNavigation(
      final @NotNull Player player, final @NotNull Location end,
      final boolean safeMode, final double recalcDistance,
      final double displayRadius, final @NotNull Set<Material> allowedMaterials,
      final @Nullable ItemStack item) {
    return startItemDisplayNavigation(player, end, safeMode, recalcDistance, displayRadius, allowedMaterials, Set.of(), null, item, null, null);
  }

  @Override
  public @Nullable ItemDisplayNavigationTask startItemDisplayNavigation(
      final @NotNull Player player, final @NotNull Location end,
      final boolean safeMode, final double recalcDistance,
      final double displayRadius, final @NotNull Set<Material> allowedMaterials,
      final @NotNull Set<Material> ignoredMaterials, final @Nullable ItemStack item) {
    return startItemDisplayNavigation(player, end, safeMode, recalcDistance, displayRadius, allowedMaterials, ignoredMaterials, null, item, null, null);
  }

  @Override
  public @Nullable ItemDisplayNavigationTask startItemDisplayNavigation(
      final @NotNull Player player, final @NotNull Location end,
      final boolean safeMode, final double recalcDistance, final double displayRadius,
      final @NotNull Set<Material> allowedMaterials, final @NotNull Set<Material> ignoredMaterials,
      final @Nullable ItemStack startItem, final @Nullable ItemStack middleItem, final @Nullable ItemStack endItem,
      final @Nullable Consumer<ItemDisplay> animator) {
    return startItemDisplayNavigation(player, end, safeMode, recalcDistance, displayRadius, 1, false, allowedMaterials, ignoredMaterials, startItem, middleItem, endItem, animator);
  }

  @Override
  public @Nullable ItemDisplayNavigationTask startItemDisplayNavigation(
      final @NotNull Player player, final @NotNull Location end,
      final boolean safeMode, final double recalcDistance, final double displayRadius,
      final int spacing, final boolean facePlayer, final @Nullable ItemStack item) {
    return startItemDisplayNavigation(player, end, safeMode, recalcDistance, displayRadius, spacing, facePlayer, Set.of(), Set.of(), null, item, null, null);
  }

  @Override
  public @Nullable ItemDisplayNavigationTask startItemDisplayNavigation(
      final @NotNull Player player, final @NotNull Location end,
      final boolean safeMode, final double recalcDistance, final double displayRadius,
      final int spacing, final boolean facePlayer,
      final @NotNull Set<Material> allowedMaterials, final @NotNull Set<Material> ignoredMaterials,
      final @Nullable ItemStack startItem, final @Nullable ItemStack middleItem, final @Nullable ItemStack endItem,
      final @Nullable Consumer<ItemDisplay> animator) {

    final var task = new ItemDisplayNavigationTask(
        player, end, safeMode, allowedMaterials, ignoredMaterials, recalcDistance, displayRadius,
        spacing, facePlayer, startItem, middleItem, endItem, animator);

    if (!new PathFindingStartEvent(task).callEvent())
      return null;

    task.runTaskTimer(DreamAPI.getAPI().plugin(), 0L, 1L);
    task.triggerRecalc();
    this.playerNavigations.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(task);
    return task;
  }

  @Override
  public void stopNavigation(final @NotNull AbstractNavigateTask task) {
    if (!task.isCancelled()) task.cancel();

    // Check player navigations
    for (final var entry : this.playerNavigations.entrySet()) {
      if (entry.getValue().remove(task)) {
        if (entry.getValue().isEmpty()) {
          this.playerNavigations.remove(entry.getKey());
        }
        return;
      }
    }

    // Check entity movements (in case it is passed here)
    if (task instanceof EntityMovementTask entityTask)
      this.entityMovements.remove(entityTask.getEntity().getUniqueId(), entityTask);
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
    tasks.removeIf(AbstractNavigateTask::isCancelled);
    if (tasks.isEmpty()) {
      this.playerNavigations.remove(player.getUniqueId());
      return false;
    }
    return true;
  }

  @Override
  public @NotNull Set<AbstractNavigateTask> getNavigationTasks(final @NotNull Player player) {
    final var tasks = this.playerNavigations.getOrDefault(player.getUniqueId(), new HashSet<>());
    tasks.removeIf(AbstractNavigateTask::isCancelled);
    if (tasks.isEmpty())
      this.playerNavigations.remove(player.getUniqueId());
    return Set.copyOf(tasks);
  }

  // ###############################################################
  // -------------------- ENTITY MOVEMENT --------------------------
  // ###############################################################

  @Override
  public @Nullable EntityMovementTask moveEntityTo(final @NotNull Entity entity, final @NotNull Location end,
                                                   final boolean safeMode, final double speed) {
    return moveEntityTo(entity, end, safeMode, speed, Set.of());
  }

  @Override
  public @Nullable EntityMovementTask moveEntityTo(final @NotNull Entity entity, final @NotNull Location end,
                                                   final boolean safeMode, final double speed,
                                                   final @NotNull Set<Material> allowedMaterials) {
    return moveEntityTo(entity, end, safeMode, speed, allowedMaterials, Set.of());
  }

  @Override
  public @Nullable EntityMovementTask moveEntityTo(final @NotNull Entity entity, final @NotNull Location end,
                                                   final boolean safeMode, final double speed,
                                                   final @NotNull Set<Material> allowedMaterials,
                                                   final @NotNull Set<Material> ignoredMaterials) {
    stopEntityMovement(entity);
    final var task = new EntityMovementTask(entity, end, safeMode, allowedMaterials, ignoredMaterials, speed);

    if (!new PathFindingStartEvent(task).callEvent())
      return null;
    
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
  public void stopEntityMovement(final @NotNull EntityMovementTask task) {
    if (!task.isCancelled()) task.cancel();
    final var currentTask = this.entityMovements.get(task.getEntity().getUniqueId());
    if (currentTask == task)
      this.entityMovements.remove(task.getEntity().getUniqueId());
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
