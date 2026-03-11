package fr.dreamin.dreamapi.api.hologram.model.line;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.dreamin.dreamapi.api.hologram.model.animation.HologramAnimation;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public final class LineElement {

  private final @NotNull HologramLine line;

  private final @Nullable HologramAnimation animation;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  @JsonCreator
  public LineElement(
    @JsonProperty("line") final @NotNull HologramLine line,
    @JsonProperty("animation") final @Nullable HologramAnimation animation
  ) {
    this.line = line;
    this.animation = animation;
  }

}
