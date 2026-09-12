package fr.dreamin.dreamapi.api.navigate.model;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.navigate.event.player.PathFindingFinishEvent;
import fr.dreamin.dreamapi.api.navigate.event.player.PathFindingRecalcEvent;
import fr.dreamin.dreamapi.api.navigate.event.player.PathFindingStopEvent;
import fr.dreamin.dreamapi.api.navigate.event.player.PathFindingWaypointReachEvent;
import lombok.Getter;
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

  private Location lastCalcLocation;
  private final Map<Integer, ItemDisplay> activeDisplays = new HashMap<>();

  // ###############################################################
  // -------------------------- CANCEL -----------------------------
  // ###############################################################

  @Override
  public void cancel() {
    super.cancel();
    clearDisplays();
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
    this(player, targetLocation, safeMode, Set.of(), Set.of(), recalcMinDistance, -1, null, item, null, null);
  }

  public ItemDisplayNavigationTask(final @NotNull Player player, final @NotNull Location targetLocation,
                                   final boolean safeMode, final double recalcMinDistance,
                                   final double displayRadius, final @Nullable ItemStack item) {
    this(player, targetLocation, safeMode, Set.of(), Set.of(), recalcMinDistance, displayRadius, null, item, null, null);
  }

  public ItemDisplayNavigationTask(final @NotNull Player player, final @NotNull Location targetLocation,
                                   final boolean safeMode, final double recalcMinDistance,
                                   final double displayRadius,
                                   final @Nullable ItemStack startItem, final @Nullable ItemStack middleItem, final @Nullable ItemStack endItem) {
    this(player, targetLocation, safeMode, Set.of(), Set.of(), recalcMinDistance, displayRadius, startItem, middleItem, endItem, null);
  }

  public ItemDisplayNavigationTask(final @NotNull Player player, final @NotNull Location targetLocation,
                                   final boolean safeMode, final double recalcMinDistance,
                                   final double displayRadius,
                                   final @Nullable ItemStack startItem, final @Nullable ItemStack middleItem, final @Nullable ItemStack endItem,
                                   final @Nullable Consumer<ItemDisplay> animator) {
    this(player, targetLocation, safeMode, Set.of(), Set.of(), recalcMinDistance, displayRadius, startItem, middleItem, endItem, animator);
  }

  public ItemDisplayNavigationTask(final @NotNull Player player, final @NotNull Location targetLocation,
                                   final boolean safeMode, final @NotNull Set<Material> allowedMaterials,
                                   final @NotNull Set<Material> ignoredMaterials, final double recalcMinDistance,
                                   final double displayRadius,
                                   final @Nullable ItemStack startItem, final @Nullable ItemStack middleItem, final @Nullable ItemStack endItem,
                                   final @Nullable Consumer<ItemDisplay> animator) {
    super(targetLocation, new AStartPathFinder(safeMode, allowedMaterials, ignoredMaterials));
    this.player = player;
    this.recalcMinDistance = recalcMinDistance;
    this.displayRadius = displayRadius;
    this.startItem = startItem;
    this.middleItem = middleItem;
    this.endItem = endItem;
    this.animator = animator;
    this.lastCalcLocation = player.getLocation().clone();
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

    updateCurrentIndex(playerLoc);
    manageDisplays(playerLoc);

    if (this.animator != null) {
      for (final var display : this.activeDisplays.values()) {
        if (display.isValid())
          this.animator.accept(display);
      }
    }

    final var distMoved = playerLoc.distanceSquared(this.lastCalcLocation);
    if (distMoved < (this.recalcMinDistance * this.recalcMinDistance))
      return;

    this.lastCalcLocation = playerLoc.clone();

    if (this.recalculating) return;
    this.recalculating = true;

    final var playerBlockLoc = playerLoc.getBlock().getLocation();
    final var targetBlockLoc = this.targetLocation.getBlock().getLocation();

    Bukkit.getScheduler().runTaskAsynchronously(DreamAPI.getAPI().plugin(), () -> {
      final var newPath = this.pathFinder.findPath(playerBlockLoc, targetBlockLoc);
      Bukkit.getScheduler().runTask(DreamAPI.getAPI().plugin(), () -> {
        if (!newPath.isEmpty()) {
          this.currentPath = newPath;
          this.currentPathIndex = 0;
          clearDisplays(); // Clear old path displays
          new PathFindingRecalcEvent(this, java.util.List.copyOf(newPath)).callEvent();
        }
        this.recalculating = false;
      });
    });
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void updateCurrentIndex(final @NotNull Location playerLoc) {
    if (this.currentPath == null || this.currentPath.isEmpty()) return;

    double minDist = Double.MAX_VALUE;
    int bestIndex = this.currentPathIndex;

    for (int i = this.currentPathIndex; i < this.currentPath.size(); i++) {
      final var dist = playerLoc.distanceSquared(this.currentPath.get(i));
      if (dist < minDist) {
        minDist = dist;
        bestIndex = i;
      }
      else if (dist > minDist + 9)
        break;
    }

    if (bestIndex != this.currentPathIndex) {
      this.currentPathIndex = bestIndex;
      new PathFindingWaypointReachEvent(this, this.currentPath.get(bestIndex), bestIndex).callEvent();
    }
  }

  private void manageDisplays(final @NotNull Location playerLoc) {
    if (this.currentPath == null || this.currentPath.isEmpty()) return;

    final var displayRadiusSq = this.displayRadius > 0 ? this.displayRadius * this.displayRadius : Double.MAX_VALUE;

    // Remove displays that are out of bounds or behind the player
    final var it = this.activeDisplays.entrySet().iterator();
    while (it.hasNext()) {
      final var entry = it.next();
      final var index = entry.getKey();
      final var display = entry.getValue();

      if (index < this.currentPathIndex || (this.displayRadius > 0 && display.getLocation().distanceSquared(playerLoc) > displayRadiusSq)) {
        display.remove();
        it.remove();
      }
    }

    // Spawn new displays within radius
    for (int i = this.currentPathIndex; i < this.currentPath.size(); i++) {
      final var loc = this.currentPath.get(i);
      if (this.displayRadius > 0 && loc.distanceSquared(playerLoc) > displayRadiusSq) {
        continue;
      }

      if (!this.activeDisplays.containsKey(i)) {
        final var itemToSpawn = getItemForIndex(i, this.currentPath.size());
        if (itemToSpawn != null && itemToSpawn.getType() != Material.AIR) {
          final var spawnLoc = loc.clone().add(0.5, 0.5, 0.5);
          final var display = spawnLoc.getWorld().spawn(spawnLoc, ItemDisplay.class, d -> {
            d.setItemStack(itemToSpawn);
            d.setBillboard(ItemDisplay.Billboard.CENTER);
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
