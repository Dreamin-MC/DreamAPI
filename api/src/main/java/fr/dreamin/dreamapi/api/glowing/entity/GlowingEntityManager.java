package fr.dreamin.dreamapi.api.glowing.entity;

import fr.dreamin.dreamapi.api.nms.packet.MetadataInterceptor;
import fr.dreamin.dreamapi.api.nms.packet.PacketConstants;
import fr.dreamin.dreamapi.api.nms.packet.PacketReflection;
import fr.dreamin.dreamapi.api.nms.packet.PacketSender;
import fr.dreamin.dreamapi.api.glowing.team.TeamDataCache;
import fr.dreamin.dreamapi.api.glowing.team.TeamOptions;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public final class GlowingEntityManager {

  private final int uid;
  private final Map<UUID, PlayerGlowingData> playerDataMap = new ConcurrentHashMap<>();

  private final TeamDataCache teamCache;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  public GlowingEntityManager(final int uid) {
    this.uid = uid;
    this.teamCache = new TeamDataCache(uid);
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public void setGlowing(final @NotNull Entity entity, final @NotNull Player viewer, final @NotNull ChatColor color, final @NotNull TeamOptions options) throws ReflectiveOperationException {

    if (!color.isColor())
      throw new IllegalArgumentException("The color must be a valid color");

    final var entityId = entity.getEntityId();
    final var entityIdentifier = getEntityIdentifier(entity);
    final var entityFlags = getEntityFlags(entity);

    setGlowing(entityId, entityIdentifier, viewer, color, entityFlags, options);
  }

  public void setGlowing(final int entityId, final @NotNull String entityIdentifier, final @NotNull Player viewer, final @NotNull ChatColor color, final byte otherFlags, final @NotNull TeamOptions options) throws ReflectiveOperationException {

    if (!color.isColor())
      throw new IllegalArgumentException("The color must be a valid color");

    final var playerData = this.playerDataMap.computeIfAbsent(viewer.getUniqueId(), uuid -> {
      try {
        return new PlayerGlowingData(viewer);
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException("Failed to create player glowing data", e);
      }
    });

    var glowData = playerData.entities.get(entityId);

    if (glowData == null) {
      // Create new glowing effect
      glowData = new EntityGlowData(entityId, entityIdentifier, color, options, otherFlags);
      playerData.entities.put(entityId, glowData);

      applyGlowingFlags(viewer, entityId, otherFlags);
      if (color != null)
        applyTeamColor(viewer, entityIdentifier, color, options, playerData);

    } else {
      // Update existing glowing effect
      final var colorChanged = !Objects.equals(glowData.getColor(), color);
      final var optionsChanged = !Objects.equals(glowData.getOptions(), options);

      if (!colorChanged && !optionsChanged) return;

      if (color == null && glowData.getColor() != null) {
        removeTeamColor(viewer, entityIdentifier, glowData.getColor(), playerData);
        glowData.setColor(null);
      } else if (color != null) {
        if (colorChanged || optionsChanged) {
          if (glowData.getColor() != null && !glowData.getColor().equals(color))
            removeTeamColor(viewer, entityIdentifier, glowData.getColor(), playerData);

          glowData.setColor(color);
          glowData.setOptions(options);
          applyTeamColor(viewer, entityIdentifier, color, options, playerData);
        }
      }
    }
  }

  public void unsetGlowing(final @NotNull Entity entity, final @NotNull Player viewer) throws ReflectiveOperationException {
    unsetGlowing(entity.getEntityId(), viewer);
  }
  public void unsetGlowing(final int entityId, final @NotNull Player viewer) throws ReflectiveOperationException {
    final var playerData = this.playerDataMap.get(viewer.getUniqueId());
    if (playerData == null) return;

    final var glowData = playerData.entities.remove(entityId);
    if (glowData == null) return;

    removeGlowingFlags(viewer, entityId, glowData.getOtherFlags());

    if (glowData.getColor() != null)
      removeTeamColor(viewer, glowData.getEntityIdentifier(), glowData.getColor(), playerData);
  }

  public @NotNull Set<Integer> getGlowingEntityIds(final @NotNull Player viewer) {
    final var playerData = this.playerDataMap.get(viewer.getUniqueId());
    if (playerData == null) return Collections.emptySet();
    return Collections.unmodifiableSet(playerData.entities.keySet());
  }

  public @Nullable EntityGlowData getGlowData(final @NotNull Player viewer, int entityId) {
    final var playerData = this.playerDataMap.get(viewer.getUniqueId());
    if (playerData == null) return null;
    return playerData.entities.get(entityId);
  }

  public void clearViewer(final @NotNull Player viewer) throws ReflectiveOperationException {
    final var playerData = this.playerDataMap.remove(viewer.getUniqueId());
    if (playerData == null) return;

    for (final var glowData : playerData.entities.values()) {
      removeGlowingFlags(viewer, glowData.getEntityId(), glowData.getOtherFlags());
      if (glowData.getColor() != null)
        removeTeamColor(viewer, glowData.getEntityIdentifier(), glowData.getColor(), playerData);
    }

    removePacketInterceptor(playerData);
  }

  public void shutdown() {
    this.playerDataMap.values().forEach(data -> {
      try {
        removePacketInterceptor(data);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    this.playerDataMap.clear();
    this.teamCache.clear();
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void applyGlowingFlags(final @NotNull Player viewer, final int entityId, final byte otherFlags) throws ReflectiveOperationException {
    final var newFlags = (byte) (otherFlags | PacketConstants.GLOWING_FLAG);
    final var dataValue = PacketReflection.createDataValue(newFlags);
    final var packet = PacketReflection.createMetadataPacket(entityId, List.of(dataValue));
    PacketSender.send(viewer, packet);
  }

  private void removeGlowingFlags(final @NotNull Player viewer, final int entityId, final byte otherFlags) throws ReflectiveOperationException {
    final var newFlags = (byte) (otherFlags & ~PacketConstants.GLOWING_FLAG);
    final var dataValue = PacketReflection.createDataValue(newFlags);
    final var packet = PacketReflection.createMetadataPacket(entityId, List.of(dataValue));
    PacketSender.send(viewer, packet);
  }

  private void applyTeamColor(final @NotNull Player viewer, final @NotNull String entityIdentifier, final @NotNull ChatColor color, final @NotNull TeamOptions options, final @NotNull PlayerGlowingData playerData) throws ReflectiveOperationException {
    final var shouldSendCreation = playerData.sentColors.add(color);
    final var teamData = this.teamCache.getOrCreate(color, options);

    final var addEntityPacket = teamData.getAddEntityPacket(entityIdentifier);

    if (shouldSendCreation)
      PacketSender.send(viewer, teamData.getCreationPacket(), addEntityPacket);
    else
      PacketSender.send(viewer, addEntityPacket);
  }

  private void removeTeamColor(final @NotNull Player viewer, final @NotNull String entityIdentifier, final @NotNull ChatColor color, final @NotNull PlayerGlowingData playerData) throws ReflectiveOperationException {
    final var teamData = this.teamCache.getOrCreate(color, TeamOptions.builder().build());
    final var removeEntityPacket = teamData.getRemoveEntityPacket(entityIdentifier);
    PacketSender.send(viewer, removeEntityPacket);
  }

  private void removePacketInterceptor(final @NotNull PlayerGlowingData playerData) throws ReflectiveOperationException {
    if (playerData.interceptor != null) {
      final var channel = PacketReflection.getChannel(playerData.viewer);
      channel.pipeline().remove(playerData.interceptor);
    }
  }

  private byte getEntityFlags(final @NotNull Entity entity) throws ReflectiveOperationException {
    final var nmsEntity = PacketReflection.getNmsEntity(entity);
    return PacketReflection.getEntityFlags(nmsEntity);
  }

  private @NotNull String getEntityIdentifier(final @NotNull Entity entity) {
    return entity instanceof Player ? entity.getName() : entity.getUniqueId().toString();
  }

  // ###############################################################
  // --------------------------- CLASSES ---------------------------
  // ###############################################################

  @Getter
  private final class PlayerGlowingData {
    private final @NotNull Player viewer;
    private final Map<Integer, EntityGlowData> entities = new ConcurrentHashMap<>();
    private final EnumSet<ChatColor> sentColors = EnumSet.noneOf(ChatColor.class);
    private MetadataInterceptor interceptor;

    private PlayerGlowingData(final @NotNull Player viewer) throws ReflectiveOperationException {
      this.viewer = viewer;
      installPacketInterceptor();
    }

    // ###############################################################
    // ----------------------- PRIVATE METHODS -----------------------
    // ###############################################################

    private void installPacketInterceptor() throws ReflectiveOperationException {
      this.interceptor = new MetadataInterceptor(entityId -> {
        final var data = this.entities.get(entityId);
        if (data == null || !data.isEnabled()) return null;
        return (byte) (data.getOtherFlags() | PacketConstants.GLOWING_FLAG);
      });

      final var channel = PacketReflection.getChannel(viewer);
      channel.pipeline().addBefore("packet_handler", null, interceptor);

    }

  }

}
