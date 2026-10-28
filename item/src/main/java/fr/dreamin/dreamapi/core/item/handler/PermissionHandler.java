package fr.dreamin.dreamapi.core.item.handler;

import fr.dreamin.dreamapi.api.item.ItemContext;
import fr.dreamin.dreamapi.api.item.ItemHandler;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class PermissionHandler implements ItemHandler {

  private final String permission;
  private Component noPermissionMessage;

  public PermissionHandler (final @NotNull String permission, final @NotNull Component message) {
    this.permission = permission;
    this.noPermissionMessage = message;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public boolean handle(@NotNull ItemContext ctx) {
    if (!ctx.player().hasPermission(this.permission)) {

      if (ctx.event() instanceof Cancellable cancellable)
        cancellable.setCancelled(true);

      if (this.noPermissionMessage != null)
        ctx.player().sendMessage(this.noPermissionMessage);

      return true;
    }
    return false;
  }
}
