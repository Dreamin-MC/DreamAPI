package fr.dreamin.dreamapi.api.item;

import org.jetbrains.annotations.NotNull;

public record ItemTag(@NotNull String name) {

  public static ItemTag of(final @NotNull String name) {
    return new ItemTag(name.toLowerCase());
  }

}
