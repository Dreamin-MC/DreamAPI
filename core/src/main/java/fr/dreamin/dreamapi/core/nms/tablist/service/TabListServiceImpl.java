package fr.dreamin.dreamapi.core.nms.tablist.service;

import fr.dreamin.dreamapi.api.annotations.Inject;
import fr.dreamin.dreamapi.api.nms.packet.PacketReflection;
import fr.dreamin.dreamapi.api.nms.packet.PacketSender;
import fr.dreamin.dreamapi.api.nms.tablist.model.TabListMode;
import fr.dreamin.dreamapi.api.nms.tablist.service.TabListService;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Inject
@DreamAutoService(TabListService.class)
public final class TabListServiceImpl implements TabListService, DreamService, Listener {

  private static final long DEFAULT_TAB_CLEANUP_DELAY_TICKS = 10L;

  private final @NotNull Plugin plugin;
  private final @NotNull Map<UUID, ViewerState> viewers = new ConcurrentHashMap<>();

  private volatile @NotNull TabListMode defaultMode = TabListMode.VISIBLE;
  private volatile boolean autoEnabled = false;
  private volatile long tabCleanupDelayTicks = DEFAULT_TAB_CLEANUP_DELAY_TICKS;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  public TabListServiceImpl(final @NotNull Plugin plugin) {
    PacketReflection.initialize();
    this.plugin = plugin;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void onClose() {
    for (final var onlinePlayer : Bukkit.getOnlinePlayers()) {
      removeInterceptor(onlinePlayer.getUniqueId());
      cancelPendingCleanup(onlinePlayer.getUniqueId());
    }
    this.viewers.clear();
  }

  @Override
  public void setMode(final @NotNull Player player, final @NotNull TabListMode mode) {
    final var viewerState = this.viewers.computeIfAbsent(player.getUniqueId(), ignored -> new ViewerState());
    viewerState.customMode = mode;
    applyEffectiveMode(player, viewerState);
  }

  @Override
  public @NotNull TabListMode getMode(final @NotNull Player player) {
    final var viewerState = this.viewers.get(player.getUniqueId());
    if (viewerState != null) {
      final var customMode = viewerState.customMode;
      if (customMode != null) {
        return customMode;
      }
    }
    return this.defaultMode;
  }

  @Override
  public void clearMode(final @NotNull Player player) {
    final var viewerState = this.viewers.computeIfAbsent(player.getUniqueId(), ignored -> new ViewerState());
    viewerState.customMode = null;
    applyEffectiveMode(player, viewerState);
    cleanupStateIfUnused(player.getUniqueId(), viewerState);
  }

  @Override
  public boolean hasCustomMode(final @NotNull Player player) {
    final var viewerState = this.viewers.get(player.getUniqueId());
    return viewerState != null && viewerState.customMode != null;
  }

  @Override
  public void refresh(final @NotNull Player player) {
    final var viewerState = this.viewers.computeIfAbsent(player.getUniqueId(), ignored -> new ViewerState());

    if (viewerState.customMode != null || this.autoEnabled) {
      applyEffectiveMode(player, viewerState);
      return;
    }

    removeInterceptor(player.getUniqueId());
    resyncTabEntriesForViewer(player);
    cleanupStateIfUnused(player.getUniqueId(), viewerState);
  }

  @Override
  public @NotNull TabListMode getDefaultMode() {
    return this.defaultMode;
  }

  @Override
  public void setDefaultMode(final @NotNull TabListMode mode) {
    this.defaultMode = mode;
    reapplyAutoManagedPlayers();
  }

  @Override
  public boolean isAutoEnabled() {
    return this.autoEnabled;
  }

  @Override
  public void setAutoEnabled(final boolean enabled) {
    this.autoEnabled = enabled;
    reapplyAutoManagedPlayers();
  }

  @Override
  public long getTabCleanupDelayTicks() {
    return this.tabCleanupDelayTicks;
  }

  @Override
  public void setTabCleanupDelayTicks(final long ticks) {
    if (ticks < 1L) {
      throw new IllegalArgumentException("Tab cleanup delay must be >= 1 tick");
    }
    this.tabCleanupDelayTicks = ticks;
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void applyEffectiveMode(final @NotNull Player player, final @NotNull ViewerState state) {
    final var effectiveMode = resolveEffectiveMode(state);

    try {
      if (effectiveMode == TabListMode.VISIBLE) {
        removeInterceptor(player.getUniqueId());
        resyncTabEntriesForViewer(player);
        return;
      }

      installInterceptor(player, state);

      scheduleTabCleanupForViewer(player.getUniqueId(), this.tabCleanupDelayTicks);
      if (effectiveMode == TabListMode.HIDDEN) {
        player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
      }
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Failed to apply tab-list mode for " + player.getName(), e);
    }
  }

  private void reapplyAutoManagedPlayers() {
    for (final var onlinePlayer : Bukkit.getOnlinePlayers()) {
      final var state = this.viewers.computeIfAbsent(onlinePlayer.getUniqueId(), ignored -> new ViewerState());
      if (state.customMode != null) {
        continue;
      }

      if (!this.autoEnabled) {
        removeInterceptor(onlinePlayer.getUniqueId());
        resyncTabEntriesForViewer(onlinePlayer);
        cleanupStateIfUnused(onlinePlayer.getUniqueId(), state);
        continue;
      }

      applyEffectiveMode(onlinePlayer, state);
      cleanupStateIfUnused(onlinePlayer.getUniqueId(), state);
    }
  }

  private @NotNull TabListMode resolveEffectiveMode(final @NotNull ViewerState state) {
    final var customMode = state.customMode;
    return customMode != null ? customMode : this.defaultMode;
  }

  private void removeTabEntries(final @NotNull Player viewer, final @NotNull List<UUID> profileIds) throws ReflectiveOperationException {
    if (profileIds.isEmpty()) {
      return;
    }

    final var removePacket = PacketReflection.createPlayerInfoRemovePacket(profileIds);
    PacketSender.send(viewer, removePacket);
  }

  private void resyncTabEntriesForViewer(final @NotNull Player viewer) {
    try {
      final var players = Bukkit.getOnlinePlayers();
      if (players.isEmpty()) {
        return;
      }

      final var updatePacket = PacketReflection.createPlayerInfoUpdatePacket(players);
      PacketSender.send(viewer, updatePacket);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Failed to resync tab-list for " + viewer.getName(), e);
    }
  }

  private void installInterceptor(final @NotNull Player player, final @NotNull ViewerState state) throws ReflectiveOperationException {
    if (state.interceptorName != null) {
      return;
    }

    final var channel = PacketReflection.getChannel(player);
    final var interceptorName = "dreamapi-tablist-" + player.getUniqueId();

    if (channel.pipeline().get(interceptorName) == null) {
      channel.pipeline().addBefore("packet_handler", interceptorName,
        new TabListUpdateInterceptor(player.getUniqueId(), () -> resolveEffectiveMode(state) != TabListMode.VISIBLE));
    }

    state.interceptorName = interceptorName;
  }

  private void cleanupStateIfUnused(final @NotNull UUID viewerUuid, final @NotNull ViewerState state) {
    if (state.customMode != null || state.interceptorName != null) {
      return;
    }

    final var cleanupTask = state.cleanupTask;
    if (cleanupTask != null) {
      cleanupTask.cancel();
      state.cleanupTask = null;
    }

    this.viewers.remove(viewerUuid, state);
  }

  private void removeInterceptor(final @NotNull UUID viewerUuid) {
    final var state = this.viewers.get(viewerUuid);
    if (state == null || state.interceptorName == null) {
      return;
    }

    final var viewer = Bukkit.getPlayer(viewerUuid);
    if (viewer == null || !viewer.isOnline()) {
      state.interceptorName = null;
      return;
    }

    try {
      final var channel = PacketReflection.getChannel(viewer);
      if (channel.pipeline().get(state.interceptorName) != null) {
        channel.pipeline().remove(state.interceptorName);
      }
      state.interceptorName = null;
    } catch (ReflectiveOperationException ignored) {
      // Channel may already be closed during quit/shutdown.
      state.interceptorName = null;
    }
  }

  private void scheduleTabCleanupForViewer(final @NotNull UUID viewerUuid, final long delayTicks) {
    final var state = this.viewers.get(viewerUuid);
    if (state == null) {
      return;
    }

    final var previousTask = state.cleanupTask;
    if (previousTask != null) {
      previousTask.cancel();
    }

    state.cleanupTask = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
      final var currentState = this.viewers.get(viewerUuid);
      if (currentState == null || currentState != state) {
        state.cleanupTask = null;
        return;
      }

      final var viewer = Bukkit.getPlayer(viewerUuid);
      if (viewer == null || !viewer.isOnline()) {
        state.cleanupTask = null;
        return;
      }

      if (resolveEffectiveMode(currentState) == TabListMode.VISIBLE) {
        currentState.cleanupTask = null;
        return;
      }

      try {
        removeTabEntries(viewer, Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).toList());
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException("Failed to keep tab-list hidden for " + viewer.getName(), e);
      } finally {
        currentState.cleanupTask = null;
      }
    }, delayTicks);
  }

  private void cancelPendingCleanup(final @NotNull UUID viewerUuid) {
    final var state = this.viewers.get(viewerUuid);
    if (state == null) {
      return;
    }

    final var cleanupTask = state.cleanupTask;
    if (cleanupTask != null) {
      cleanupTask.cancel();
      state.cleanupTask = null;
    }
  }

  @FunctionalInterface
  private interface VisibilitySupplier {
    boolean isHiddenModeEnabled();
  }

  // ###############################################################
  // ----------------------- STATIC METHODS ------------------------
  // ###############################################################

  private final class TabListUpdateInterceptor extends ChannelDuplexHandler {

    private final @NotNull UUID viewerUuid;
    private final @NotNull VisibilitySupplier visibilitySupplier;

    private TabListUpdateInterceptor(
      final @NotNull UUID viewerUuid,
      final @NotNull VisibilitySupplier visibilitySupplier
    ) {
      this.viewerUuid = viewerUuid;
      this.visibilitySupplier = visibilitySupplier;
    }

    @Override
    public void write(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg, final @NotNull ChannelPromise promise) throws Exception {
      if (this.visibilitySupplier.isHiddenModeEnabled()
        && PacketReflection.getPacketPlayerInfoUpdateClass().isInstance(msg)) {
        // Keep packet flow for skin/game profile consistency, then re-hide tab entries.
        super.write(ctx, msg, promise);
        scheduleTabCleanupForViewer(this.viewerUuid, tabCleanupDelayTicks);
        return;
      }

      super.write(ctx, msg, promise);
    }
  }

  private static final class ViewerState {
    private volatile @Nullable TabListMode customMode;
    private volatile @Nullable String interceptorName;
    private volatile @Nullable BukkitTask cleanupTask;
  }

  // ###############################################################
  // ---------------------- LISTENER METHODS -----------------------
  // ###############################################################

  @EventHandler(priority = EventPriority.MONITOR)
  private void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
    final var joinedPlayer = event.getPlayer();

    // Delay by one tick to run after vanilla join synchronization packets are queued.
    Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
      final var joinedState = this.viewers.computeIfAbsent(joinedPlayer.getUniqueId(), ignored -> new ViewerState());
      if (joinedState.customMode != null || this.autoEnabled) {
        applyEffectiveMode(joinedPlayer, joinedState);
        if (resolveEffectiveMode(joinedState) != TabListMode.VISIBLE) {
          scheduleTabCleanupForViewer(joinedPlayer.getUniqueId(), this.tabCleanupDelayTicks);
        }
      }
      cleanupStateIfUnused(joinedPlayer.getUniqueId(), joinedState);

      for (final var entry : this.viewers.entrySet()) {
        final var viewerState = entry.getValue();
        if (resolveEffectiveMode(viewerState) == TabListMode.VISIBLE) {
          continue;
        }

        final var viewer = Bukkit.getPlayer(entry.getKey());
        if (viewer == null || !viewer.isOnline()) {
          continue;
        }

        scheduleTabCleanupForViewer(viewer.getUniqueId(), this.tabCleanupDelayTicks);
      }
    }, 1L);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  private void onPlayerQuit(final @NotNull PlayerQuitEvent event) {
    final var uuid = event.getPlayer().getUniqueId();
    cancelPendingCleanup(uuid);
    removeInterceptor(uuid);
    final var state = this.viewers.get(uuid);
    if (state != null) {
      cleanupStateIfUnused(uuid, state);
    }
  }

}


