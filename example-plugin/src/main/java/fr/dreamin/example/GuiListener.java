package fr.dreamin.example;

import fr.dreamin.dreamapi.api.event.annotation.DreamEvent;
import fr.dreamin.dreamapi.api.gui.event.GuiWindowCloseEvent;
import fr.dreamin.dreamapi.api.gui.event.GuiWindowOpenEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

@DreamEvent
public final class GuiListener implements Listener {

  @EventHandler
  private void onGuiOpen(final @NotNull GuiWindowOpenEvent event) {
    event.getPlayer().sendMessage(Component.text("open"));
  }

  @EventHandler
  private void onGuiClose(final @NotNull GuiWindowCloseEvent event) {
    event.getPlayer().sendMessage(Component.text("close"));
  }

}
