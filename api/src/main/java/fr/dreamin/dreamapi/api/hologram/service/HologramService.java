package fr.dreamin.dreamapi.api.hologram.service;

import fr.dreamin.dreamapi.api.hologram.model.HologramBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface HologramService {

//  @NotNull HologramBuilder.HologramBuilderBuilder builder(final @NotNull String id);
  @Nullable HologramBuilder getHologram(final @NotNull String id);
  void deleteHologram(final @NotNull String id);
  void deleteHologram(final @NotNull HologramBuilder hologramBuilder);
  void deleteAllHolograms();
  @NotNull Collection<HologramBuilder> getAllHolograms();

  void register(final @NotNull HologramBuilder hologram);

}
