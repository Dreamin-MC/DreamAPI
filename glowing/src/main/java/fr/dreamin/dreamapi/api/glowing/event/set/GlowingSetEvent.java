package fr.dreamin.dreamapi.api.glowing.event.set;

import fr.dreamin.dreamapi.api.event.ToolsCancelEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class GlowingSetEvent extends ToolsCancelEvent {

  private final @NotNull Player viewer;

  @Setter
  private @NotNull ChatColor color;

}
