package fr.dreamin.dreamapi.core.worldborder.service;

import fr.dreamin.dreamapi.api.annotations.Inject;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.worldborder.model.*;
import fr.dreamin.dreamapi.api.worldborder.model.WorldBorderDataTagType;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

@Inject
@RequiredArgsConstructor
@DreamAutoService(WorldBorderService.class)
public final class WorldBorderServiceImpl implements WorldBorderService {

  private final @NotNull Plugin plugin;

  private final NamespacedKey worldBorderDataKey;
  private final WorldBorderDataTagType worldBorderDataTagType;

  private final @NotNull Function<Player, IWorldBorder> getWorldBorderPlayer;
  private final @NotNull Function<World, IWorldBorder> getWorldBorder;


  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public WorldBorderServiceImpl(final @NotNull Plugin plugin) {
    this.plugin = plugin;
    this.worldBorderDataKey = new NamespacedKey(plugin, "world_border_data");
    this.worldBorderDataTagType = new WorldBorderDataTagType(plugin);
  }

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public IWorldBorder getWorldBorder(@NotNull Player p) {
    final var worldBorder = getWorldBorderPlayer.apply(p);

    final var persistentDataContainer = p.getPersistentDataContainer();
    if (persistentDataContainer.has(this.worldBorderDataKey, this.worldBorderDataTagType))
      applyWorldDataToWorldBorder(
        worldBorder,
        Objects.requireNonNull(persistentDataContainer.get(
          this.worldBorderDataKey,
          this.worldBorderDataTagType
        ))
      );

    return worldBorder;
  }

  @Override
  public IWorldBorder getWorldBorder(@NotNull World world) {
    return getWorldBorder.apply(world);
  }

  @Override
  public void resetWorldBorderToGlobal(@NotNull Player player) {
    getWorldBorder(player.getWorld()).send(player, WorldBorderAction.INITIALIZE);

    final var persistentDataContainer = player.getPersistentDataContainer();
    if (persistentDataContainer.has(this.worldBorderDataKey, this.worldBorderDataTagType))
      persistentDataContainer.remove(this.worldBorderDataKey);
  }

  @Override
  public void setBorder(@NotNull Player player, double size) {
    setBorder(player, size, player.getWorld().getSpawnLocation());

    modifyAndUpdateWorldData(
      player,
      worldBorderData -> worldBorderData.setSize(size)
    );
  }

  @Override
  public void setBorder(@NotNull Player player, double size, @NotNull Vector vector) {
    setBorder(player, size, Position.of(vector));
  }

  @Override
  public void setBorder(@NotNull Player player, double size, @NotNull Location location) {
    setBorder(player, size, Position.of(location));
  }

  @Override
  public void setBorder(@NotNull Player player, double size, @NotNull Position position) {
    final var border = getWorldBorder(player);
    border.setSize(size);
    border.setCenter(position);
    border.send(player, WorldBorderAction.SET_SIZE);
    border.send(player, WorldBorderAction.SET_CENTER);

    modifyAndUpdateWorldData(
      player,
      worldBorderData -> {
        worldBorderData.setSize(size);
        worldBorderData.setCenter(position.x(), position.z());
      }
    );

  }

  @Override
  public void sendRedScreenForSeconds(@NotNull Player player, @NotNull Duration time) {
    final var border = getWorldBorder(player);
    border.setWarningDistanceInBlocks((int) border.getSize());

    border.send(player, WorldBorderAction.SET_WARNING_BLOCKS);

    Bukkit.getScheduler().runTaskLater(
      this.plugin,
      () -> {
        border.setWarningTimeInSeconds(0);
        border.send(player, WorldBorderAction.SET_WARNING_BLOCKS);
      },
      time.toMillis() / 50
    );

  }

  @Override
  public WorldBorderData getWorldBorderData(@NotNull Player p) {
    final var persistentDataContainer = p.getPersistentDataContainer();
    if (persistentDataContainer.has(this.worldBorderDataKey, this.worldBorderDataTagType))
      return persistentDataContainer.get(this.worldBorderDataKey, this.worldBorderDataTagType);

    return null;
  }

  @Override
  public void setBorder(@NotNull Player player, double size, long milliSeconds) {
    final var worldBorder = getWorldBorder(player);
    worldBorder.lerp(worldBorder.getSize(), size, milliSeconds);
    worldBorder.send(player, WorldBorderAction.LERP_SIZE);

    modifyAndUpdateWorldData(
      player,
      worldBorderData -> worldBorderData.setSize(size)
    );

  }

  @Override
  public void setBorder(@NotNull Player player, double size, long time, @NotNull TimeUnit timeUnit) {
    setBorder(player, size, timeUnit.toMillis(time));

    modifyAndUpdateWorldData(
      player,
      worldBorderData -> worldBorderData.setSize(size)
    );

  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void applyWorldDataToWorldBorder(IWorldBorder iWorldBorder, WorldBorderData worldBorderData) {
    worldBorderData.applyCenter((x, z) -> iWorldBorder.setCenter(new Position(x, z)));
    iWorldBorder.setSize(worldBorderData.getSize());
    iWorldBorder.setDamageBufferInBlocks(worldBorderData.getDamageBuffer());
    iWorldBorder.setWarningDistanceInBlocks(worldBorderData.getWarningDistance());
    iWorldBorder.setWarningTimeInSeconds(worldBorderData.getWarningTimeSeconds());
  }

  private void modifyAndUpdateWorldData(Player player, Consumer<WorldBorderData> worldBorderDataConsumer) {
    PersistentDataContainer persistentDataContainer = player.getPersistentDataContainer();
    var worldBorderData = new WorldBorderData();
    if (persistentDataContainer.has(worldBorderDataKey, worldBorderDataTagType))
      worldBorderData = persistentDataContainer.get(worldBorderDataKey, worldBorderDataTagType);

    worldBorderDataConsumer.accept(worldBorderData);
    persistentDataContainer.set(worldBorderDataKey, worldBorderDataTagType, worldBorderData);
  }

}
