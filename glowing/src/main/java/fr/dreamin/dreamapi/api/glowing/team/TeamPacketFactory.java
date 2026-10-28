package fr.dreamin.dreamapi.api.glowing.team;

import fr.dreamin.dreamapi.api.nms.packet.PacketReflection;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * Factory for creating team-related packets
 */
public final class TeamPacketFactory {

  private TeamPacketFactory() {}

  // ###############################################################
  // -------------------- PUBLIC STATIC METHODS --------------------
  // ###############################################################

  /**
   * Create a team creation packet
   */
  public static @NotNull Object createTeamCreationPacket(final @NotNull String teamName, final @NotNull ChatColor color, final @NotNull TeamOptions options) throws ReflectiveOperationException {
    final var team = PacketReflection.createTeam(teamName);

    PacketReflection.configureTeam(
      team,
      color.getChar(),
      convertCollisionRule(options.getCollisionRule()),
      convertNameTagVisibility(options.getNameTagVisibility()),
      options.isFriendlyFire(),
      options.isSeeFriendlyInvisibles()
    );

    // Set empty prefix/suffix to prevent tab list coloring
    PacketReflection.setTeamPrefixSuffix(team, "", "");

    final var params = PacketReflection.createTeamParams(team);

    // Mode 0 = CREATE
    return PacketReflection.createTeamPacket(teamName, 0, Optional.of(params), Collections.emptyList());
  }

  /**
   * Create a packet to add entities to a team
   */
  public static @NotNull Object createAddEntitiesToTeamPacket(final @NotNull String teamName, final @NotNull String... entityIdentifiers) throws ReflectiveOperationException {
    // Mode 3 = ADD_ENTITIES
    return PacketReflection.createTeamPacket(teamName, 3, Optional.empty(), Arrays.asList(entityIdentifiers));
  }

  /**
   * Create a packet to remove entities from a team
   */
  public static @NotNull Object createRemoveEntitiesFromPacket(final @NotNull String teamName, final @NotNull String... entityIdentifiers) throws ReflectiveOperationException {
    // Mode 4 = REMOVE_ENTITIES
    return PacketReflection.createTeamPacket(teamName, 4, Optional.empty(), Arrays.asList(entityIdentifiers));
  }

  // ###############################################################
  // -------------------- PRIVATE STATIC METHODS -------------------
  // ###############################################################

  /**
   * Convert Bukkit Team.OptionStatus to NMS collision rule name
   */
  private static @NotNull String convertCollisionRule(final @NotNull Team.OptionStatus status) {
    return switch (status) {
      case ALWAYS -> "always";
      case NEVER -> "never";
      case FOR_OTHER_TEAMS -> "pushOtherTeams";
      case FOR_OWN_TEAM -> "pushOwnTeam";
    };
  }

  /**
   * Convert Bukkit Team.OptionStatus to NMS visibility name
   */
  private static @NotNull String convertNameTagVisibility(final @NotNull Team.OptionStatus status) {
    return switch (status) {
      case ALWAYS -> "always";
      case NEVER -> "never";
      case FOR_OTHER_TEAMS -> "hideForOtherTeams";
      case FOR_OWN_TEAM -> "hideForOwnTeam";
    };
  }
}
