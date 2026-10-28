package fr.dreamin.dreamapi.api.item.model;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ItemHandler {

  boolean handle(final @NotNull ItemContext ctx);

}
