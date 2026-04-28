# RecipeRegistryService

Navigation: [`docs/services/recipe-category-registry-service.md`](/docs/services/recipe-category-registry-service.md) | [`docs/services/index.md`](/docs/services/index.md) | [`docs/services/recipe-viewer-service.md`](/docs/services/recipe-viewer-service.md)

## Purpose

Registers and manages custom recipes with condition-aware crafting flow.

## Source links

- API: [`api/src/main/java/fr/dreamin/dreamapi/api/recipe/service/RecipeRegistryService.java`](/api/src/main/java/fr/dreamin/dreamapi/api/recipe/service/RecipeRegistryService.java)
- Core: [`core/src/main/java/fr/dreamin/dreamapi/core/recipe/service/RecipeRegistryServiceImpl.java`](/core/src/main/java/fr/dreamin/dreamapi/core/recipe/service/RecipeRegistryServiceImpl.java)

## Availability

- Auto service: yes
- Default load: `LoadMode.ALL`, `LoadMode.DATA`

## Method reference

- `getStorage()`: returns current recipe storage service instance.
- `loadStorage()`: initializes storage and loads persisted recipes.
- `registerRecipe(CustomRecipe)`: adds recipe and injects it into server registry.
- `unregisterRecipe(String key)`: removes recipe by key and unregisters it.
- `getRecipe(String key)`: fetches one recipe by key.
- `getRecipesByTag(RecipeTag tag)`: filtered lookup by tag.
- `getAllRecipes()`: returns all known custom recipes.
- `reload()`: clears/reinjects recipes in server registry.

## Technical notes

- Core implementation hooks craft/smith/furnace events and applies recipe conditions.
- It emits custom pre/post events for integration points.

## Practical example

```java
RecipeRegistryService recipes = DreamPlugin.getService(RecipeRegistryService.class);
recipes.registerRecipe(customRecipe);

var loaded = recipes.getRecipe(customRecipe.getKey());
```

## Common pitfalls

- Reusing recipe keys overrides in-memory mapping.
- Bulk runtime updates should end with `reload()`.

Navigation: [`docs/services/recipe-category-registry-service.md`](/docs/services/recipe-category-registry-service.md) | [`docs/services/index.md`](/docs/services/index.md) | [`docs/services/recipe-viewer-service.md`](/docs/services/recipe-viewer-service.md)

