package fr.dreamin.dreamapi.api.world;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Builder
@Getter
@RequiredArgsConstructor
public final class BiomeProviderDefinition {

  private final @NotNull String type;
  private final @NotNull List<String> biomes;
  private final @Nullable String seed;

}
