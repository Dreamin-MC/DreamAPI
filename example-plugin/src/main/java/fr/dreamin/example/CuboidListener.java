package fr.dreamin.example;

import fr.dreamin.dreamapi.api.event.annotation.DreamEvent;
import fr.dreamin.dreamapi.core.cuboid.event.CuboidPlayerEnterEvent;
import fr.dreamin.dreamapi.core.cuboid.event.CuboidPlayerLeaveEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@DreamEvent
public final class CuboidListener implements Listener {

  @EventHandler
  private void onPlayerEnter(CuboidPlayerEnterEvent event) {
    event.getEntity().sendMessage(Component.text("enter"));
  }

  @EventHandler
  private void onPlayerLeave(CuboidPlayerLeaveEvent event) {
    event.getPlayer().sendMessage(Component.text("leave"));
  }

}
