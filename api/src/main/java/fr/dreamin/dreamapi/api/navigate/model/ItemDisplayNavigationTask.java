package fr.dreamin.dreamapi.api.navigate.model;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.navigate.event.player.PathFindingFinishEvent;
import fr.dreamin.dreamapi.api.navigate.event.player.PathFindingRecalcEvent;
import fr.dreamin.dreamapi.api.navigate.event.player.PathFindingStopEvent;
import fr.dreamin.dreamapi.api.navigate.event.player.PathFindingWaypointReachEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

@Getter
public final class ItemDisplayNavigationTask extends AbstractNavigateTask {

  private final Player player;
  private final double recalcMinDistance;
  private final double displayRadius;

  private final ItemStack startItem;
  private final ItemStack middleItem;
  private final ItemStack endItem;

  @Nullable
  private final Consumer<ItemDisplay> animator;

  @Getter
  private int spacing = 1;

  @Getter
  private boolean facePlayer = false;

  @Getter
  private ItemDisplay.Billboard billboard = ItemDisplay.Billboard.FIXED;

  private Location lastCalcLocation;
  private final Map<Integer, ItemDisplay> activeDisplays = new HashMap<>();
  private final TreeSet<Integer> plannedDisplayIndices = new TreeSet<>();

  // ###############################################################
  // -------------------------- CANCEL -----------------------------
  // ###############################################################

  @Override
  public void cancel() {
    super.cancel();
    clearDisplays();
    this.plannedDisplayIndices.clear();
    if (this.finished)
      new PathFindingFinishEvent(this).callEvent();
    else
      new PathFindingStopEvent(this).callEvent();
  }

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  public ItemDisplayNavigationTask(final @NotNull Player player, final @NotNull Location targetLocation,
                                   final boolean safeMode, final double recalcMinDistance,
                                   final @Nullable ItemStack item) {
    this(player, targetLocation, safeMode, Set.of(), Set.of(), recalcMinDistance, -1, 1, false, null, item, null, null);
  }

  public ItemDisplayNavigationTask(final @NotNull Player player, final @NotNull Location targetLocation,
                                   final boolean safeMode, final double recalcMinDistance,
                                   final double displayRadius, final @Nullable ItemStack item) {
    this(player, targetLocation, safeMode, Set.of(), Set.of(), recalcMinDistance, displayRadius, 1, false, null, item, null, null);
  }

  public ItemDisplayNavigationTask(final @NotNull Player player, final @NotNull Location targetLocation,
                                   final boolean safeMode, final double recalcMinDistance,
                                   final double displayRadius,
                                   final @Nullable ItemStack startItem, final @Nullable ItemStack middleItem, final @Nullable ItemStack endItem) {
    this(player, targetLocation, safeMode, Set.of(), Set.of(), recalcMinDistance, displayRadius, 1, false, startItem, middleItem, endItem, null);
  }

  public ItemDisplayNavigationTask(final @NotNull Player player, final @NotNull Location targetLocation,
                                   final boolean safeMode, final double recalcMinDistance,
                                   final double displayRadius,
                                   final @Nullable ItemStack startItem, final @Nullable ItemStack middleItem, final @Nullable ItemStack endItem,
                                   final @Nullable Consumer<ItemDisplay> animator) {
    this(player, targetLocation, safeMode, Set.of(), Set.of(), recalcMinDistance, displayRadius, 1, false, startItem, middleItem, endItem, animator);
  }

  public ItemDisplayNavigationTask(final @NotNull Player player, final @NotNull Location targetLocation,
                                   final boolean safeMode, final @NotNull Set<Material> allowedMaterials,
                                   final @NotNull Set<Material> ignoredMaterials, final double recalcMinDistance,
                                   final double displayRadius,
                                   final @Nullable ItemStack startItem, final @Nullable ItemStack middleItem, final @Nullable ItemStack endItem,
                                   final @Nullable Consumer<ItemDisplay> animator) {
    this(player, targetLocation, safeMode, allowedMaterials, ignoredMaterials, recalcMinDistance, displayRadius, 1, false, startItem, middleItem, endItem, animator);
  }

  public ItemDisplayNavigationTask(final @NotNull Player player, final @NotNull Location targetLocation,
                                   final boolean safeMode, final @NotNull Set<Material> allowedMaterials,
                                   final @NotNull Set<Material> ignoredMaterials, final double recalcMinDistance,
                                   final double displayRadius,
                                   final int spacing, final boolean facePlayer,
                                   final @Nullable ItemStack startItem, final @Nullable ItemStack middleItem, final @Nullable ItemStack endItem,
                                   final @Nullable Consumer<ItemDisplay> animator) {
    super(targetLocation, new AStartPathFinder(safeMode, allowedMaterials, ignoredMaterials));
    this.player = player;
    this.recalcMinDistance = recalcMinDistance;
    this.displayRadius = displayRadius;
    this.spacing = Math.max(1, spacing);
    this.facePlayer = facePlayer;
    this.billboard = facePlayer ? ItemDisplay.Billboard.CENTER : ItemDisplay.Billboard.FIXED;
    this.startItem = startItem;
    this.middleItem = middleItem;
    this.endItem = endItem;
    this.animator = animator;
    this.lastCalcLocation = player.getLocation().clone();
  }

