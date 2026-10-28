package fr.dreamin.dreamapi.api.gui.event;

import fr.dreamin.dreamapi.api.event.ToolsCancelEvent;
import fr.dreamin.dreamapi.api.gui.model.GuiInterface;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.window.Window;

/**
 * Fired before a GUI window opens.
 */
@Getter
@RequiredArgsConstructor
public final class GuiWindowOpenEvent extends ToolsCancelEvent {

  private final GuiInterface gui;
  private final Player player;
  private final @Nullable Window window;



}

