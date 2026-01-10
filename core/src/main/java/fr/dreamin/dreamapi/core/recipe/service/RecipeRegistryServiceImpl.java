package fr.dreamin.dreamapi.core.recipe.service;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.recipe.*;
import fr.dreamin.dreamapi.api.recipe.condition.RecipeConditionContext;
import fr.dreamin.dreamapi.api.recipe.condition.RecipeConditionResult;
import fr.dreamin.dreamapi.api.recipe.service.RecipeRegistryService;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.core.recipe.event.*;
import fr.dreamin.dreamapi.api.recipe.storage.RecipeStorageService;
import fr.dreamin.dreamapi.core.recipe.impl.RecipeRegistryImpl;
import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import org.bukkit.Keyed;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@DreamAutoService(RecipeRegistryService.class)
public final class RecipeRegistryServiceImpl implements RecipeRegistryService, DreamService, Listener {

  private final Map<String, CustomRecipe> recipes = new HashMap<>();
  private final RecipeRegistry registry = new RecipeRegistryImpl();

  private RecipeStorageService storage;

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void onReload() {
    reload();
  }

  @Override
  public void onReset() {
    reload();
  }

  @Override
  public void onClose() {
    this.registry.clearAll();
    this.recipes.clear();
  }

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public RecipeStorageService getStorage() {
    return this.storage;
  }

  @Override
  public void loadStorage() {
    this.storage = new RecipeStorageService(DreamAPI.getAPI().plugin());

    this.storage.loadAllRecipes(recipe -> {
      this.recipes.put(recipe.getKey(), recipe);
      this.registry.injectIntoServer(recipe);
    });
  }

  @Override
  public void registerRecipe(@NotNull CustomRecipe recipe) {
    this.recipes.put(recipe.getKey(), recipe);
    this.registry.injectIntoServer(recipe);
  }

  @Override
  public boolean unregisterRecipe(@NotNull String key) {
    final var removed = this.recipes.remove(key);
    if (removed != null) {
      this.registry.removeFromServer(key);
      return true;
    }
    return false;
  }
  @Override
  public @Nullable CustomRecipe getRecipe(@NotNull String key) {
    return this.recipes.get(key);
  }

  @Override
  public @NotNull List<CustomRecipe> getRecipesByTag(@NotNull RecipeTag tag) {
    return this.recipes.values().stream()
      .filter(r -> r.getTags().contains(tag))
      .toList();
  }


  @Override
  public @NotNull Collection<CustomRecipe> getAllRecipes() {
    return List.copyOf(this.recipes.values());
  }

