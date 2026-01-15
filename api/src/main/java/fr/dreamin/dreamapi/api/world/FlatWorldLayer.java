package fr.dreamin.dreamapi.api.world;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public final class FlatWorldLayer {

  private final @NotNull String block;
  private final int height;

  private FlatWorldLayer(final @NotNull String block, final int height) {
    if (height <= 0)
      throw new IllegalArgumentException("Height must be greater than 0");
    this.block = block;
    this.height = height;
  }

  // ###############################################################
  // ----------------------- STATIC METHODS ------------------------
  // ###############################################################

  public static FlatWorldLayer of(final @NotNull String block, final int height) {
    return new FlatWorldLayer(block, height);
  }

}
