package fr.dreamin.dreamapi.api.glowing.entity;

import fr.dreamin.dreamapi.api.glowing.team.TeamOptions;
import lombok.Data;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public final class EntityGlowData {

  private final int entityId;
  private final @NotNull String entityIdentifier;

  private @Nullable ChatColor color;
  private @NotNull TeamOptions options;
  private byte otherFlags;
  private boolean enabled;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  public EntityGlowData(final int entityId, final @NotNull String entityIdentifier, final @NotNull ChatColor color, final @NotNull TeamOptions options, final byte otherFlags) {
    this.entityId = entityId;
    this.entityIdentifier = entityIdentifier;
    this.color = color;
    this.options = options;
    this.otherFlags = otherFlags;
    this.enabled = true;
  }

}
