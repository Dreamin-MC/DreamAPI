package fr.dreamin.dreamapi.api.navigate.model;

import fr.dreamin.dreamapi.api.DreamAPI;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public final class PathFindingTask extends BukkitRunnable {

  public static Map<UUID, PathFindingTask> ACTIVATE_NAVIGATION = new HashMap<>();

  private final Player player;
  private final Location targetLocation;
  private final AStartPathFinder pathFinder;

  private List<Location> currentPath;

  private Location lastCalcLocation;
  private final double recalcMinDistance;

  private boolean recalculating = false;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  public PathFindingTask(final @NotNull Player player, final @NotNull Location targetLocation, final boolean safeMode, final @NotNull Set<Material> allowedMaterials, final double recalcMinDistance) {
    this.player = player;
    this.targetLocation = targetLocation;
    this.pathFinder = new AStartPathFinder(safeMode, allowedMaterials);
    this.lastCalcLocation = player.getLocation().clone();
    this.recalcMinDistance = recalcMinDistance;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void run() {
    if (!this.player.isOnline()) {
      cancelNavigation();
      return;
    }

    if (this.player.getLocation().distanceSquared(this.targetLocation) < 4) {
      this.player.sendMessage(Component.text("You gave reached your destination"));
      cancelNavigation();
      return;
    }

    final var playerLoc = this.player.getLocation();
    final var distMoved = playerLoc.distanceSquared(this.lastCalcLocation);

    // Always display the current path (old or new) — never blank during recalculation
    displayPathParticles();

    if (distMoved < (this.recalcMinDistance * this.recalcMinDistance))
      return;

    this.lastCalcLocation = playerLoc.clone();

    if (this.recalculating) return;
    this.recalculating = true;

    // Capture block locations on the main thread BEFORE going async (Bukkit API is not thread-safe)
    final var playerBlockLoc = this.player.getLocation().getBlock().getLocation();
    final var targetBlockLoc = this.targetLocation.getBlock().getLocation();

    Bukkit.getScheduler().runTaskAsynchronously(DreamAPI.getAPI().plugin(), () -> {
      final var newPath = this.pathFinder.findPath(playerBlockLoc, targetBlockLoc);
      // Bring the result back to the main thread before updating shared state
      Bukkit.getScheduler().runTask(DreamAPI.getAPI().plugin(), () -> {
        if (newPath != null && !newPath.isEmpty())
          this.currentPath = newPath;
        this.recalculating = false;
      });
    });

  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void displayPathParticles() {
    if (this.currentPath == null || this.currentPath.isEmpty())
      return;

    final var dustOptions = new Particle.DustOptions(Color.fromRGB(220, 20, 60), 1);

    for (final var pathBlock : this.currentPath) {
      this.player.spawnParticle(Particle.DUST, pathBlock.clone().add(0.5, 0.5, 0.5), 3, 0, 0, 0, 0, dustOptions);
    }

  }

  private void cancelNavigation() {
    cancel();
    ACTIVATE_NAVIGATION.remove(this.player.getUniqueId());
  }

}