  // ###############################################################
  // -------------------------- SETTERS ----------------------------
  // ###############################################################

  public void setSpacing(int spacing) {
    this.spacing = Math.max(1, spacing);
    computePlannedDisplayIndices();
    if (this.currentPath != null && !this.currentPath.isEmpty()) {
      manageDisplays(this.player.getLocation());
      for (final var entry : this.activeDisplays.entrySet()) {
        final int idx = entry.getKey();
        final var display = entry.getValue();
        if (display != null && display.isValid()) {
          final int nextIdx = getNextDisplayIndex(idx, this.currentPath.size());
          if (nextIdx != -1) {
            final var loc = this.currentPath.get(idx);
            final var nextLoc = this.currentPath.get(nextIdx);
            final var dispLoc = display.getLocation();
            dispLoc.setYaw(AStartPathFinder.getYaw(loc, nextLoc));
            dispLoc.setPitch(AStartPathFinder.getPitch(loc, nextLoc));
            display.teleport(dispLoc);
          } else {
            final var prevIdx = this.plannedDisplayIndices.lower(idx);
            if (prevIdx != null) {
              final var loc = this.currentPath.get(idx);
              final var prevLoc = this.currentPath.get(prevIdx);
              final var dispLoc = display.getLocation();
              dispLoc.setYaw(AStartPathFinder.getYaw(prevLoc, loc));
              dispLoc.setPitch(AStartPathFinder.getPitch(prevLoc, loc));
              display.teleport(dispLoc);
            }
          }
        }
      }
    }
  }

  public void setFacePlayer(boolean facePlayer) {
    this.facePlayer = facePlayer;
    this.billboard = facePlayer ? ItemDisplay.Billboard.CENTER : ItemDisplay.Billboard.FIXED;
    for (final var display : this.activeDisplays.values()) {
      if (display != null && display.isValid()) {
        display.setBillboard(this.billboard);
      }
    }
  }

