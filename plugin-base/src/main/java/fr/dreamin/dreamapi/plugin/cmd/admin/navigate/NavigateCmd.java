package fr.dreamin.dreamapi.plugin.cmd.admin.navigate;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.navigate.model.AStartPathFinder;
import fr.dreamin.dreamapi.api.navigate.service.NavigateService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class NavigateCmd {

  // ###############################################################
  // -------------------- PLAYER NAVIGATION ------------------------
  // ###############################################################

  private Set<Material> parseMaterials(String str) {
    if (str == null || str.isEmpty() || str.equalsIgnoreCase("none")) return Set.of();
    return Arrays.stream(str.split(","))
      .map(s -> {
        try {
          return org.bukkit.Material.valueOf(s.toUpperCase());
        } catch (Exception e) {
          return null;
        }
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
  }

  @CommandDescription("Start navigating to a location with auto particle display")
  @CommandMethod("navigation start <x> <y> <z> <safeMode> [recalcDistance] [ignoredMaterials]")
  @CommandPermission("dreamin.cmd.navigation.start")
  private void navigationStart(
    final @NotNull CommandSender sender,
    @Argument(value = "x") int x,
    @Argument(value = "y") int y,
    @Argument(value = "z") int z,
    @Argument(value = "safeMode") boolean safeMode,
    @Argument(value = "recalcDistance") Double recalcDistance,
    @Argument(value = "ignoredMaterials") String ignoredMaterialsStr
  ) {
    if (!(sender instanceof Player player)) return;

    final double distance = (recalcDistance != null && recalcDistance > 0) ? recalcDistance : 3.0;
    final var targetLocation = new Location(player.getWorld(), x, y, z);
    final var ignored = parseMaterials(ignoredMaterialsStr);

    DreamAPI.getAPI().getService(NavigateService.class).startNavigation(player, targetLocation, safeMode, distance, Set.of(), ignored);
    player.sendMessage(Component.text("▶ Navigation started → X:%s Y:%s Z:%s | safeMode:%s | recalc:%s blocks | ignored:%s"
      .formatted(x, y, z, safeMode, distance, ignored.size()), NamedTextColor.GREEN));
  }

  @CommandDescription("Stop the current navigation")
  @CommandMethod("navigation stop")
  @CommandPermission("dreamin.cmd.navigation.start")
  private void navigationStop(
    final @NotNull CommandSender sender
  ) {
    if (!(sender instanceof Player player)) return;

    final var service = DreamAPI.getAPI().getService(NavigateService.class);
    if (service.isNavigating(player)) {
      service.stopNavigation(player);
      player.sendMessage(Component.text("■ Navigation stopped.", NamedTextColor.YELLOW));
    } else {
      player.sendMessage(Component.text("You are not currently navigating.", NamedTextColor.RED));
    }
  }

  @CommandDescription("Display info about the current navigation task")
  @CommandMethod("navigation info")
  @CommandPermission("dreamin.cmd.navigation.start")
  private void navigationInfo(
    final @NotNull CommandSender sender
  ) {
    if (!(sender instanceof Player player)) return;

    final var service = DreamAPI.getAPI().getService(NavigateService.class);
    final var tasks = service.getNavigationTasks(player);

    if (tasks.isEmpty()) {
      player.sendMessage(Component.text("No active navigation task.", NamedTextColor.RED));
      return;
    }

    player.sendMessage(Component.text("─── Navigation Info (%s active) ───".formatted(tasks.size()), NamedTextColor.GOLD));

    int i = 1;
    for (final var task : tasks) {
      final var path = task.getCurrentPath();
      final var target = task.getTargetLocation();
      final var distToTarget = (int) player.getLocation().distance(target);
      final var pathSize = path != null ? path.size() : 0;
      final var pathIndex = task.getCurrentPathIndex();
      final var remaining = pathSize - pathIndex;
      final var recalculating = task.isRecalculating();

      player.sendMessage(Component.text("Task #%s".formatted(i++), NamedTextColor.GREEN));
      player.sendMessage(Component.text("  Target      : X:%s Y:%s Z:%s".formatted(
        target.getBlockX(), target.getBlockY(), target.getBlockZ()), NamedTextColor.AQUA));
      player.sendMessage(Component.text("  Distance    : %s blocks".formatted(distToTarget), NamedTextColor.AQUA));
      player.sendMessage(Component.text("  Path size   : %s waypoints".formatted(pathSize), NamedTextColor.AQUA));
      player.sendMessage(Component.text("  Path index  : %s / %s (remaining: %s)".formatted(pathIndex, pathSize, remaining), NamedTextColor.AQUA));
      player.sendMessage(Component.text("  Recalculating: %s".formatted(recalculating), recalculating ? NamedTextColor.YELLOW : NamedTextColor.GREEN));
      player.sendMessage(Component.text("  RecalcDistance: %s blocks".formatted(task.getRecalcMinDistance()), NamedTextColor.AQUA));
      player.sendMessage(Component.text("  Mode        : %s".formatted(task.getDustOptions() != null ? "Particle display" : "Callback"), NamedTextColor.AQUA));
    }
  }

  // ###############################################################
  // -------------------- ONE-SHOT PATH ----------------------------
  // ###############################################################

  @CommandDescription("Compute path once and display it (no recurring task)")
  @CommandMethod("navigation path <x> <y> <z> <safeMode> [ignoredMaterials]")
  @CommandPermission("dreamin.cmd.navigation.start")
  private void navigationPath(
    final @NotNull CommandSender sender,
    @Argument(value = "x") int x,
    @Argument(value = "y") int y,
    @Argument(value = "z") int z,
    @Argument(value = "safeMode") boolean safeMode,
    @Argument(value = "ignoredMaterials") String ignoredMaterialsStr
  ) {
    if (!(sender instanceof Player player)) return;

    final var targetLocation = new Location(player.getWorld(), x, y, z);
    final var service = DreamAPI.getAPI().getService(NavigateService.class);
    final var ignored = parseMaterials(ignoredMaterialsStr);

    player.sendMessage(Component.text("⏳ Computing path...", NamedTextColor.YELLOW));

    service.findPathAsync(player.getLocation(), targetLocation, safeMode, Set.of(), ignored, path -> {
      if (path.isEmpty()) {
        player.sendMessage(Component.text("✗ No path found to X:%s Y:%s Z:%s".formatted(x, y, z), NamedTextColor.RED));
        return;
      }

      player.sendMessage(Component.text("✔ Path found: %s waypoints. Displaying for 10s...".formatted(path.size()), NamedTextColor.GREEN));

      // Display the path every 10 ticks for 10 seconds (20 displays)
      final int[] count = {0};
      final var dustOptions = new Particle.DustOptions(Color.fromRGB(0, 200, 255), 1);
      DreamAPI.getAPI().plugin().getServer().getScheduler().runTaskTimer(DreamAPI.getAPI().plugin(), task -> {
        if (count[0]++ >= 20 || !player.isOnline()) {
          task.cancel();
          return;
        }
        service.displayPath(player, path, dustOptions);
      }, 0L, 10L);
    });
  }

  // ###############################################################
  // -------------------- ENTITY MOVEMENT --------------------------
  // ###############################################################

  @CommandDescription("Move the nearest entity to a location using A* pathfinding")
  @CommandMethod("navigation entity move <x> <y> <z> <safeMode> <speed> [ignoredMaterials]")
  @CommandPermission("dreamin.cmd.navigation.start")
  private void navigationEntityMove(
    final @NotNull CommandSender sender,
    @Argument(value = "x") int x,
    @Argument(value = "y") int y,
    @Argument(value = "z") int z,
    @Argument(value = "safeMode") boolean safeMode,
    @Argument(value = "speed") double speed,
    @Argument(value = "ignoredMaterials") String ignoredMaterialsStr
  ) {
    if (!(sender instanceof Player player)) return;

    final var nearby = player.getNearbyEntities(20, 20, 20).stream()
      .filter(e -> !(e instanceof Player))
      .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(player.getLocation())));

    if (nearby.isEmpty()) {
      player.sendMessage(Component.text("No entity found within 20 blocks.", NamedTextColor.RED));
      return;
    }

    final var entity = nearby.get();
    final var target = new Location(player.getWorld(), x, y, z);
    final var service = DreamAPI.getAPI().getService(NavigateService.class);
    final var ignored = parseMaterials(ignoredMaterialsStr);

    service.moveEntityTo(entity, target, safeMode, Math.max(0.05, speed), Set.of(), ignored);
    player.sendMessage(Component.text("▶ Moving %s [%s] → X:%s Y:%s Z:%s | speed:%s | ignored:%s"
      .formatted(entity.getType().name(), entity.getUniqueId().toString().substring(0, 8),
        x, y, z, speed, ignored.size()), NamedTextColor.GREEN));
  }

  @CommandDescription("Stop the movement of the nearest entity")
  @CommandMethod("navigation entity stop")
  @CommandPermission("dreamin.cmd.navigation.start")
  private void navigationEntityStop(
    final @NotNull CommandSender sender
  ) {
    if (!(sender instanceof Player player)) return;

    final var nearby = player.getNearbyEntities(20, 20, 20).stream()
      .filter(e -> !(e instanceof Player))
      .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(player.getLocation())));

    if (nearby.isEmpty()) {
      player.sendMessage(Component.text("No entity found within 20 blocks.", NamedTextColor.RED));
      return;
    }

    final var entity = nearby.get();
    final var service = DreamAPI.getAPI().getService(NavigateService.class);

    if (!service.isEntityMoving(entity)) {
      player.sendMessage(Component.text("This entity has no active movement task.", NamedTextColor.RED));
      return;
    }

    service.stopEntityMovement(entity);
    player.sendMessage(Component.text("■ Movement stopped for %s [%s]."
      .formatted(entity.getType().name(), entity.getUniqueId().toString().substring(0, 8)), NamedTextColor.YELLOW));
  }

  @CommandDescription("Display movement info for the nearest entity")
  @CommandMethod("navigation entity info")
  @CommandPermission("dreamin.cmd.navigation.start")
  private void navigationEntityInfo(
    final @NotNull CommandSender sender
  ) {
    if (!(sender instanceof Player player)) return;

    final var nearby = player.getNearbyEntities(20, 20, 20).stream()
      .filter(e -> !(e instanceof Player))
      .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(player.getLocation())));

    if (nearby.isEmpty()) {
      player.sendMessage(Component.text("No entity found within 20 blocks.", NamedTextColor.RED));
      return;
    }

    final var entity = nearby.get();
    final var service = DreamAPI.getAPI().getService(NavigateService.class);
    final var taskOpt = service.getEntityMovementTask(entity);

    player.sendMessage(Component.text("─── Entity Movement Info ───", NamedTextColor.GOLD));
    player.sendMessage(Component.text("Entity  : %s [%s]".formatted(
      entity.getType().name(), entity.getUniqueId().toString().substring(0, 8)), NamedTextColor.AQUA));

    if (taskOpt.isEmpty()) {
      player.sendMessage(Component.text("Status  : No active movement task", NamedTextColor.RED));
      return;
    }

    final var task = taskOpt.get();
    final var path = task.getCurrentPath();
    final var target = task.getTargetLocation();
    final var distToTarget = (int) entity.getLocation().distance(target);
    final var pathSize = path != null ? path.size() : 0;
    final var pathIndex = task.getCurrentPathIndex();

    player.sendMessage(Component.text("Target  : X:%s Y:%s Z:%s".formatted(
      target.getBlockX(), target.getBlockY(), target.getBlockZ()), NamedTextColor.AQUA));
    player.sendMessage(Component.text("Distance: %s blocks".formatted(distToTarget), NamedTextColor.AQUA));
    player.sendMessage(Component.text("Path    : %s / %s waypoints".formatted(pathIndex, pathSize), NamedTextColor.AQUA));
    player.sendMessage(Component.text("Speed   : %s b/t".formatted(task.getSpeed()), NamedTextColor.AQUA));
    player.sendMessage(Component.text("Recalc  : %s".formatted(task.isRecalculating()),
      task.isRecalculating() ? NamedTextColor.YELLOW : NamedTextColor.GREEN));

    // Highlight the current waypoint with a gold particle
    if (path != null && pathIndex < pathSize) {
      final var waypoint = path.get(pathIndex);
      player.spawnParticle(Particle.DUST, waypoint.clone().add(0.5, 0.5, 0.5),
        10, 0.1, 0.1, 0.1, 0, new Particle.DustOptions(Color.fromRGB(255, 165, 0), 1.5f));
    }
  }

  // ###############################################################
  // -------------------- DIRECTION UTILITIES ----------------------
  // ###############################################################

  @CommandDescription("Display the compass direction from you to a location")
  @CommandMethod("navigation direction <x> <y> <z>")
  @CommandPermission("dreamin.cmd.navigation.start")
  private void navigationDirection(
    final @NotNull CommandSender sender,
    @Argument(value = "x") int x,
    @Argument(value = "y") int y,
    @Argument(value = "z") int z
  ) {
    if (!(sender instanceof Player player)) return;

    final var target = new Location(player.getWorld(), x, y, z);
    final var from = player.getLocation();
    final var direction = AStartPathFinder.getDirection(from, target);
    final var yaw = AStartPathFinder.getYaw(from, target);
    final var distXZ = AStartPathFinder.getDistance2D(from, target);
    final var dist3D = (int) from.distance(target);

    player.sendMessage(Component.text("─── Direction Info ───", NamedTextColor.GOLD));
    player.sendMessage(Component.text("Target    : X:%s Y:%s Z:%s".formatted(x, y, z), NamedTextColor.AQUA));
    player.sendMessage(Component.text("Direction : %s".formatted(direction), NamedTextColor.GREEN));
    player.sendMessage(Component.text("Yaw       : %.1f°".formatted(yaw), NamedTextColor.AQUA));
    player.sendMessage(Component.text("Distance  : %s blocks (2D: %s blocks)".formatted(dist3D, distXZ), NamedTextColor.AQUA));
  }

}
