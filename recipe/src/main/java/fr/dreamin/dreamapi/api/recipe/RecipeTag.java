package fr.dreamin.dreamapi.api.recipe;

import org.jetbrains.annotations.NotNull;

public record RecipeTag(String namespace, String key) {

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public @NotNull String getFullKey() {
    return String.format("%s:%s", this.namespace, this.key);
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof RecipeTag that)) return false;
    return this.namespace.equals(that.namespace) && this.key.equals(that.key);
  }

  @Override
  public String toString() {
    return getFullKey();
  }

  // ###############################################################
  // ----------------------- STATIC METHODS ------------------------
  // ###############################################################

  public static final RecipeTag SHAPED = of("dreamapi", "shaped");
  public static final RecipeTag SHAPELESS = of("dreamapi", "shapeless");
  public static final RecipeTag WEAPON = of("dreamapi", "weapon");
  public static final RecipeTag TOOL = of("dreamapi", "tool");
  public static final RecipeTag MAGIC = of("dreamapi", "magic");
  public static final RecipeTag FOOD = of("dreamapi", "food");
  public static final RecipeTag FURNACE = of("dreamapi", "furnace");
  public static final RecipeTag SMITHING = of("dreamapi", "smithing");

  public static @NotNull RecipeTag of(final @NotNull String namespace, final @NotNull String key) {
    return new RecipeTag(namespace.toLowerCase(), key.toLowerCase());
  }

  public static @NotNull RecipeTag of(final @NotNull String fullKey) {
    final var split = fullKey.split(":", 2);
    if (split.length != 2)
      throw new IllegalArgumentException("Invalid tag: " + fullKey + " (expected namespace:key");

    return of(split[0], split[1]);
  }

}