  public void setBillboard(final @NotNull ItemDisplay.Billboard billboard) {
    this.billboard = billboard;
    this.facePlayer = (billboard == ItemDisplay.Billboard.CENTER);
    for (final var display : this.activeDisplays.values()) {
      if (display != null && display.isValid()) {
        display.setBillboard(billboard);
      }
    }
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

    // 1. Update waypoint index bidirectionally (advancing and backtracking)
    final double closestDistSq = updateCurrentIndex(playerLoc);

    // 2. Synchronize visible displays (no flickering: removes passed, restores backed up, spawns ahead)
    manageDisplays(playerLoc);

    // 3. Tick animation
    if (this.animator != null) {
      for (final var display : this.activeDisplays.values()) {
        if (display.isValid())
          this.animator.accept(display);
      }
    }

    // 4. Trigger recalculation ONLY if:
    // - Initial start (no path yet)
    // - Player deviated significantly from the path (> recalcMinDistance)
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

    final var playerBlockLoc = this.player.getLocation().getBlock().getLocation();
    final var targetBlockLoc = this.targetLocation.getBlock().getLocation();

    Bukkit.getScheduler().runTaskAsynchronously(DreamAPI.getAPI().plugin(), () -> {
      final var newPath = this.pathFinder.findPath(playerBlockLoc, targetBlockLoc);
      Bukkit.getScheduler().runTask(DreamAPI.getAPI().plugin(), () -> {
        if (!newPath.isEmpty()) {
          this.currentPath = newPath;
          this.currentPathIndex = 0;
          clearDisplays(); // Clear displays only on genuine deviation / initial path
          computePlannedDisplayIndices();
          new PathFindingRecalcEvent(this, java.util.List.copyOf(newPath)).callEvent();
          manageDisplays(this.player.getLocation());
        }
        this.recalculating = false;
      });
    });
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  /**
   * Updates {@link #currentPathIndex} to the waypoint closest to {@code playerLoc},
   * scanning bidirectionally to support both advancing and backtracking.
   *
   * @return squared distance to the closest waypoint, or Double.MAX_VALUE if no path.
   */
  private double updateCurrentIndex(final @NotNull Location playerLoc) {
    if (this.currentPath == null || this.currentPath.isEmpty()) return Double.MAX_VALUE;

    double minDist = Double.MAX_VALUE;
    int bestIndex = this.currentPathIndex;

    // 1. Search in local window around currentPathIndex (prevents jumping across walls/hairpins)
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

  private void computePlannedDisplayIndices() {
    this.plannedDisplayIndices.clear();
    if (this.currentPath == null || this.currentPath.isEmpty()) return;

    final int totalPoints = this.currentPath.size();
    if (totalPoints == 1) {
      this.plannedDisplayIndices.add(0);
      return;
    }

    final int maxIndex = totalPoints - 1;
    final int spacingStep = Math.max(1, this.spacing);

    if (spacingStep == 1) {
      for (int i = 0; i < totalPoints; i++) {
        this.plannedDisplayIndices.add(i);
      }
      return;
    }

    // Evenly distribute intervals along the entire path length so start and end align and gaps remain balanced
    final int intervals = Math.max(1, (int) Math.round((double) maxIndex / spacingStep));
    for (int step = 0; step <= intervals; step++) {
      final int index = (int) Math.round((double) (step * maxIndex) / intervals);
      this.plannedDisplayIndices.add(index);
    }
  }

  private boolean shouldDisplayAtIndex(int index, int pathSize) {
    return this.plannedDisplayIndices.contains(index);
  }

  private int getNextDisplayIndex(int currentIndex, int pathSize) {
    final var next = this.plannedDisplayIndices.higher(currentIndex);
    return next != null ? next : -1;
  }

  private void manageDisplays(final @NotNull Location playerLoc) {
    if (this.currentPath == null || this.currentPath.isEmpty()) return;

    final var displayRadiusSq = this.displayRadius > 0 ? this.displayRadius * this.displayRadius : Double.MAX_VALUE;

    // 1. Remove displays that are behind the player, out of display radius, or not matching spacing
    final var it = this.activeDisplays.entrySet().iterator();
    while (it.hasNext()) {
      final var entry = it.next();
      final var index = entry.getKey();
      final var display = entry.getValue();

      if (index < this.currentPathIndex
          || (this.displayRadius > 0 && display.getLocation().distanceSquared(playerLoc) > displayRadiusSq)
          || !shouldDisplayAtIndex(index, this.currentPath.size())) {
        display.remove();
        it.remove();
      }
    }

    // 2. Spawn displays within radius that match the spacing
    for (int i = this.currentPathIndex; i < this.currentPath.size(); i++) {
      if (!shouldDisplayAtIndex(i, this.currentPath.size())) {
        continue;
      }

      final var loc = this.currentPath.get(i);
      if (this.displayRadius > 0 && loc.distanceSquared(playerLoc) > displayRadiusSq) {
        continue;
      }

      if (!this.activeDisplays.containsKey(i)) {
        final var itemToSpawn = getItemForIndex(i, this.currentPath.size());
        if (itemToSpawn != null && itemToSpawn.getType() != Material.AIR) {
          final var spawnLoc = loc.clone().add(0.5, 0.5, 0.5);

          // Point directly towards the NEXT displayed item (yaw and pitch)
          final int nextDisplayIdx = getNextDisplayIndex(i, this.currentPath.size());
          if (nextDisplayIdx != -1) {
            final var nextLoc = this.currentPath.get(nextDisplayIdx);
            spawnLoc.setYaw(AStartPathFinder.getYaw(loc, nextLoc));
            spawnLoc.setPitch(AStartPathFinder.getPitch(loc, nextLoc));
          } else {
            final var prevDisplayIdx = this.plannedDisplayIndices.lower(i);
            if (prevDisplayIdx != null) {
              final var prevLoc = this.currentPath.get(prevDisplayIdx);
              spawnLoc.setYaw(AStartPathFinder.getYaw(prevLoc, loc));
              spawnLoc.setPitch(AStartPathFinder.getPitch(prevLoc, loc));
            }
          }

          final var display = spawnLoc.getWorld().spawn(spawnLoc, ItemDisplay.class, d -> {
            d.setItemStack(itemToSpawn);
            d.setBillboard(this.billboard);
            try {
              d.setVisibleByDefault(false);
            } catch (NoSuchMethodError ignored) {}
          });

          // Fallback if setVisibleByDefault is not strictly supported or works differently
          try {
            if (!display.isVisibleByDefault())
              this.player.showEntity(DreamAPI.getAPI().plugin(), display);
          } catch (NoSuchMethodError ignored) {}
          
          this.activeDisplays.put(i, display);
        }
      }
    }
  }

  private @Nullable ItemStack getItemForIndex(int index, int pathSize) {
    if (index == 0 && this.startItem != null) return this.startItem;
    if (index == pathSize - 1 && this.endItem != null) return this.endItem;
    return this.middleItem;
  }

  private void clearDisplays() {
    for (final var display : this.activeDisplays.values()) {
      if (display != null && display.isValid())
        display.remove();
    }
    this.activeDisplays.clear();
  }

}
