package fr.dreamin.dreamapi.api.hologram.model.line;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class LineConfig {

  @Builder.Default
  private final double height = 0.3;

  @Nullable
  private final Integer updateIntervalTicks;

  @Builder.Default
  private final double offsetX = 0.0;

  @Builder.Default
  private final double offsetY = 0.0;

  @Builder.Default
  private final double offsetZ = 0.0;

  @Builder.Default
  private final boolean clickable = false;

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public int resolveInterval(final int globalInterval) {
    return this.updateIntervalTicks != null ? this.updateIntervalTicks : globalInterval;
  }

}
