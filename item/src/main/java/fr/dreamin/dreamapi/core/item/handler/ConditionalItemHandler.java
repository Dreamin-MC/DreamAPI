package fr.dreamin.dreamapi.core.item.handler;

import fr.dreamin.dreamapi.api.item.ItemContext;
import fr.dreamin.dreamapi.api.item.ItemHandler;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@RequiredArgsConstructor
public final class ConditionalItemHandler implements ItemHandler {

  private final ItemHandler delegate;
  private final Predicate<ItemContext> condition;

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public boolean handle(@NotNull ItemContext ctx) {
    if (!this.condition.test(ctx))
      return false;
    return this.delegate.handle(ctx);
  }

  // ###############################################################
  // ----------------------- STATIC METHODS ------------------------
  // ###############################################################

  public static ItemHandler always(final @NotNull ItemHandler handler) {
    return new ConditionalItemHandler(handler, ctx -> true);
  }

  public static ItemHandler when(
    final @NotNull Predicate<ItemContext> condition,
    final @NotNull ItemHandler handler
  ) {
    return new ConditionalItemHandler(handler, condition);
  }

}
