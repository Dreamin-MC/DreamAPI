package fr.dreamin.dreamapi.api.hologram.event;

import fr.dreamin.dreamapi.api.event.ToolsCancelEvent;
import fr.dreamin.dreamapi.api.hologram.model.Hologram;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Getter
@RequiredArgsConstructor
public final class HologramSaveEvent extends ToolsCancelEvent {

  private final @NotNull String hologramId;
  private final @NotNull Hologram hologram;
  private final @NotNull File file;

}
