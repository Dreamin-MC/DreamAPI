package fr.dreamin.dreamapi.core.nms.visual.service;

import fr.dreamin.dreamapi.api.annotations.Inject;
import fr.dreamin.dreamapi.api.nms.visual.event.ReapplyVisualForPlayerOnJoinEvent;
import fr.dreamin.dreamapi.api.nms.visual.fakeEntity.model.FakeEntity;
import fr.dreamin.dreamapi.api.nms.visual.fakeEntity.model.FakeEntityMetadata;
import fr.dreamin.dreamapi.api.nms.packet.PacketReflection;
import fr.dreamin.dreamapi.api.nms.packet.PacketSender;
import fr.dreamin.dreamapi.api.nms.visual.service.VisualService;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Inject
@DreamAutoService(VisualService.class)
public final class VisualServiceImpl implements VisualService, DreamService {

  private final AtomicInteger entityIdCounter = new AtomicInteger(2_000_000);
  private final @NotNull Map<Integer, FakeEntityImpl> fakeEntities = new ConcurrentHashMap<>();

  private final Map<UUID, ViewerState> byViewer = new ConcurrentHashMap<>();

  private final Map<Integer, Set<UUID>> viewersByEntity = new ConcurrentHashMap<>();

  // Reverse index: blockKey -> viewers who see it
  private final Map<BlockKey, Set<UUID>> viewersByBlock = new ConcurrentHashMap<>();
  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  public VisualServiceImpl() {
    PacketReflection.initialize();
  }

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public @NotNull FakeEntity spawnFakeEntity(@NotNull EntityType type, @NotNull Location location, @NonNull @NotNull Player... viewers) throws ReflectiveOperationException {
    final var entityId = this.entityIdCounter.incrementAndGet();
    final var handle = new FakeEntityImpl(entityId, type, location.clone());

    final var spawnPacket = PacketReflection.createSpawnEntityPacket(entityId, UUID.randomUUID(), type, location);
    PacketSender.sendToAll(spawnPacket, viewers);

    this.fakeEntities.put(entityId, handle);

    for (final var viewer : viewers) {
      final var viewerState = this.byViewer.computeIfAbsent(viewer.getUniqueId(), k -> new ViewerState());
      viewerState.entities.add(handle);

      this.viewersByEntity.computeIfAbsent(entityId, k -> ConcurrentHashMap.newKeySet())
        .add(viewer.getUniqueId());
    }

    return handle;
  }

  @Override
  public void removeFakeEntity(@NotNull FakeEntity entity, @NonNull @NotNull Player... viewers) throws ReflectiveOperationException {
    final var destroy = PacketReflection.createRemoveEntitiesPacket(entity.getEntityId());
    PacketSender.sendToAll(destroy, viewers);

    for (final var viewer : viewers) {
      final var viewerState = this.byViewer.get(viewer.getUniqueId());
      if (viewerState != null)
        viewerState.entities.remove(entity);

      // Remove from reverse index
      final var rev = this.viewersByEntity.get(entity.getEntityId());
      if (rev != null) {
        rev.remove(viewer.getUniqueId());
        if (rev.isEmpty())
          this.viewersByEntity.remove(entity.getEntityId());
      }
    }

    if (!this.viewersByEntity.containsKey(entity.getEntityId()))
      this.fakeEntities.remove(entity.getEntityId());
  }

  @Override
  public void updateFakeEntity(@NotNull FakeEntity entity, @NotNull Consumer<FakeEntityMetadata> mutator, @NonNull @NotNull Player... viewers) throws ReflectiveOperationException {
    final var metadata = new FakeEntityMetadataImpl(entity.getEntityId());
    mutator.accept(metadata);

    final var metadataPacket = PacketReflection.createMetadataPacket(entity.getEntityId(), metadata.toNmsList());
    PacketSender.sendToAll(metadataPacket, viewers);
  }

