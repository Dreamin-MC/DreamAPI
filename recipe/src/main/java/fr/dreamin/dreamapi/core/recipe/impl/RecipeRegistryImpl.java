package fr.dreamin.dreamapi.core.recipe.impl;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.recipe.CustomRecipe;
import fr.dreamin.dreamapi.api.recipe.RecipeRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class RecipeRegistryImpl implements RecipeRegistry {

  private final Map<String, NamespacedKey> registered = new HashMap<>();

  @Override
  public void injectIntoServer(@NotNull CustomRecipe recipe) {
    final var key = new NamespacedKey(DreamAPI.getAPI().plugin(), recipe.getKey());
    final var bukkit = recipe.toBukkitRecipe(key);
    Bukkit.addRecipe(bukkit);
    this.registered.put(recipe.getKey(), key);
  }

  @Override
  public void removeFromServer(@NotNull String key) {
    final var ns = this.registered.remove(key);
    if (ns == null) return;

    final var it = Bukkit.recipeIterator();
    while (it.hasNext()) {
      final var r = it.next();
      if (r instanceof Keyed keyed && keyed.getKey().equals(ns))
        it.remove();
    }
  }

  @Override
  public void clearAll() {
    if (this.registered.isEmpty()) return;

    final var it = Bukkit.recipeIterator();
    while (it.hasNext()) {
      final var r = it.next();
      if (r instanceof Keyed keyed && this.registered.containsValue(keyed.getKey()))
        it.remove();
    }
    this.registered.clear();
  }
}
