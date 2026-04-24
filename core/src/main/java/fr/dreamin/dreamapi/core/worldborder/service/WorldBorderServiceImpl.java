package fr.dreamin.dreamapi.core.worldborder.service;

import fr.dreamin.dreamapi.api.annotations.Inject;
import fr.dreamin.dreamapi.api.nms.packet.PacketReflection;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.api.worldborder.model.*;
import fr.dreamin.dreamapi.api.worldborder.model.WorldBorderDataTagType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

@Inject
@DreamAutoService(WorldBorderService.class)
public final class WorldBorderServiceImpl implements WorldBorderService, DreamService, Listener {

  private final @NotNull Plugin plugin;

  private final NamespacedKey worldBorderDataKey;
  private final WorldBorderDataTagType worldBorderDataTagType;

  private final @NotNull Function<Player, IWorldBorder> getWorldBorderPlayer;
  private final @NotNull Function<World, IWorldBorder> getWorldBorder;

  private boolean healthOverlayEnabled = false;
  private final @NotNull Map<UUID, OverlayState> healthOverlayWarningCache = new HashMap<>();
  private final @NotNull Map<UUID, BukkitTask> borderPulseTasks = new HashMap<>();
  private final @NotNull Map<UUID, Integer> borderPulseBaseWarningDistance = new HashMap<>();
  private BukkitTask healthOverlayTask;


  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public WorldBorderServiceImpl(final @NotNull Plugin plugin) {
    PacketReflection.initialize();

    this.plugin = plugin;
    this.worldBorderDataKey = new NamespacedKey(plugin, "world_border_data");
    this.worldBorderDataTagType = new WorldBorderDataTagType(plugin);
    this.getWorldBorderPlayer = WorldBorder::new;
    this.getWorldBorder = WorldBorder::new;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void onClose() {
    stopHealthOverlayTask();
    for (final var player : Bukkit.getOnlinePlayers()) {
      stopPulseForPlayer(player, true);
    }
    resetAllHealthOverlays();
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
      () -> resetWorldBorderToGlobal(player),
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

  @Override
  public void pulseBorder(
    final @NotNull Player player,
    final double minSize,
    final double maxSize,
    final int pulses,
    final @NotNull Duration duration
  ) {
    if (pulses <= 0 || minSize <= 0 || maxSize <= 0 || duration.isZero() || duration.isNegative()) {
      throw new IllegalArgumentException("Invalid pulse parameters: pulses/minSize/maxSize/duration must be > 0");
    }

    final long stepDurationMs = duration.toMillis();

    final int low = (int) Math.round(Math.min(minSize, maxSize));
    final int high = (int) Math.round(Math.max(minSize, maxSize));

    final var border = getWorldBorder(player);
    stopPulseForPlayer(player, true);
    this.borderPulseBaseWarningDistance.put(player.getUniqueId(), border.getWarningDistanceInBlocks());

    final long stepTicks = Math.max(1L, stepDurationMs / 50L);
    final int halfPulses = pulses * 2;
    final int[] cursor = {0};

    final var task = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
      if (!player.isOnline()) {
        stopPulseForPlayer(player, false);
        return;
      }

      if (cursor[0] >= halfPulses) {
        stopPulseForPlayer(player, true);
        return;
      }

      final int targetWarningDistance = cursor[0] % 2 == 0 ? low : high;
      border.setWarningDistanceInBlocks(targetWarningDistance);
      border.send(player, WorldBorderAction.SET_WARNING_BLOCKS);
      cursor[0]++;
    }, 0L, stepTicks);

    this.borderPulseTasks.put(player.getUniqueId(), task);
  }

  @Override
  public void setHealthOverlayEnabled(final boolean enabled) {
    this.healthOverlayEnabled = enabled;

    if (enabled) {
      startHealthOverlayTask();
      for (final var player : Bukkit.getOnlinePlayers()) {
        applyHealthOverlay(player, player.getHealth());
      }
      return;
    }

    stopHealthOverlayTask();
    resetAllHealthOverlays();
  }

  @Override
  public boolean isHealthOverlayEnabled() {
    return this.healthOverlayEnabled;
  }

