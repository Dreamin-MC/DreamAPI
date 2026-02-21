package fr.dreamin.dreamapi.api.hologram.event;

import fr.dreamin.dreamapi.api.event.ToolsCancelEvent;
import fr.dreamin.dreamapi.api.hologram.model.HologramConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public final class HologramUpdateEvent extends ToolsCancelEvent {

  private final @NotNull String id;
  private final @NotNull HologramConfig oldConfig;
  private final @NotNull HologramConfig newConfig;

}
