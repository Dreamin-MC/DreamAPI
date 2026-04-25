package fr.dreamin.example;

import fr.dreamin.dreamapi.api.event.annotation.DreamEvent;
import fr.dreamin.dreamapi.core.cuboid.event.CuboidEntityEnterEvent;
import fr.dreamin.dreamapi.core.cuboid.event.CuboidEntityLeaveEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@DreamEvent
public final class CuboidListener implements Listener {

  @EventHandler
  private void onPlayerEnter(CuboidEntityEnterEvent event) {
    event.getPlayer().sendMessage(Component.text("enter"));
  }

  @EventHandler
  private void onPlayerLeave(CuboidEntityLeaveEvent event) {
    event.getPlayer().sendMessage(Component.text("leave"));
  }

}
