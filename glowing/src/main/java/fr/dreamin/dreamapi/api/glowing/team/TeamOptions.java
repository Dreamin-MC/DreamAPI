package fr.dreamin.dreamapi.api.glowing.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class TeamOptions {

  @Builder.Default
  private boolean friendlyFire = false;

  @Builder.Default
  private boolean seeFriendlyInvisibles = false;

  @Builder.Default
  private @NotNull Team.OptionStatus nameTagVisibility = Team.OptionStatus.ALWAYS;

  @Builder.Default
  private @NotNull Team.OptionStatus collisionRule = Team.OptionStatus.ALWAYS;

  // ###############################################################
  // ----------------------- STATIC METHODS ------------------------
  // ###############################################################

  public static TeamOptions noCollision() {
    return TeamOptions.builder()
      .collisionRule(Team.OptionStatus.NEVER)
      .build();
  }

  public static TeamOptions hideNameTag() {
    return TeamOptions.builder()
      .nameTagVisibility(Team.OptionStatus.NEVER)
      .build();
  }

  public static TeamOptions ghostMode() {
    return TeamOptions.builder()
      .collisionRule(Team.OptionStatus.NEVER)
      .nameTagVisibility(Team.OptionStatus.NEVER)
      .build();
  }

}
