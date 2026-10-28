package fr.dreamin.dreamapi.api.glowing.block;

import fr.dreamin.dreamapi.api.glowing.team.TeamOptions;
import lombok.Data;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Data
public final class BlockGlowData {

  private final @NotNull Location location;
  private @NotNull ChatColor color;
  private @NotNull TeamOptions options;

  private @Nullable Integer entityId;
  private @Nullable UUID entityUuid;
  private boolean spawned;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  public BlockGlowData(final @NotNull Location location, final @NotNull ChatColor color, final @NotNull TeamOptions options) {
    this.location = location;
    this.color = color;
    this.options = options;
    this.spawned = false;
  }

}
