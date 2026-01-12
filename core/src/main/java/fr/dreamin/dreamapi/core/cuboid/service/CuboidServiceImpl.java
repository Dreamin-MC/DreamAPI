package fr.dreamin.dreamapi.core.cuboid.service;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.cuboid.service.CuboidService;
import fr.dreamin.dreamapi.api.logger.DebugMeta;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.api.annotations.Inject;
import fr.dreamin.dreamapi.api.cuboid.Cuboid;
import fr.dreamin.dreamapi.core.cuboid.event.CuboidEnterEvent;
import fr.dreamin.dreamapi.core.cuboid.event.CuboidLeaveEvent;
import fr.dreamin.dreamapi.api.logger.DreamLogger;
import fr.dreamin.dreamapi.core.glowing.GlowingServiceImpl;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
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
@DreamAutoService(value = CuboidService.class, dependencies = {GlowingServiceImpl.class})
public final class CuboidServiceImpl implements CuboidService, DreamService, Listener {

  private final @NotNull Set<Cuboid> cuboids = new HashSet<>();
  private final @NotNull Map<UUID, Set<Cuboid>> playerCuboids = new HashMap<>();

  private boolean autoRegister = true;

  @SuppressWarnings("NullableProblems")
  private DreamLogger dreamLogger;

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

    this.dreamLogger.info("New cuboid registered at locA %s and locB %s",
      cuboid.getLocA().toString(),
      cuboid.getLocB().toString()
    );
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

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  /**
   * Handles the player entering a cuboid.
   *
   * @param player
   * @param cuboid
   * @param event
   * @param currentCuboids
   *
   * @author Dreamin
   * @since 1.0.0
   */
  private void enter(
    final @NotNull Player player,
    final @NotNull Cuboid cuboid,
    final @NotNull PlayerMoveEvent event,
    final @NotNull Set<Cuboid> currentCuboids
  ) {
    var enterEvent = new CuboidEnterEvent(player, cuboid);
    if (DreamAPI.getAPI().callEvent(enterEvent).isCancelled())
      event.setCancelled(true);
    else {
      this.dreamLogger.info(DebugMeta.of(player), String.format("%s enter at cuboid", player.getName()));
      currentCuboids.add(cuboid);
    }
  }

  /**
   * Handles the player leaving a cuboid.
   *
   * @param player
   * @param cuboid
   * @param event
   * @param currentCuboids
   *
   * @author Dreamin
   * @since 1.0.0
   */
  private void leave(
    final @NotNull Player player,
    final @NotNull Cuboid cuboid,
    final @NotNull PlayerMoveEvent event,
    final @NotNull Set<Cuboid> currentCuboids
  ) {
    var leaveEvent = new CuboidLeaveEvent(player, cuboid);
    if (DreamAPI.getAPI().callEvent(leaveEvent).isCancelled())
      event.setCancelled(true);
    else {
      this.dreamLogger.info(DebugMeta.of(player), String.format("%s leave a cuboid", player.getName()));
      currentCuboids.remove(cuboid);
    }
  }

}
