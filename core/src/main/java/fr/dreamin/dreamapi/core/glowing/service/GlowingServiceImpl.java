package fr.dreamin.dreamapi.core.glowing.service;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.glowing.GlowingStats;
import fr.dreamin.dreamapi.api.glowing.animation.GlowAnimation;
import fr.dreamin.dreamapi.api.glowing.service.GlowingService;
import fr.dreamin.dreamapi.api.glowing.block.GlowingBlockManager;
import fr.dreamin.dreamapi.api.glowing.entity.GlowingEntityManager;
import fr.dreamin.dreamapi.api.glowing.packet.PacketReflection;
import fr.dreamin.dreamapi.api.glowing.team.TeamOptions;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.api.glowing.event.set.GlowingBlockSetEvent;
import fr.dreamin.dreamapi.api.glowing.event.set.GlowingEntitySetEvent;
import fr.dreamin.dreamapi.api.glowing.event.unset.GlowingBlockUnSetEvent;
import fr.dreamin.dreamapi.api.glowing.event.unset.GlowingEntityUnSetEvent;
import fr.dreamin.dreamapi.core.time.TickTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Implementation of {@link GlowingService} using GlowingEntities and GlowingBlocks.
 * Provides a unified API for managing glowing effects.
 */
@DreamAutoService(value = GlowingService.class)
public final class GlowingServiceImpl implements GlowingService, DreamService, Listener {

  private final Plugin plugin;

  private final @NotNull GlowingEntityManager entityManager;
  private final @NotNull GlowingBlockManager blockManager;

  /** Per viewer state: entities (with color) & blocks currently glowing for that viewer. */
  private final Map<UUID, ViewerState> byViewer = new ConcurrentHashMap<>();
  /** Reverse index: entityId -> viewers who see it glowing. */
  private final Map<UUID, Set<UUID>> viewersByEntity = new ConcurrentHashMap<>();
  /** Reverse index: blockKey -> viewers who see it glowing. */
  private final Map<BlockKey, Set<UUID>> viewersByBlock = new ConcurrentHashMap<>();;

  /**
   * Scheduled auto-unset for ENTITY per (viewer, entityId).
   * key = viewerId + "|" + entityId
   */
  private final Map<String, TickTask<?>> entityTimers = new ConcurrentHashMap<>();
  /**
   * Scheduled auto-unset for BLOCK per (viewer, blockKey).
   * key = viewerId + "|" + blockKey
   */
  private final Map<String, TickTask<?>> blockTimers = new ConcurrentHashMap<>();

  // Animations
  private final Map<String, GlowAnimation> entityAnimations = new ConcurrentHashMap<>();
  private final Map<String, GlowAnimation> blockAnimations = new ConcurrentHashMap<>();
  private final Map<String, TickTask<?>> animationTasks = new ConcurrentHashMap<>();

  // Conditional glowing
  private final Map<UUID, ConditionalGlow> conditionalGlows = new ConcurrentHashMap<>();

  // Statistics
  private final AtomicLong totalOperations = new AtomicLong(0);

