package fr.dreamin.example.item;

import fr.dreamin.dreamapi.api.item.model.ItemAction;
import fr.dreamin.dreamapi.api.item.model.ItemContext;
import fr.dreamin.dreamapi.api.item.annotations.OnDreamItemUse;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public final class TestHandlers {

  static boolean called = false;

  @OnDreamItemUse(
    tags = {"TEST"},
    action = ItemAction.RIGHT_CLICK_ENTITY
  )
  public boolean onUse(final @NotNull ItemContext ctx) {
    called = true;

    ctx.player().sendMessage(Component.text("Test sword used!"));

    return true;
  }

//  @OnDreamItemUse(
//    tags = {"weapons"},
//    action = ItemAction.LEFT_CLICK
//  )
//  public boolean onUse(final @NotNull ItemContext ctx) {
//
//  }

}
