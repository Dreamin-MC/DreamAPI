package fr.dreamin.dreamapi.api.gui.event;

import fr.dreamin.dreamapi.api.event.ToolsEvent;
import fr.dreamin.dreamapi.api.gui.model.GuiInterface;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.window.Window;

/**
 * Fired after a GUI window closes.
 */
@Getter
@RequiredArgsConstructor
public final class GuiWindowCloseEvent extends ToolsEvent {

  private final GuiInterface gui;
  private final Player player;
  private final @Nullable Window window;
  private final @Nullable Reason reason;

}

