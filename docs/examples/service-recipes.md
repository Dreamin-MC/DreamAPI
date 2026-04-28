# Service Recipes

This page contains practical usage recipes combining multiple services.

## Recipe 1: Safe world clone and border setup

```java
WorldService worlds = DreamPlugin.getService(WorldService.class);
WorldBorderService borders = DreamPlugin.getService(WorldBorderService.class);

worlds.cloneWorld("arena_template", "arena_match", world -> {
  if (world == null) {
    getLogger().warning("Clone failed");
    return;
  }

  Player player = Bukkit.getPlayerExact("Scraven");
  if (player != null && player.getWorld().equals(world)) {
    borders.setBorder(player, 300.0);
  }
});
```

## Recipe 2: Localized glowing feedback

```java
LangService lang = DreamPlugin.getService(LangService.class);
GlowingService glowing = DreamPlugin.getService(GlowingService.class);

String message = lang.getTranslation(player, "combat.target_marked")
  .orElse("Target marked");

glowing.glowEntity(target, ChatColor.RED, 60L, player);
player.sendMessage(message);
```

## Recipe 3: Register and validate custom recipe startup

```java
RecipeRegistryService recipes = DreamPlugin.getService(RecipeRegistryService.class);
DebugService debug = DreamPlugin.getService(DebugService.class);

recipes.registerRecipe(customRecipe);
if (recipes.getRecipe(customRecipe.getKey()) == null) {
  debug.setCategory("recipe", true);
  getLogger().warning("Custom recipe registration failed: " + customRecipe.getKey());
}
```

## Recipe 4: Per-player UI cleanup on disable

```java
@Override
public void onDreamDisable() {
  VisualService visual = DreamPlugin.getService(VisualService.class);
  GlowingService glowing = DreamPlugin.getService(GlowingService.class);

  for (Player online : Bukkit.getOnlinePlayers()) {
    try {
      visual.clearForViewer(online);
    } catch (ReflectiveOperationException ex) {
      getLogger().warning("Visual cleanup failed for " + online.getName());
    }
    glowing.clearForViewer(online);
  }
}
```

