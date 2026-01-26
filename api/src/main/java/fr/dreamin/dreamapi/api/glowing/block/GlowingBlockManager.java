package fr.dreamin.dreamapi.api.glowing.block;

import fr.dreamin.dreamapi.api.glowing.entity.GlowingEntityManager;
import fr.dreamin.dreamapi.api.glowing.packet.PacketConstants;
import fr.dreamin.dreamapi.api.glowing.packet.PacketReflection;
import fr.dreamin.dreamapi.api.glowing.packet.PacketSender;
import fr.dreamin.dreamapi.api.glowing.team.TeamOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@RequiredArgsConstructor
public final class GlowingBlockManager {

  private static final AtomicInteger ENTITY_ID_COUNTER =
    new AtomicInteger(ThreadLocalRandom.current().nextInt(1_000_000, 2_000_000_000));

  private final @NotNull GlowingEntityManager entityManager;
  private final Map<UUID, PlayerBlockData> playerDataMap = new ConcurrentHashMap<>();

  public void setGlowing(final @NotNull Block block, final @NotNull Player viewer, final @NotNull ChatColor color, final @NotNull TeamOptions options) throws ReflectiveOperationException {
    setGlowing(block.getLocation(), viewer, color, options);
  }


  public void setGlowing(final @NotNull Location location, final @NotNull Player viewer, final @NotNull ChatColor color, final @NotNull TeamOptions options) throws ReflectiveOperationException {

    if (!color.isColor())
      throw new IllegalArgumentException("ChatColor must be a color format");

    final var normalized = normalizeLocation(location);

    final var playerData = this.playerDataMap.computeIfAbsent(viewer.getUniqueId(),
      uuid -> new PlayerBlockData(viewer));

    var blockData = playerData.blocks.get(normalized);

    if (blockData == null) {
      blockData = new BlockGlowData(normalized, color, options);
      playerData.blocks.put(normalized, blockData);

      if (canSee(viewer, normalized))
        spawnGlowingEntity(blockData, viewer);

    } else {
      final var colorChanged = !blockData.getColor().equals(color);
      final var optionsChanged = !blockData.getOptions().equals(options);

      if (colorChanged || optionsChanged) {
        blockData.setColor(color);
        blockData.setOptions(options);

        if (blockData.isSpawned() && blockData.getEntityId() != null) {
          this.entityManager.setGlowing(
            blockData.getEntityId(),
            blockData.getEntityUuid().toString(),
            viewer,
            color,
            PacketConstants.INVISIBILITY_FLAG,
            options
          );
        }
      }
    }
  }

  /**
   * Remove glowing effect from block
   */
  public void unsetGlowing(final @NotNull Block block, final @NotNull Player viewer) throws ReflectiveOperationException {
    unsetGlowing(block.getLocation(), viewer);
  }

  /**
   * Remove glowing effect from block at location
   */
  public void unsetGlowing(final @NotNull Location location, final @NotNull Player viewer) throws ReflectiveOperationException {
    final var normalized = normalizeLocation(location);

    final var playerData = this.playerDataMap.get(viewer.getUniqueId());
    if (playerData == null) return;

    final var blockData = playerData.blocks.remove(normalized);
    if (blockData == null) return;

    removeGlowingEntity(blockData, viewer);

    if (playerData.blocks.isEmpty())
      this.playerDataMap.remove(viewer.getUniqueId());
  }

  /**
   * Get all glowing blocks for a viewer
   */
  public @NotNull Set<Location> getGlowingBlocks(final @NotNull Player viewer) {
    final var playerData = this.playerDataMap.get(viewer.getUniqueId());
    if (playerData == null) return Collections.emptySet();
    return Collections.unmodifiableSet(playerData.blocks.keySet());
  }

