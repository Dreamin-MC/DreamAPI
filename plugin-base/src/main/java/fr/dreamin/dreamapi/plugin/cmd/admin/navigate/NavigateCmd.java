package fr.dreamin.dreamapi.plugin.cmd.admin.navigate;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import fr.dreamin.dreamapi.api.navigate.model.PathFindingTask;
import fr.dreamin.dreamapi.plugin.DreamPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class NavigateCmd {

  @CommandDescription("navigation start")
  @CommandMethod("navigation start <x> <y> <z> <safeMode> [recalcDistance]")
  @CommandPermission("dreamin.cmd.navigation.start")
  private void navigationStart(
    final @NotNull CommandSender sender,
    @Argument(value = "x") int x,
    @Argument(value = "y") int y,
    @Argument(value = "z") int z,
    @Argument(value = "safeMode") boolean safeMode,
    @Argument(value = "recalcDistance") Double recalcDistance
  ) {
    if (!(sender instanceof Player player)) return;

    final double distance = (recalcDistance != null && recalcDistance > 0) ? recalcDistance : 3.0;

    if (PathFindingTask.ACTIVATE_NAVIGATION.containsKey(player.getUniqueId())) {
      PathFindingTask.ACTIVATE_NAVIGATION.get(player.getUniqueId()).cancel();
      PathFindingTask.ACTIVATE_NAVIGATION.remove(player.getUniqueId());
      player.sendMessage(Component.text("Your previous navigation has been stopped."));
    }

    final var targetLocation = new Location(player.getWorld(), x, y, z);
    final var newTask = new PathFindingTask(player, targetLocation, safeMode, Set.of(), distance);
    newTask.runTaskTimer(DreamPlugin.getInstance(), 0, 10);

    PathFindingTask.ACTIVATE_NAVIGATION.put(player.getUniqueId(), newTask);
    player.sendMessage(Component.text("Navigation started to X: %s Y: %s Z: %s | safeMode: %s | recalcDistance: %s blocks".formatted(x, y, z, safeMode, distance)));
  }

  @CommandDescription("navigation stop")
  @CommandMethod("navigation stop")
  @CommandPermission("dreamin.cmd.navigation.start")
  private void navigationStop(
    final @NotNull CommandSender sender
  ) {
    if (!(sender instanceof Player player)) return;

    if (PathFindingTask.ACTIVATE_NAVIGATION.containsKey(player.getUniqueId())) {
      PathFindingTask.ACTIVATE_NAVIGATION.get(player.getUniqueId()).cancel();
      PathFindingTask.ACTIVATE_NAVIGATION.remove(player.getUniqueId());
      player.sendMessage(Component.text("Your previous navigation has been stopped."));
    }
    else {
      player.sendMessage(Component.text("You are not currently navigation."));
    }

  }

}
