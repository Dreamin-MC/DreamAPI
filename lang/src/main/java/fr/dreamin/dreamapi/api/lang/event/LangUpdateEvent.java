package fr.dreamin.dreamapi.api.lang.event;

import fr.dreamin.dreamapi.api.event.ToolsEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public final class LangUpdateEvent extends ToolsEvent {
  private final @NotNull Player player;
  private final @NotNull Locale locale;
}
