package fr.dreamin.dreamapi.core.item;

import fr.dreamin.dreamapi.api.item.*;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public final class RegisteredItemImpl implements RegisteredItem {

  private final String id;
  private final ItemStack item;
  private final Set<ItemTag> tags;
  private final Map<ItemAction, List<ItemHandler>> handlers;

  @Override
  public String id() {
    return this.id;
  }

  @Override
  public ItemStack item() {
    return this.item.clone();
  }

  @Override
  public Set<ItemTag> tags() {
    return this.tags;
  }

  @Override
  public boolean hasTag(@NotNull ItemTag tag) {
    return this.tags.contains(tag);
  }

  @Override
  public void addHandler(@NotNull ItemAction action, @NotNull ItemHandler handler) {
    final var list = this.handlers.get(action);

    if (list == null) this.handlers.put(action, new ArrayList<>(List.of(handler)));
    else list.add(handler);
  }

  @Override
  public void execute(@NotNull ItemAction action, @NotNull ItemContext ctx) {
    final var toExecute = new ArrayList<ItemAction>();
    toExecute.add(action);

    switch (action) {
      case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK, LEFT_CLICK_ENTITY -> toExecute.add(ItemAction.LEFT_CLICK);
      case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK, RIGHT_CLICK_ENTITY -> toExecute.add(ItemAction.RIGHT_CLICK);
      case SHIFT_LEFT_CLICK_AIR, SHIFT_LEFT_CLICK_BLOCK, SHIFT_LEFT_CLICK_ENTITY -> toExecute.add(ItemAction.SHIFT_LEFT_CLICK);
      case SHIFT_RIGHT_CLICK_AIR, SHIFT_RIGHT_CLICK_BLOCK, SHIFT_RIGHT_CLICK_ENTITY -> toExecute.add(ItemAction.SHIFT_RIGHT_CLICK);
      default -> {}
    }

    for (ItemAction a : toExecute) {
      final var list = this.handlers.get(a);
      if (list == null) continue;

      for (final var handler : list) {
        boolean stop = handler.handle(ctx);
        if (stop) break;

        if (ctx.event() instanceof Cancellable cancellable && cancellable.isCancelled())
          break;
      }
    }
  }
}