  /**
   * Handle chunk load - respawn blocks in that chunk
   */
  public void onChunkLoad(final @NotNull Player viewer, final int chunkX, final int chunkZ, final @NotNull World world) {
    final var playerData = this.playerDataMap.get(viewer.getUniqueId());
    if (playerData == null) return;

    playerData.blocks.forEach((location, blockData) -> {
      if (location.getWorld().equals(world)
        && location.getBlockX() >> 4 == chunkX
        && location.getBlockZ() >> 4 == chunkZ) {
        try {
          if (!blockData.isSpawned()) {
            spawnGlowingEntity(blockData, viewer);
          }
        } catch (ReflectiveOperationException e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Clear all glowing blocks for a viewer
   */
  public void clearViewer(final @NotNull Player viewer) throws ReflectiveOperationException {
    final var playerData = this.playerDataMap.remove(viewer.getUniqueId());
    if (playerData == null) return;

    for (final var blockData : playerData.blocks.values()) {
      removeGlowingEntity(blockData, viewer);
    }
  }

  /**
   * Cleanup on disable
   */
  public void shutdown() {
    this.playerDataMap.values().forEach(data -> {
      data.blocks.values().forEach(blockData -> {
        try {
          removeGlowingEntity(blockData, data.viewer);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    });
    this.playerDataMap.clear();
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void spawnGlowingEntity(final @NotNull BlockGlowData blockData, final @NotNull Player viewer)
    throws ReflectiveOperationException {

    if (blockData.isSpawned()) return;

    final var entityId = ENTITY_ID_COUNTER.getAndIncrement();
    final var entityUuid = UUID.randomUUID();

    blockData.setEntityId(entityId);
    blockData.setEntityUuid(entityUuid);

    final var loc = blockData.getLocation();
    final var addEntityPacket = PacketReflection.createAddEntityPacket(
      entityId,
      entityUuid,
      loc.getX() + 0.5,
      loc.getY(),
      loc.getZ() + 0.5,
      0f,
      0f
    );

    PacketSender.send(viewer, addEntityPacket);

    // Apply invisibility and glowing
    final var metadataPacket = PacketReflection.createMetadataPacket(
      entityId,
      List.of(PacketReflection.createDataValue(PacketConstants.INVISIBILITY_FLAG))
    );
    PacketSender.send(viewer, metadataPacket);

    // Apply glowing with team color
    this.entityManager.setGlowing(
      entityId,
      entityUuid.toString(),
      viewer,
      blockData.getColor(),
      PacketConstants.INVISIBILITY_FLAG,
      blockData.getOptions()
    );

    blockData.setSpawned(true);
  }

  private void removeGlowingEntity(final @NotNull BlockGlowData blockData, final @NotNull Player viewer)
    throws ReflectiveOperationException {

    if (!blockData.isSpawned() || blockData.getEntityId() == null) return;

    final var removePacket = PacketReflection.createRemoveEntitiesPacket(blockData.getEntityId());
    PacketSender.send(viewer, removePacket);

    this.entityManager.unsetGlowing(blockData.getEntityId(), viewer);

    blockData.setEntityId(null);
    blockData.setEntityUuid(null);
    blockData.setSpawned(false);
  }

  private @NotNull Location normalizeLocation(final @NotNull Location location) {
    location.checkFinite();
    return new Location(
      location.getWorld(),
      location.getBlockX(),
      location.getBlockY(),
      location.getBlockZ()
    );
  }

  private boolean canSee(final @NotNull Player player, final @NotNull Location location) {
    final var viewDistance = Math.min(player.getViewDistance(), Bukkit.getViewDistance());
    final var deltaChunkX = (player.getLocation().getBlockX() >> 4) - (location.getBlockX() >> 4);
    final var deltaChunkZ = (player.getLocation().getBlockZ() >> 4) - (location.getBlockZ() >> 4);
    final var chunkDistanceSquared = deltaChunkX * deltaChunkX + deltaChunkZ * deltaChunkZ;
    return chunkDistanceSquared <= viewDistance * viewDistance;
  }

  // ###############################################################
  // --------------------------- CLASSES ---------------------------
  // ###############################################################

  @Getter
  @RequiredArgsConstructor
  private static final class PlayerBlockData {
    private final @NotNull Player viewer;
    private final Map<Location, BlockGlowData> blocks = new ConcurrentHashMap<>();
  }
}