  @Override
  public void reload() {
    this.registry.clearAll();
    this.recipes.values().forEach(registry::injectIntoServer);
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private @NotNull RecipeConditionContext createContext(
    final @NotNull Player player,
    final @Nullable InventoryView view,
    final @NotNull CustomRecipe recipe,
    final @NotNull RecipeCraftingType craftingType
  ) {
    return new RecipeConditionContext(
      player,
      view,
      recipe.getType(),
      craftingType
    );
  }

  private @NotNull RecipeConditionResult testCondition(
    final @NotNull CustomRecipe recipe,
    final @NotNull Player player,
    final @Nullable InventoryView view,
    final @NotNull RecipeCraftingType craftingType
  ) {
    final var context = createContext(player, view, recipe, craftingType);

    return recipe.getPlayerCondition()
      .map(condition -> condition.test(context))
      .orElse(RecipeConditionResult.allow());
  }

  private RecipeCraftingType getCraftingType(final @NotNull InventoryView view) {
    final var top = view.getTopInventory();

    return switch (top.getType()) {
      case CRAFTING -> RecipeCraftingType.PLAYER;
      case WORKBENCH -> RecipeCraftingType.WORKBENCH;
      case CRAFTER -> RecipeCraftingType.CRAFTER;
      default -> RecipeCraftingType.ALL;
    };
  }

  // ###############################################################
  // ---------------------- LISTENER METHODS -----------------------
  // ###############################################################

  @EventHandler
  private void onPrepareCraft(final @NotNull PrepareItemCraftEvent event) {
    if (!(event.getView().getPlayer() instanceof Player player)) return;
    final var bukkitRecipe = event.getRecipe();
    if (!(bukkitRecipe instanceof Keyed keyed)) return;

    final var logicalKey = keyed.getKey().getKey();
    final var recipe = this.recipes.get(logicalKey);
    if (recipe == null) return;

    final var craftingType = getCraftingType(event.getView());
    if (!recipe.isCraftingTypeAllowed(craftingType)) {
      event.getInventory().setResult(null);
      return;
    }

    final var condition = testCondition(recipe, player, event.getView(), craftingType);
    if (!condition.allowed()) {
      var lore = recipe.getResult().lore();
      if (lore == null) lore = new ArrayList<>();

      lore.addAll(condition.message());

      event.getInventory().setResult(
        new ItemBuilder(recipe.getResult())
          .setLore(lore)
          .build()
      );
      return;
    }

    if (!new CustomCraftPreEvent(player, recipe).callEvent())
      event.getInventory().setResult(null);
  }

  @EventHandler
  private void onCraft(final @NotNull CraftItemEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) return;
    final var bukkitRecipe = event.getRecipe();
    if (!(bukkitRecipe instanceof Keyed keyed)) return;

    final var logicalKey = keyed.getKey().getKey();
    final var recipe = this.recipes.get(logicalKey);
    if (recipe == null) return;

    final var craftingType = getCraftingType(event.getView());
    if (!recipe.isCraftingTypeAllowed(craftingType)) {
      event.setCancelled(true);
      return;
    }

    final var condition = testCondition(recipe, player, event.getView(), craftingType);
    if (!condition.allowed()) {
      event.setCancelled(true);
      return;
    }

    if (!new CustomCraftEvent(player, recipe, recipe.getResult().clone()).callEvent())
      event.setCancelled(true);
  }

  @EventHandler
  private void onPreFurnaceSmelt(final @NotNull FurnaceStartSmeltEvent event) {
    final var keyed = event.getRecipe();
    final var recipe = this.recipes.get(keyed.getKey().getKey());
    if (recipe == null) return;

    if (!new CustomFurnacePreEvent(recipe).callEvent())
      event.setTotalCookTime(0);
  }

  @EventHandler
  private void onFurnaceSmelt(final @NotNull FurnaceSmeltEvent event) {
    final var r = event.getRecipe();
    if (!(r instanceof Keyed keyed)) return;

    final var recipe = this.recipes.get(keyed.getKey().getKey());
    if (recipe == null) return;

    new CustomFurnaceEvent(recipe).callEvent();

  }

  @EventHandler
  private void onPrepareSmith(final @NotNull PrepareSmithingEvent event) {
    if (!(event.getView().getPlayer() instanceof Player player)) return;

    final var r = event.getInventory().getRecipe();
    if (!(r instanceof Keyed keyed)) return;

    final var recipe = this.recipes.get(keyed.getKey().getKey());
    if (recipe == null) return;

    final var condition = testCondition(recipe, player, event.getView(), RecipeCraftingType.SMITHING);
    if (condition.allowed()) {
      var lore = recipe.getResult().lore();
      if (lore == null) lore = new ArrayList<>();
      lore.addAll(condition.message());
      event.setResult(new ItemBuilder(recipe.getResult()).setLore(lore).build());
      return;
    }

    if (!new CustomSmithingPreEvent(player, recipe).callEvent())
      event.setResult(null);
  }

  @EventHandler
  private void onSmith(final @NotNull SmithItemEvent event) {
    if (!(event.getView().getPlayer() instanceof Player player)) return;

    final var r = event.getInventory().getRecipe();
    if (!(r instanceof Keyed keyed)) return;

    final var recipe = this.recipes.get(keyed.getKey().getKey());
    if (recipe == null) return;

    final var condition = testCondition(recipe, player, event.getView(), RecipeCraftingType.SMITHING);
    if (!condition.allowed()) {
      event.setResult(Event.Result.DENY);
      return;
    }

    if (!new CustomSmithingEvent(player, recipe).callEvent())
      event.setResult(Event.Result.DENY);

  }

}