  // ###############################################################
  // ---------------------- LISTENER METHODS -----------------------
  // ###############################################################

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  private void onPlayerDamage(final @NotNull EntityDamageEvent event) {
    if (!this.healthOverlayEnabled) return;
    if (!(event.getEntity() instanceof Player player)) return;

    final double projectedHealth = Math.max(0d, player.getHealth() - event.getFinalDamage());
    applyHealthOverlay(player, projectedHealth);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  private void onPlayerRegainHealth(final @NotNull EntityRegainHealthEvent event) {
    if (!this.healthOverlayEnabled) return;
    if (!(event.getEntity() instanceof Player player)) return;

    final double projectedHealth = Math.min(getMaxHealth(player), player.getHealth() + event.getAmount());
    applyHealthOverlay(player, projectedHealth);
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void applyHealthOverlay(final @NotNull Player player, final double healthValue) {
    if (this.borderPulseTasks.containsKey(player.getUniqueId())) return;

    final double maxHealth = getMaxHealth(player);
    if (maxHealth <= 0d) return;

    final double remainingHealth = Math.max(0d, Math.min(healthValue, maxHealth));
    final double ratio = 1d - (remainingHealth / maxHealth);

    final var border = getWorldBorder(player);
    final var state = healthOverlayWarningCache.computeIfAbsent(
      player.getUniqueId(),
      ignored -> new OverlayState(border.getWarningDistanceInBlocks(), Integer.MIN_VALUE)
    );

    final int targetWarningDistance = Math.max(
      state.baseWarningDistance(),
      (int) Math.round(Math.max(0d, border.getSize()) * ratio)
    );

    if (targetWarningDistance == state.lastAppliedWarningDistance()) {
      return;
    }

    border.setWarningDistanceInBlocks(targetWarningDistance);
    border.send(player, WorldBorderAction.SET_WARNING_BLOCKS);
    healthOverlayWarningCache.put(player.getUniqueId(), state.withLastAppliedWarningDistance(targetWarningDistance));
  }

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
      worldBorderData = Objects.requireNonNull(persistentDataContainer.get(worldBorderDataKey, worldBorderDataTagType));

    worldBorderDataConsumer.accept(worldBorderData);
    persistentDataContainer.set(worldBorderDataKey, worldBorderDataTagType, worldBorderData);
  }

  private void resetAllHealthOverlays() {
    for (final var onlinePlayer : Bukkit.getOnlinePlayers()) {
      final var previousState = this.healthOverlayWarningCache.remove(onlinePlayer.getUniqueId());
      if (previousState == null) continue;

      final var border = getWorldBorder(onlinePlayer);
      border.setWarningDistanceInBlocks(previousState.baseWarningDistance());
      border.send(onlinePlayer, WorldBorderAction.SET_WARNING_BLOCKS);
    }

    this.healthOverlayWarningCache.clear();
  }

  private void startHealthOverlayTask() {
    if (this.healthOverlayTask != null) return;

    // Poll health changes to catch external setHealth calls that bypass damage/regain events.
    this.healthOverlayTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
      if (!this.healthOverlayEnabled) return;
      for (final var player : Bukkit.getOnlinePlayers()) {
        applyHealthOverlay(player, player.getHealth());
      }
    }, 1L, 5L);
  }

  private void stopHealthOverlayTask() {
    if (this.healthOverlayTask == null) return;
    this.healthOverlayTask.cancel();
    this.healthOverlayTask = null;
  }

  private void stopPulseForPlayer(final @NotNull Player player, final boolean restoreWarningDistance) {
    final var task = this.borderPulseTasks.remove(player.getUniqueId());
    if (task != null) {
      task.cancel();
    }

    final var baseWarningDistance = this.borderPulseBaseWarningDistance.remove(player.getUniqueId());
    if (!restoreWarningDistance || baseWarningDistance == null) {
      return;
    }

    final var border = getWorldBorder(player);
    border.setWarningDistanceInBlocks(baseWarningDistance);
    border.send(player, WorldBorderAction.SET_WARNING_BLOCKS);

    if (this.healthOverlayEnabled) {
      applyHealthOverlay(player, player.getHealth());
    }
  }

  private double getMaxHealth(final @NotNull Player player) {
    final var attribute = player.getAttribute(Attribute.MAX_HEALTH);
    return attribute == null ? 20d : attribute.getValue();
  }

  private record OverlayState(int baseWarningDistance, int lastAppliedWarningDistance) {
    private OverlayState withLastAppliedWarningDistance(final int value) {
      return new OverlayState(this.baseWarningDistance, value);
    }
  }

}
