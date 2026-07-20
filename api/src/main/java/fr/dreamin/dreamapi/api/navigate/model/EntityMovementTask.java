package fr.dreamin.dreamapi.api.navigate.model;

import fr.dreamin.dreamapi.api.DreamAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import fr.dreamin.dreamapi.api.navigate.event.entity.EntityMovementStopEvent;
import fr.dreamin.dreamapi.api.navigate.event.entity.EntityMovementFinishEvent;
import fr.dreamin.dreamapi.api.navigate.event.entity.EntityMovementRecalcEvent;
import fr.dreamin.dreamapi.api.navigate.event.entity.EntityMovementWaypointReachEvent;

/**
 * A recurring Bukkit task that moves an entity along an A*-computed path
 * by teleporting it incrementally toward each waypoint.
 *
 * <p>This task is managed by {@code NavigateService} — do not start/cancel it manually.</p>
 */
@Getter
public final class EntityMovementTask extends BukkitRunnable {

  private static final double WAYPOINT_REACH_DISTANCE_SQ = 0.6 * 0.6;
  private static final double ARRIVAL_DISTANCE_SQ = 1.5 * 1.5;

  private final Entity entity;
  private final Location targetLocation;
  private final AStartPathFinder pathFinder;
  private final double speed;

  /** The latest computed path. May be null before the first computation completes. */
  private List<Location> currentPath;

  /**
   * Index of the current target waypoint in {@link #currentPath}.
   * Reflects how far along the path the entity currently is.
   */
  private int currentPathIndex = 0;

  private boolean recalculating = false;
  private boolean finished = false;

  // ###############################################################
  // -------------------------- CANCEL -----------------------------
  // ###############################################################

  @Override
  public void cancel() {
    super.cancel();
    if (this.finished)
      new EntityMovementFinishEvent(this).callEvent();
    else
      new EntityMovementStopEvent(this).callEvent();

  }

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  /**
   * @param speed Movement speed in blocks per tick (e.g. {@code 0.2} for a slow walk,
   *              {@code 0.4} for a fast walk).
   */
  public EntityMovementTask(final @NotNull Entity entity, final @NotNull Location targetLocation,
                            final boolean safeMode, final @NotNull Set<Material> allowedMaterials,
                            final double speed) {
    this.entity = entity;
    this.targetLocation = targetLocation;
    this.pathFinder = new AStartPathFinder(safeMode, allowedMaterials);
    this.speed = speed;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void run() {
    if (!this.entity.isValid()) {
      cancel();
      return;
    }

    final var entityLoc = this.entity.getLocation();

    // Arrived at destination
    if (entityLoc.distanceSquared(this.targetLocation) < ARRIVAL_DISTANCE_SQ) {
      this.finished = true;
      cancel();
      return;
    }

    // Path exists and not exhausted → move along it
    if (this.currentPath != null && !this.currentPath.isEmpty()
      && this.currentPathIndex < this.currentPath.size()) {
      moveAlongPath(entityLoc);
    }

    // No path yet or path finished → trigger initial / re-calculation
    if (!this.recalculating && (this.currentPath == null || this.currentPathIndex >= this.currentPath.size())) {
      triggerRecalc();
    }
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void moveAlongPath(final @NotNull Location entityLoc) {
    // Advance waypoint index while the entity is within reach of the current waypoint CENTER
    // (block corner + 0.5 on X/Z). Using the center is critical — without it, the entity
    // never advances past its own starting block (dist ≈ 0 to the center → early return).
    while (this.currentPathIndex < this.currentPath.size()) {
      final var center = this.currentPath.get(this.currentPathIndex).clone().add(0.5, 0, 0.5);
      if (entityLoc.distanceSquared(center) >= WAYPOINT_REACH_DISTANCE_SQ) break;
      new EntityMovementWaypointReachEvent(this, this.currentPath.get(this.currentPathIndex), this.currentPathIndex).callEvent();
      this.currentPathIndex++;
    }

    if (this.currentPathIndex >= this.currentPath.size()) return;

    final var waypointCenter = this.currentPath.get(this.currentPathIndex).clone().add(0.5, 0, 0.5);

    final var dx = waypointCenter.getX() - entityLoc.getX();
    final var dy = waypointCenter.getY() - entityLoc.getY();
    final var dz = waypointCenter.getZ() - entityLoc.getZ();
    final var dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

    if (dist < 0.01) return;

    // Move exactly `speed` blocks toward the waypoint, or snap to it if closer
    final var step = Math.min(this.speed, dist);
    final var newX = entityLoc.getX() + (dx / dist) * step;
    final var newY = entityLoc.getY() + (dy / dist) * step;
    final var newZ = entityLoc.getZ() + (dz / dist) * step;

    final var newLoc = new Location(
      entityLoc.getWorld(), newX, newY, newZ,
      AStartPathFinder.getYaw(entityLoc, waypointCenter), 0f
    );

    this.entity.teleport(newLoc);
  }

  private void triggerRecalc() {
    this.recalculating = true;

    // Capture locations on main thread before going async
    final var entityBlockLoc = this.entity.getLocation().getBlock().getLocation();
    final var targetBlockLoc = this.targetLocation.getBlock().getLocation();

    Bukkit.getScheduler().runTaskAsynchronously(DreamAPI.getAPI().plugin(), () -> {
      final var newPath = this.pathFinder.findPath(entityBlockLoc, targetBlockLoc);
      Bukkit.getScheduler().runTask(DreamAPI.getAPI().plugin(), () -> {
        if (!newPath.isEmpty()) {
          this.currentPath = newPath;
          this.currentPathIndex = 0;
          new EntityMovementRecalcEvent(this, List.copyOf(newPath)).callEvent();
        }
        this.recalculating = false;
      });
    });
  }

}