  /** Default team options for glowing (no color in tab). */
  private static final TeamOptions DEFAULT_OPTIONS = TeamOptions.builder()
    .collisionRule(Team.OptionStatus.NEVER)
    .nameTagVisibility(Team.OptionStatus.NEVER)
    .friendlyFire(false)
    .seeFriendlyInvisibles(false)
    .build();

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  public GlowingServiceImpl(Plugin plugin) {
    this.plugin = plugin;

    // Initialize packet reflection
    PacketReflection.initialize();

    // Create managers with unique UID
    final var uid = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
    this.entityManager = new GlowingEntityManager(uid);
    this.blockManager = new GlowingBlockManager(entityManager);

  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public @NotNull GlowingEntityManager getEntityManager() {
    return this.entityManager;
  }

  @Override
  public @NotNull GlowingBlockManager getGlowingBlocks() {
    return this.blockManager;
  }

  // ###############################################################
  // --------------------------- ENTITY ----------------------------
  // ###############################################################

  @Override
  public void glowEntity(final @NotNull Entity entity, final @NotNull ChatColor color, final @NotNull Player... viewers) {
    for (final var viewer : viewers) {
      if (!isViewerValid(viewer)) continue;

      if (showEntityToViewer(entity, color, viewer)) {
        final var viewerState = this.byViewer.computeIfAbsent(viewer.getUniqueId(), id -> new ViewerState());
        viewerState.entities.put(entity, color);
        this.viewersByEntity.computeIfAbsent(entity.getUniqueId(), k -> ConcurrentHashMap.newKeySet())
          .add(viewer.getUniqueId());
        this.totalOperations.incrementAndGet();
      }
    }
  }

  @Override
  public void glowEntity(final @NotNull Entity entity, final @NotNull ChatColor color, final long durationTicks, final @NotNull Player... viewers) {
    for (final var viewer : viewers) {
      if (!isViewerValid(viewer)) continue;

      if (showEntityToViewer(entity, color, viewer)) {
        final var viewerState = this.byViewer.computeIfAbsent(viewer.getUniqueId(), id -> new ViewerState());
        viewerState.entities.put(entity, color);
        this.viewersByEntity.computeIfAbsent(entity.getUniqueId(), k -> ConcurrentHashMap.newKeySet())
          .add(viewer.getUniqueId());

        final var key = entityTimerKey(viewer.getUniqueId(), entity.getUniqueId());
        cancelIfPresent(this.entityTimers, key);
        this.entityTimers.put(key, runLater(durationTicks, () -> stopEntity(entity, viewer)));
        this.totalOperations.incrementAndGet();
      }
    }
  }

  @Override
  public void stopEntity(final @NotNull Entity entity, final @NotNull Player... viewers) {
    if (viewers.length == 0) {
      final var original = this.viewersByEntity.get(entity.getUniqueId());
      if (original == null || original.isEmpty()) return;
      final var viewersSet = new HashSet<>(original);

      for (final var viewerId : viewersSet) {
        final var viewer = Bukkit.getPlayer(viewerId);
        if (viewer == null) continue;
        final var viewerState = this.byViewer.get(viewerId);

        if (hideEntityFromViewer(entity, viewer)) {
          if (viewerState != null) viewerState.entities.remove(entity);
          original.remove(viewerId);
          cancelIfPresent(this.entityTimers, entityTimerKey(viewerId, entity.getUniqueId()));
          cancelIfPresent(this.animationTasks, entityAnimationKey(viewerId, entity.getUniqueId()));
          this.entityAnimations.remove(entityAnimationKey(viewerId, entity.getUniqueId()));
          this.totalOperations.incrementAndGet();
        }
      }
      return;
    }

    for (final var viewer : viewers) {
      final var viewerState = this.byViewer.get(viewer.getUniqueId());
      if (viewerState != null) viewerState.entities.remove(entity);

      final var rev = this.viewersByEntity.get(entity.getUniqueId());

      if (hideEntityFromViewer(entity, viewer)) {
        if (rev != null) {
          rev.remove(viewer.getUniqueId());
          if (rev.isEmpty()) this.viewersByEntity.remove(entity.getUniqueId());
        }

        cancelIfPresent(this.entityTimers, entityTimerKey(viewer.getUniqueId(), entity.getUniqueId()));
        cancelIfPresent(this.animationTasks, entityAnimationKey(viewer.getUniqueId(), entity.getUniqueId()));
        this.entityAnimations.remove(entityAnimationKey(viewer.getUniqueId(), entity.getUniqueId()));
        this.totalOperations.incrementAndGet();
      }
    }
  }

  // ###############################################################
  // --------------------------- BLOCK -----------------------------
  // ###############################################################

  @Override
  public void glowBlock(final @NotNull Block block, final @NotNull ChatColor color, final @NotNull Player... viewers) {
    if (!isBlockValid(block) || viewers.length == 0) return;

    final var key = BlockKey.of(block);
    for (final var viewer : viewers) {
      if (!isViewerValid(viewer)) continue;

      if (showBlockToViewer(block, color, viewer)) {
        final var viewerState = this.byViewer.computeIfAbsent(viewer.getUniqueId(), id -> new ViewerState());
        viewerState.blocks.put(block, color);
        this.viewersByBlock.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
          .add(viewer.getUniqueId());
        this.totalOperations.incrementAndGet();
      }
    }
  }

  @Override
  public void glowBlock(final @NotNull Block block, final @NotNull ChatColor color, final long durationTicks, final @NotNull Player... viewers) {
    if (!isBlockValid(block) || viewers.length == 0) return;

    final var key = BlockKey.of(block);
    for (final var viewer : viewers) {
      if (!isViewerValid(viewer)) continue;

      if (showBlockToViewer(block, color, viewer)) {
        final var viewerState = this.byViewer.computeIfAbsent(viewer.getUniqueId(), id -> new ViewerState());
        viewerState.blocks.put(block, color);
        this.viewersByBlock.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
          .add(viewer.getUniqueId());

        final var tkey = blockTimerKey(viewer.getUniqueId(), key);
        cancelIfPresent(this.blockTimers, tkey);
        this.blockTimers.put(tkey, runLater(durationTicks, () -> stopBlock(block, viewer)));
        this.totalOperations.incrementAndGet();
      }
    }
  }

  @Override
  public void stopBlock(final @NotNull Block block, final @NotNull Player... viewers) {
    if (!isBlockValid(block)) return;

    final var key = BlockKey.of(block);

    if (viewers.length == 0) {
      final var original = this.viewersByBlock.get(key);
      if (original == null || original.isEmpty()) return;
      final var viewersSet = new HashSet<>(original);

      for (final var viewerId : viewersSet) {
        final var viewer = Bukkit.getPlayer(viewerId);
        if (viewer == null) continue;
        final var viewerState = this.byViewer.get(viewerId);

        if (hideBlockFromViewer(block, viewer)) {
          if (viewerState != null) viewerState.blocks.remove(block);
          original.remove(viewerId);
          cancelIfPresent(this.blockTimers, blockTimerKey(viewerId, key));
          cancelIfPresent(this.animationTasks, blockAnimationKey(viewerId, key));
          this.blockAnimations.remove(blockAnimationKey(viewerId, key));
          this.totalOperations.incrementAndGet();
        }
      }
      return;
    }

    for (final var viewer : viewers) {
      if (viewer == null) continue;
      final var viewerState = this.byViewer.get(viewer.getUniqueId());
      if (viewerState != null) viewerState.blocks.remove(block);

      final var rev = this.viewersByBlock.get(key);

      if (hideBlockFromViewer(block, viewer)) {
        if (rev != null) {
          rev.remove(viewer.getUniqueId());
          if (rev.isEmpty()) this.viewersByBlock.remove(key);
        }

        cancelIfPresent(this.blockTimers, blockTimerKey(viewer.getUniqueId(), key));
        cancelIfPresent(this.animationTasks, blockAnimationKey(viewer.getUniqueId(), key));
        this.blockAnimations.remove(blockAnimationKey(viewer.getUniqueId(), key));
        this.totalOperations.incrementAndGet();
      }
    }
  }

  // ###############################################################
  // ------------------------- ANIMATIONS --------------------------
  // ###############################################################

  @Override
  public void glowEntityAnimated(final @NotNull Entity entity, final @NotNull GlowAnimation animation, final @NotNull Player... viewers) {
    for (final var viewer : viewers) {
      if (!isViewerValid(viewer)) continue;

      final var animKey = entityAnimationKey(viewer.getUniqueId(), entity.getUniqueId());
      this.entityAnimations.put(animKey, animation);

      // Start animation task
      cancelIfPresent(this.animationTasks, animKey);
      final var task = startEntityAnimationTask(entity, animation, viewer);
      this.animationTasks.put(animKey, task);
      this.totalOperations.incrementAndGet();
    }
  }

  @Override
  public void glowEntityAnimated(final @NotNull Entity entity, final @NotNull GlowAnimation animation, final long durationTicks, final @NotNull Player... viewers) {
    for (final var viewer : viewers) {
      if (!isViewerValid(viewer)) continue;

      final var animKey = entityAnimationKey(viewer.getUniqueId(), entity.getUniqueId());
      this.entityAnimations.put(animKey, animation);

      // Start animation task
      cancelIfPresent(this.animationTasks, animKey);
      final var task = startEntityAnimationTask(entity, animation, viewer);
      this.animationTasks.put(animKey, task);

      // Schedule stop
      final var timerKey = entityTimerKey(viewer.getUniqueId(), entity.getUniqueId());
      cancelIfPresent(this.entityTimers, timerKey);
      this.entityTimers.put(timerKey, runLater(durationTicks, () -> stopEntity(entity, viewer)));
      this.totalOperations.incrementAndGet();
    }
  }

  @Override
  public void glowBlockAnimated(final @NotNull Block block, final @NotNull GlowAnimation animation, final @NotNull Player... viewers) {
    for (final var viewer : viewers) {
      if (!isViewerValid(viewer)) continue;

      final var animKey = blockAnimationKey(viewer.getUniqueId(), BlockKey.of(block));
      this.blockAnimations.put(animKey, animation);

      // Start animation task
      cancelIfPresent(this.animationTasks, animKey);
      final var task = startBlockAnimationTask(block, animation, viewer);
      this.animationTasks.put(animKey, task);
      this.totalOperations.incrementAndGet();
    }
  }

  @Override
  public void glowBlockAnimated(final @NotNull Block block, final @NotNull GlowAnimation animation, final long durationTicks, final @NotNull Player... viewers) {
    for (final var viewer : viewers) {
      if (!isViewerValid(viewer)) continue;

      final var animKey = blockAnimationKey(viewer.getUniqueId(), BlockKey.of(block));
      this.blockAnimations.put(animKey, animation);

      // Start animation task
      cancelIfPresent(this.animationTasks, animKey);
      final var task = startBlockAnimationTask(block, animation, viewer);
      this.animationTasks.put(animKey, task);

      // Schedule stop
      final var key = BlockKey.of(block);
      final var timerKey = blockTimerKey(viewer.getUniqueId(), key);
      cancelIfPresent(this.blockTimers, timerKey);
      this.blockTimers.put(timerKey, runLater(durationTicks, () -> stopBlock(block, viewer)));
      this.totalOperations.incrementAndGet();
    }
  }

  // ###############################################################
  // ----------------------- CONDITIONAL ---------------------------
  // ###############################################################

  @Override
  public void glowEntitiesMatching(final @NotNull Predicate<Entity> condition, final @NotNull ChatColor color,
                                   final long checkInterval, final @NotNull Player viewer) {
    if (!isViewerValid(viewer)) return;

    // Stop existing conditional glow
    stopConditionalGlowing(viewer);

    // Start new conditional glow
    final var task = new TickTask() {
      @Override
      public void onTick() {
        viewer.getWorld().getEntities().stream()
          .filter(condition)
          .forEach(entity -> {
            if (!getGlowingEntities(viewer).contains(entity)) {
              glowEntity(entity, color, viewer);
            }
          });
      }
    }.every(checkInterval).start();

    this.conditionalGlows.put(viewer.getUniqueId(), new ConditionalGlow(condition, color, checkInterval, task));
    this.totalOperations.incrementAndGet();
  }

  @Override
  public void stopConditionalGlowing(final @NotNull Player viewer) {
    final var conditional = this.conditionalGlows.remove(viewer.getUniqueId());
    if (conditional != null) {
      conditional.task().stop();
      this.totalOperations.incrementAndGet();
    }
  }

  // ###############################################################
  // ------------------------- BY ZONE -----------------------------
  // ###############################################################

  @Override
  public void glowEntitiesInRadius(final @NotNull Location center, final double radius, final @Nullable EntityType type, final @NotNull ChatColor color, final @NotNull Player... viewers) {
    final var radiusSquared = radius * radius;
    final var entities = center.getWorld().getNearbyEntities(center, radius, radius, radius).stream()
      .filter(e -> type == null || e.getType() == type)
      .filter(e -> e.getLocation().distanceSquared(center) <= radiusSquared)
      .toList();

    for (final var entity : entities) {
      glowEntity(entity, color, viewers);
    }
  }

  @Override
  public void glowBlocksInRadius(final @NotNull Location center, final double radius, final @NotNull ChatColor color, final @NotNull Player... viewers) {
    final var radiusInt = (int) Math.ceil(radius);
    final var centerBlock = center.getBlock();

    for (int x = -radiusInt; x <= radiusInt; x++) {
      for (int y = -radiusInt; y <= radiusInt; y++) {
        for (int z = -radiusInt; z <= radiusInt; z++) {
          if (x * x + y * y + z * z > radius * radius) continue;

          final var block = centerBlock.getRelative(x, y, z);
          if (isBlockValid(block)) {
            glowBlock(block, color, viewers);
          }
        }
      }
    }
  }

  // ###############################################################
  // --------------------- LINE OF SIGHT ---------------------------
  // ###############################################################

  @Override
  public @Nullable Entity glowEntityInCrosshair(final @NotNull Player viewer, final int maxDistance, final @NotNull ChatColor color, final long duration) {
    final var result = viewer.rayTraceEntities(maxDistance);
    if (result != null && result.getHitEntity() != null) {
      final var entity = result.getHitEntity();
      glowEntity(entity, color, duration, viewer);
      return entity;
    }
    return null;
  }

  @Override
  public @Nullable Block glowBlockInCrosshair(final @NotNull Player viewer, final int maxDistance, final @NotNull ChatColor color, final long duration) {
    final var block = viewer.getTargetBlockExact(maxDistance);
    if (block != null && isBlockValid(block)) {
      glowBlock(block, color, duration, viewer);
      return block;
    }
    return null;
  }

  @Override
  public void glowVisibleEntities(final @NotNull Player viewer, final double maxDistance, final @Nullable Predicate<Entity> filter, final @NotNull ChatColor color) {
    final var entities = viewer.getWorld().getNearbyEntities(viewer.getLocation(), maxDistance, maxDistance, maxDistance)
      .stream()
      .filter(e -> filter == null || filter.test(e))
      .filter(e -> viewer.hasLineOfSight(e))
      .toList();

    for (final var entity : entities) {
      glowEntity(entity, color, viewer);
    }
  }

  @Override
  public void glowVisibleBlocks(final @NotNull Player viewer, final double maxDistance, final @NotNull ChatColor color) {
    final var location = viewer.getLocation();
    final var direction = location.getDirection().normalize();

    for (double dist = 1; dist <= maxDistance; dist += 0.5) {
      final var target = location.clone().add(direction.clone().multiply(dist));
      final var block = target.getBlock();

      if (isBlockValid(block)) {
        glowBlock(block, color, viewer);
      }
    }
  }

  // ###############################################################
  // ----------------------- BULK / VIEWER -------------------------
  // ###############################################################

  @Override
  public void clearForViewer(final @NotNull Player viewer) {
    final var viewerUniqueId = viewer.getUniqueId();
    final var viewerState = this.byViewer.get(viewerUniqueId);
    if (viewerState == null) return;

    for (final var entity : new ArrayList<>(viewerState.entities.keySet())) {
      final var rev = this.viewersByEntity.get(entity.getUniqueId());

      if (hideEntityFromViewer(entity, viewer)) {
        if (rev != null) {
          rev.remove(viewerUniqueId);
          if (rev.isEmpty()) this.viewersByEntity.remove(entity.getUniqueId());
        }

        cancelIfPresent(this.entityTimers, entityTimerKey(viewerUniqueId, entity.getUniqueId()));
        cancelIfPresent(this.animationTasks, entityAnimationKey(viewerUniqueId, entity.getUniqueId()));
      }
    }

    for (final var block : new ArrayList<>(viewerState.blocks.keySet())) {
      final var bk = BlockKey.of(block);
      final var rev = this.viewersByBlock.get(bk);

      if (hideBlockFromViewer(block, viewer)) {
        if (rev != null) {
          rev.remove(viewerUniqueId);
          if (rev.isEmpty()) this.viewersByBlock.remove(bk);
        }

        cancelIfPresent(this.blockTimers, blockTimerKey(viewerUniqueId, bk));
        cancelIfPresent(this.animationTasks, blockAnimationKey(viewerUniqueId, bk));
      }
    }

    stopConditionalGlowing(viewer);
    viewerState.entities.clear();
    viewerState.blocks.clear();
    this.totalOperations.incrementAndGet();
  }

  @Override
  public void reapplyForViewer(final @NotNull Player viewer) {
    if (!isViewerValid(viewer)) return;
    final var viewerState = this.byViewer.get(viewer.getUniqueId());
    if (viewerState == null) return;

    for (final var e : viewerState.entities.entrySet()) {
      final var ent = e.getKey();
      if (isEntityValid(ent))
        showEntityToViewer(ent, e.getValue(), viewer);
    }

    for (final var e : viewerState.blocks.entrySet()) {
      final var block = e.getKey();
      if (isBlockValid(block))
        showBlockToViewer(block, e.getValue(), viewer);
    }
  }

  @Override
  public void reapplyTargetPlayerForAllViewers(final @NotNull Player target) {
    final var viewers = this.viewersByEntity.get(target.getUniqueId());
    if (viewers == null || viewers.isEmpty()) return;

    for (final var viewerId : viewers) {
      final var viewer = Bukkit.getPlayer(viewerId);
      if (!isViewerValid(viewer)) continue;

      final var vs = this.byViewer.get(viewerId);
      if (vs == null) continue;

      final var color = vs.entities.get(target);
      if (color != null)
        showEntityToViewer(target, color, viewer);
    }
  }

  // ###############################################################
  // --------------------------- QUERIES ---------------------------
  // ###############################################################

  @Override
  public Set<Entity> getGlowingEntities(final @NotNull Player viewer) {
    final var viewerState = this.byViewer.get(viewer.getUniqueId());
    if (viewerState == null) return Collections.emptySet();
    return Collections.unmodifiableSet(viewerState.entities.keySet());
  }

  @Override
  public Set<Block> getGlowingBlocks(final @NotNull Player viewer) {
    final var viewerState = this.byViewer.get(viewer.getUniqueId());
    if (viewerState == null) return Collections.emptySet();
    return Collections.unmodifiableSet(viewerState.blocks.keySet());
  }

  @Override
  public @NotNull GlowingStats getStats() {
    return new GlowingStatsImpl();
  }


  // ###############################################################
  // ---------------------- LISTENER METHODS -----------------------
  // ###############################################################

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  private void onViewerJoin(final @NotNull PlayerJoinEvent event) {
    final var player = event.getPlayer();
    reapplyTargetPlayerForAllViewers(player);
    reapplyForViewer(player);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  private void onTargetEntityDeath(final @NotNull EntityDeathEvent event) {
    stopEntity(event.getEntity());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  private void onBlockBreak(final @NotNull BlockBreakEvent event) {
    stopBlock(event.getBlock());
  }

  // ###############################################################
  // ------------------------ ANIMATION TASKS ----------------------
  // ###############################################################

  private TickTask<?> startEntityAnimationTask(final @NotNull Entity entity, final @NotNull GlowAnimation animation, final @NotNull Player viewer) {
    return new TickTask() {
      private long tick = 0;

      @Override
      public void onTick() {
        if (!isEntityValid(entity) || !isViewerValid(viewer)) {
          stop();
          return;
        }

        final var color = animation.getColorAtTick(tick);
        showEntityToViewer(entity, color, viewer);

        if (animation.isComplete(tick)) {
          stop();
        }

        tick++;
      }
    }.every(5L).start(); // Update every 5 ticks (4 times per second)
  }

  private TickTask<?> startBlockAnimationTask(final @NotNull Block block, final @NotNull GlowAnimation animation, final @NotNull Player viewer) {
    return new TickTask() {
      private long tick = 0;

      @Override
      public void onTick() {
        if (!isBlockValid(block) || !isViewerValid(viewer)) {
          stop();
          return;
        }

        final var color = animation.getColorAtTick(tick);
        showBlockToViewer(block, color, viewer);

        if (animation.isComplete(tick)) {
          stop();
        }

        tick++;
      }
    }.every(5L).start();
  }

  // ###############################################################
  // ---------------------------- GLUE -----------------------------
  // ###############################################################

  private boolean showEntityToViewer(final @NotNull Entity entity, final @NotNull ChatColor color, final @NotNull Player viewer) {
    final var event = new GlowingEntitySetEvent(viewer, color, entity);
    DreamAPI.getAPI().callEvent(event);
    if (event.isCancelled()) return false;

    try {
      this.entityManager.setGlowing(entity, viewer, event.getColor(), DEFAULT_OPTIONS);
      return true;
    } catch (ReflectiveOperationException e) {
      DreamAPI.getAPI().getLogger().warning("Failed to set entity glowing: " + e.getMessage());
      return false;
    }
  }

  private boolean hideEntityFromViewer(final @NotNull Entity entity, final @NotNull Player viewer) {
    final var event = new GlowingEntityUnSetEvent(viewer, entity);
    DreamAPI.getAPI().callEvent(event);
    if (event.isCancelled()) return false;

    try {
      this.entityManager.unsetGlowing(entity, viewer);
      return true;
    } catch (ReflectiveOperationException e) {
      DreamAPI.getAPI().getLogger().warning("Failed to unset entity glowing: " + e.getMessage());
      return false;
    }
  }

  private boolean showBlockToViewer(final @NotNull Block block, final @NotNull ChatColor color, final @NotNull Player viewer) {
    final var event = new GlowingBlockSetEvent(viewer, color, block);
    DreamAPI.getAPI().callEvent(event);
    if (event.isCancelled()) return false;

    try {
      this.blockManager.setGlowing(block, viewer, event.getColor(), DEFAULT_OPTIONS);
      return true;
    } catch (ReflectiveOperationException e) {
      DreamAPI.getAPI().getLogger().warning("Failed to set block glowing: " + e.getMessage());
      return false;
    }
  }

  private boolean hideBlockFromViewer(final @NotNull Block block, final @NotNull Player viewer) {
    final var event = new GlowingBlockUnSetEvent(viewer, block);
    DreamAPI.getAPI().callEvent(event);
    if (event.isCancelled()) return false;

    try {
      this.blockManager.unsetGlowing(block, viewer);
      return true;
    } catch (ReflectiveOperationException e) {
      DreamAPI.getAPI().getLogger().warning("Failed to unset block glowing: " + e.getMessage());
      return false;
    }
  }

  // ###############################################################
  // --------------------------- HELPER ----------------------------
  // ###############################################################

  private boolean isViewerValid(final Player viewer) {
    return viewer != null && viewer.isOnline();
  }

  private boolean isEntityValid(final Entity e) {
    return e != null && (e.isValid() || (e instanceof Player p && p.isOnline()));
  }

  private boolean isBlockValid(final Block b) {
    return b != null && b.getType() != Material.AIR;
  }

  private String entityTimerKey(final UUID viewerId, final UUID entityId) {
    return viewerId + "|" + entityId;
  }

  private String blockTimerKey(final UUID viewerId, final BlockKey key) {
    return viewerId + "|" + key.toString();
  }

  private String entityAnimationKey(final UUID viewerId, final UUID entityId) {
    return "anim_" + viewerId + "|" + entityId;
  }

  private String blockAnimationKey(final UUID viewerId, final BlockKey key) {
    return "anim_" + viewerId + "|" + key.toString();
  }

  private void cancelIfPresent(final Map<String, TickTask<?>> map, final String key) {
    final var task = map.remove(key);
    if (task != null)
      task.stop();
  }

  private TickTask<?> runLater(final long ticks, final Runnable action) {
    return new TickTask() {
      @Override public void onEnd() { action.run(); }
    }.limit(1)
      .delay(ticks)
      .autoStop(true)
      .start();
  }

  // ###############################################################
  // --------------------------- CLASS -----------------------------
  // ###############################################################

  private static final class ViewerState {
    final Map<Entity, ChatColor> entities = new HashMap<>();
    final Map<Block, ChatColor> blocks = new HashMap<>();
  }

  private record BlockKey(UUID world, int x, int y, int z) {
    static BlockKey of(final Block b) {
      return new BlockKey(b.getWorld().getUID(), b.getX(), b.getY(), b.getZ());
    }
    @Override public String toString() {
      return world + ":" + x + "," + y + "," + z;
    }
  }

  private record ConditionalGlow(Predicate<Entity> condition, ChatColor color, long checkInterval, TickTask<?> task) {}

  // ###############################################################
  // ------------------------- STATS IMPL --------------------------
  // ###############################################################

  private final class GlowingStatsImpl implements GlowingStats {

    @Override
    public int getTotalGlowingEntities() {
      return byViewer.values().stream()
        .mapToInt(vs -> vs.entities.size())
        .sum();
    }

    @Override
    public int getTotalGlowingBlocks() {
      return byViewer.values().stream()
        .mapToInt(vs -> vs.blocks.size())
        .sum();
    }

    @Override
    public @NotNull Map<UUID, Integer> getViewerEntityCounts() {
      return byViewer.entrySet().stream()
        .collect(Collectors.toMap(
          Map.Entry::getKey,
          e -> e.getValue().entities.size(),
          (a, b) -> a,
          HashMap::new
        ));
    }

    @Override
    public @NotNull Map<UUID, Integer> getViewerBlockCounts() {
      return byViewer.entrySet().stream()
        .collect(Collectors.toMap(
          Map.Entry::getKey,
          e -> e.getValue().blocks.size(),
          (a, b) -> a,
          HashMap::new
        ));
    }

    @Override
    public @NotNull Map<ChatColor, Integer> getColorDistribution() {
      final Map<ChatColor, Integer> distribution = new HashMap<>();

      for (final var vs : byViewer.values()) {
        for (final var color : vs.entities.values()) {
          distribution.merge(color, 1, Integer::sum);
        }
        for (final var color : vs.blocks.values()) {
          distribution.merge(color, 1, Integer::sum);
        }
      }

      return distribution;
    }

    @Override
    public int getActiveViewers() {
      return byViewer.size();
    }

    @Override
    public long getTotalOperations() {
      return totalOperations.get();
    }
  }
}