  @Override
  public void showFakeBlock(@NotNull Location location, @NotNull Material type, @NonNull @NotNull Player... viewers) throws ReflectiveOperationException {
    final var blockPacket = PacketReflection.createBlockChangePacket(location, type);
    PacketSender.sendToAll(blockPacket, viewers);

    final var blockKey = BlockKey.of(location);

    for (final var viewer : viewers) {
      final var viewerState = this.byViewer.computeIfAbsent(viewer.getUniqueId(), k -> new ViewerState());
      viewerState.blocks.put(blockKey, type);

      this.viewersByBlock.computeIfAbsent(blockKey, k -> ConcurrentHashMap.newKeySet())
        .add(viewer.getUniqueId());
    }

  }

  @Override
  public void hideFakeBlock(@NotNull Location location, @NonNull @NotNull Player... viewers) throws ReflectiveOperationException {
    showFakeBlock(location, location.getBlock().getType(), viewers);

    final var blockKey = BlockKey.of(location);

    for (final var viewer : viewers) {
      final var viewerState = this.byViewer.get(viewer.getUniqueId());
      if (viewerState != null)
        viewerState.blocks.remove(blockKey);
      final var rev = this.viewersByBlock.get(blockKey);
      if (rev != null) {
        rev.remove(viewer.getUniqueId());
        if (rev.isEmpty())
          this.viewersByBlock.remove(blockKey);
      }
    }

  }

  @Override
  public void setFrozenTime(@NotNull Player player, long time) {
    player.setPlayerTime(time, true);
  }

  @Override
  public void resetTime(@NotNull Player player) {
    player.resetPlayerTime();
  }

  @Override
  public void clearForViewer(final @NotNull Player viewer) throws ReflectiveOperationException {
    final var viewerState = this.byViewer.remove(viewer.getUniqueId());
    if (viewerState == null) return;

    final var entityIds = viewerState.entities.stream()
      .mapToInt(FakeEntity::getEntityId)
      .toArray();

    if (entityIds.length > 0) {
      final var destroyPacket = PacketReflection.createRemoveEntitiesPacket(entityIds);
      PacketSender.send(viewer, destroyPacket);

      for (final var entityId : entityIds) {
        final var rev = this.viewersByEntity.get(entityId);
        if (rev != null) {
          rev.remove(viewer.getUniqueId());
          if (rev.isEmpty()) {
            this.viewersByEntity.remove(entityId);
            this.fakeEntities.remove(entityId);
          }
        }
      }
    }

    // Restore all fake blocks
    for (final var entry : viewerState.blocks.entrySet()) {
      final var blockKey = entry.getKey();
      final var location = blockKey.toLocation();
      if (location != null) {
        final var realType = location.getBlock().getType();
        final var restorePacket = PacketReflection.createBlockChangePacket(location, realType);
        PacketSender.send(viewer, restorePacket);
      }

      // Clean reverse index
      final var rev = this.viewersByBlock.get(blockKey);
      if (rev != null) {
        rev.remove(viewer.getUniqueId());
        if (rev.isEmpty()) {
          this.viewersByBlock.remove(blockKey);
        }
      }
    }
  }

  @Override
  public void reapplyForViewer(final @NotNull Player viewer) throws ReflectiveOperationException {
    final var viewerState = this.byViewer.get(viewer.getUniqueId());
    if (viewerState == null) return;

    // Respawn all fake entities
    for (final var entity : viewerState.entities) {
      final var handle = this.fakeEntities.get(entity.getEntityId());
      if (handle != null) {
        final var spawnPacket = PacketReflection.createSpawnEntityPacket(
          handle.entityId(),
          UUID.randomUUID(),
          handle.type(),
          handle.location()
        );
        PacketSender.send(viewer, spawnPacket);
      }
    }

    // Re-show all fake blocks
    for (final var entry : viewerState.blocks.entrySet()) {
      final var location = entry.getKey().toLocation();
      if (location != null) {
        final var blockPacket = PacketReflection.createBlockChangePacket(location, entry.getValue());
        PacketSender.send(viewer, blockPacket);
      }
    }
  }

