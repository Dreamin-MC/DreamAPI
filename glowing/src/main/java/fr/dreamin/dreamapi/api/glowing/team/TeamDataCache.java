package fr.dreamin.dreamapi.api.glowing.team;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public final class TeamDataCache {

  private final int uid;
  private final Map<TeamKey, CachedTeamData> cache = new ConcurrentHashMap<>();

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public @NotNull CachedTeamData getOrCreate(final @NotNull ChatColor color, final @NotNull TeamOptions options) {
    final var key = new TeamKey(color, options);

    return this.cache.computeIfAbsent(key, k -> {
      try {
        return new CachedTeamData(generateTeamName(color), color, options);
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException("Failed to create team data", e);
      }
    });
  }

  public void clear() {
    this.cache.clear();
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private @NotNull String generateTeamName(final @NotNull ChatColor color) {
    return String.format("team_%s_%s", uid, color.getChar());
  }

  // ###############################################################
  // -------------------------- CLASSES ----------------------------
  // ###############################################################

  private record TeamKey(@NotNull ChatColor color, @NotNull TeamOptions options) {}

  @Getter
  @RequiredArgsConstructor
  public static final class CachedTeamData {
    private final @NotNull String teamName;
    private final @NotNull Object creationPacket;
    private final Map<String, Object> addPacketsCache = new ConcurrentHashMap<>();
    private final Map<String, Object> removePacketsCache = new ConcurrentHashMap<>();

    // ###############################################################
    // --------------------- CONSTRUCTOR METHODS ---------------------
    // ###############################################################

    private CachedTeamData(final @NotNull String teamName, final @NotNull ChatColor color, final @NotNull TeamOptions options) throws ReflectiveOperationException {
      this.teamName = teamName;
      this.creationPacket = TeamPacketFactory.createTeamCreationPacket(teamName, color, options);
    }

    // ###############################################################
    // ----------------------- PUBLIC METHODS ------------------------
    // ###############################################################

    public @NotNull Object getAddEntityPacket(final @NotNull String entityIdentifier) {
      return this.addPacketsCache.computeIfAbsent(entityIdentifier, id -> {
        try {
          return TeamPacketFactory.createAddEntitiesToTeamPacket(teamName, id);
        } catch (ReflectiveOperationException e) {
          throw new RuntimeException("Failed to create add entity packet", e);
        }
      });
    }

    public @NotNull Object getRemoveEntityPacket(final @NotNull String entityIdentifier) {
      return this.removePacketsCache.computeIfAbsent(entityIdentifier, id -> {
        try {
          return TeamPacketFactory.createRemoveEntitiesFromPacket(teamName, id);
        } catch (ReflectiveOperationException e) {
          throw new RuntimeException("Failed to create remove entity packet", e);
        }
      });
    }

  }

}
