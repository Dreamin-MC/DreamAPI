package fr.dreamin.dreamapi.core.cuboid.service;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.cuboid.service.CuboidService;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.api.annotations.Inject;
import fr.dreamin.dreamapi.api.cuboid.Cuboid;
import fr.dreamin.dreamapi.core.cuboid.event.*;
import io.papermc.paper.event.entity.EntityMoveEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Implementation of the CuboidService interface for managing Cuboid objects.
 *
 * @author Dreamin
 * @since 1.0.0
 */
@Inject
@RequiredArgsConstructor
@DreamAutoService(value = CuboidService.class)
public final class CuboidServiceImpl implements CuboidService, DreamService, Listener {

  private final @NotNull Plugin plugin;

  private final @NotNull Set<Cuboid> cuboids = new HashSet<>();
  private final @NotNull Map<UUID, Set<Cuboid>> playerCuboids = new HashMap<>();

  private boolean autoRegister = true;

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void onClose() {
    clear();
  }

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public void register(@NotNull Cuboid cuboid) {
    this.cuboids.add(cuboid);
  }

  @Override
  public void unregister(@NotNull Cuboid cuboid) {
    this.cuboids.remove(cuboid);
  }

  @Override
  public boolean isAutoRegister() {
    return this.autoRegister;
  }

  @Override
  public void autoRegister(boolean value) {
    this.autoRegister = value;
  }

  @Override
  public void clear() {
    this.cuboids.clear();
    this.playerCuboids.clear();
  }

  @Override
  public @NotNull Set<Cuboid> getCuboids() {
    return Collections.unmodifiableSet(this.cuboids);
  }

  @Override
  public @NotNull Set<Cuboid> getCuboidsOf(@NotNull UUID uuid) {
    return playerCuboids.getOrDefault(uuid, Set.of());
  }

  // ###############################################################
  // ---------------------- LISTENER METHODS -----------------------
  // ###############################################################

  @EventHandler
  private void onPlayerMove(final @NotNull PlayerMoveEvent event) {
    var player = event.getPlayer();
    var from = event.getFrom();
    var to = event.getTo();
    if ((from.getBlockX() == to.getBlockX() &&
        from.getBlockY() == to.getBlockY() &&
        from.getBlockZ() == to.getBlockZ()))
      return;

    var currentCuboids = playerCuboids.computeIfAbsent(player.getUniqueId(), id -> new HashSet<>());

    for (Cuboid cuboid : cuboids) {
      boolean wasIn = currentCuboids.contains(cuboid);
      boolean isIn = cuboid.isLocationIn(to);

      if (!wasIn && isIn)
        enter(player, cuboid, event, currentCuboids);
      else if (wasIn && !isIn)
        leave(player, cuboid, event, currentCuboids);
    }
  }

  @EventHandler
  private void onPlayerTeleport(final @NotNull PlayerTeleportEvent event) {
    var player = event.getPlayer();
    var from = event.getFrom();
    var to = event.getTo();
    if ((from.getBlockX() == to.getBlockX() &&
      from.getBlockY() == to.getBlockY() &&
      from.getBlockZ() == to.getBlockZ()))
      return;

    var currentCuboids = playerCuboids.computeIfAbsent(player.getUniqueId(), id -> new HashSet<>());

    for (Cuboid cuboid : cuboids) {
      boolean wasIn = currentCuboids.contains(cuboid);
      boolean isIn = cuboid.isLocationIn(to);

      if (!wasIn && isIn)
        enter(player, cuboid, event, currentCuboids);
      else if (wasIn && !isIn)
        leave(player, cuboid, event, currentCuboids);
    }
  }

  @EventHandler
  private void onEntityMove(final @NotNull EntityMoveEvent event) {
    var entity = event.getEntity();
    var from = event.getFrom();
    var to = event.getTo();
    if ((from.getBlockX() == to.getBlockX() &&
      from.getBlockY() == to.getBlockY() &&
      from.getBlockZ() == to.getBlockZ()))
      return;

    var currentCuboids = playerCuboids.computeIfAbsent(entity.getUniqueId(), id -> new HashSet<>());

    for (Cuboid cuboid : cuboids) {
      boolean wasIn = currentCuboids.contains(cuboid);
      boolean isIn = cuboid.isLocationIn(to);

      if (!wasIn && isIn)
        enter(entity, cuboid, event, currentCuboids);
      else if (wasIn && !isIn)
        leave(entity, cuboid, event, currentCuboids);
    }
  }

  @EventHandler
  private void onEntityTeleport(final @NotNull EntityTeleportEvent event) {
    var entity = event.getEntity();
    var from = event.getFrom();
    var to = event.getTo();
    if ((from.getBlockX() == to.getBlockX() &&
      from.getBlockY() == to.getBlockY() &&
      from.getBlockZ() == to.getBlockZ()))
      return;

    var currentCuboids = playerCuboids.computeIfAbsent(entity.getUniqueId(), id -> new HashSet<>());

    for (Cuboid cuboid : cuboids) {
      boolean wasIn = currentCuboids.contains(cuboid);
      boolean isIn = cuboid.isLocationIn(to);

      if (!wasIn && isIn)
        enter(entity, cuboid, event, currentCuboids);
      else if (wasIn && !isIn)
        leave(entity, cuboid, event, currentCuboids);
    }
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  /**
   * Handles the entity entering a cuboid.
   *
   * @param entity
   * @param cuboid
   * @param event
   * @param currentCuboids
   *
   * @author Dreamin
   * @since 1.0.0
   */
  private void enter(
    final @NotNull Entity entity,
    final @NotNull Cuboid cuboid,
    final @NotNull Cancellable event,
    final @NotNull Set<Cuboid> currentCuboids
  ) {
    var enterEvent = getCuboidEnterEvent(entity, cuboid);
    if (DreamAPI.getAPI().callEvent(enterEvent).isCancelled())
      event.setCancelled(true);
    else
      currentCuboids.add(cuboid);
  }

  /**
   * Handles the entity leaving a cuboid.
   *
   * @param entity
   * @param cuboid
   * @param event
   * @param currentCuboids
   *
   * @author Dreamin
   * @since 1.0.0
   */
  private void leave(
    final @NotNull Entity entity,
    final @NotNull Cuboid cuboid,
    final @NotNull Cancellable event,
    final @NotNull Set<Cuboid> currentCuboids
  ) {
    var leaveEvent = getCuboidLeaveEvent(entity, cuboid);
    if (DreamAPI.getAPI().callEvent(leaveEvent).isCancelled())
      event.setCancelled(true);
    else
      currentCuboids.remove(cuboid);
  }

  private CuboidEntityEnterEvent getCuboidEnterEvent(final @NotNull Entity entity, final @NotNull Cuboid cuboid) {
    if (entity instanceof Player player)
      return new CuboidPlayerEnterEvent(player, cuboid);
    else if (entity instanceof Mannequin mannequin)
      return new CuboidMannequinEnterEvent(mannequin, cuboid);
    else
      return new CuboidEntityEnterEvent(entity, cuboid);
  }

  private CuboidEntityLeaveEvent getCuboidLeaveEvent(final @NotNull Entity entity, final @NotNull Cuboid cuboid) {
    if (entity instanceof Player player)
      return new CuboidPlayerLeaveEvent(player, cuboid);
    else if (entity instanceof Mannequin mannequin)
      return new CuboidMannequinLeaveEvent(mannequin, cuboid);
    else
      return new CuboidEntityLeaveEvent(entity, cuboid);
  }

}
