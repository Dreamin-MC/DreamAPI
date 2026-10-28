package fr.dreamin.dreamapi.api.item;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ItemHandler {

  boolean handle(final @NotNull ItemContext ctx);

}