  @Override
  public @NotNull Set<FakeEntity> getFakeEntities(final @NotNull Player viewer) {
    final var viewerState = this.byViewer.get(viewer.getUniqueId());
    if (viewerState == null) return Collections.emptySet();
    return Collections.unmodifiableSet(viewerState.entities);
  }

  @Override
  public @NotNull Map<Location, Material> getFakeBlocks(final @NotNull Player viewer) {
    final var viewerState = this.byViewer.get(viewer.getUniqueId());
    if (viewerState == null) return Collections.emptyMap();

    final Map<Location, Material> result = new HashMap<>();
    for (final var entry : viewerState.blocks.entrySet()) {
      final var location = entry.getKey().toLocation();
      if (location != null) {
        result.put(location, entry.getValue());
      }
    }
    return Collections.unmodifiableMap(result);
  }

  // ###############################################################
  // ---------------------- LISTENER METHODS -----------------------
  // ###############################################################

  @EventHandler(priority = EventPriority.MONITOR)
  private void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
    final var player = event.getPlayer();

    try {
      if (new ReapplyVisualForPlayerOnJoinEvent(player, getFakeEntities(player), getFakeBlocks(player)).callEvent())
        reapplyForViewer(player);
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
    }
  }

  // ###############################################################
  // ------------------------- INNER CLASSES -----------------------
  // ###############################################################

  private static final class ViewerState {
    final Set<FakeEntity> entities = ConcurrentHashMap.newKeySet();
    final Map<BlockKey, Material> blocks = new ConcurrentHashMap<>();
  }

  private record BlockKey(UUID world, int x, int y, int z) {
    static BlockKey of(final @NotNull Location location) {
      return new BlockKey(
        location.getWorld().getUID(),
        location.getBlockX(),
        location.getBlockY(),
        location.getBlockZ()
      );
    }

    @Nullable Location toLocation() {
      final var world = org.bukkit.Bukkit.getWorld(this.world);
      if (world == null) return null;
      return new Location(world, x, y, z);
    }

    @Override
    public String toString() {
      return world + ":" + x + "," + y + "," + z;
    }
  }

  private record FakeEntityImpl(int entityId, @NotNull EntityType type, @NotNull Location location) implements FakeEntity {

    @Override
    public int getEntityId() {
      return this.entityId;
    }

    @Override
    public @NotNull EntityType getType() {
      return this.type;
    }

    @Override
    public @NotNull Location getInitialLocation() {
      return this.location;
    }

  }

  @RequiredArgsConstructor
  private static final class FakeEntityMetadataImpl implements FakeEntityMetadata {

    private final int entityId;
    private final List<Object> dataValues = new ArrayList<>();

    @Override
    public void setCustomName(@Nullable Component name) {
    }

    @Override
    public void setCustomNameVisible(boolean visible) {
    }

    @Override
    public void setGlowing(boolean glowing) {
    }

    @Override
    public void setInvisible(boolean invisible) {
    }

    @Override
    public void setNoGravity(boolean noGravity) {
    }

    @Override
    public void setSilent(boolean silent) {
    }

    @Override
    public void setHelmet(@Nullable ItemStack item) {
    }

    @Override
    public void setChestplate(@Nullable ItemStack item) {
    }

    @Override
    public void setLeggings(@Nullable ItemStack item) {
    }

    @Override
    public void setBoots(@Nullable ItemStack item) {
    }

    @Override
    public void setMainHand(@Nullable ItemStack item) {
    }

    @Override
    public void setOffHand(@Nullable ItemStack item) {
    }

    public @NotNull List<Object> toNmsList() {
      return this.dataValues;
    }

  }


}
