# RecipeViewerService

Navigation: [`docs/services/recipe-registry-service.md`](docs/services/recipe-registry-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/skin-service.md`](docs/services/skin-service.md)

## Purpose

Defines UI preview operations for custom recipes.

## Source links

- API: [`api/src/main/java/fr/dreamin/dreamapi/api/recipe/service/RecipeViewerService.java`](api/src/main/java/fr/dreamin/dreamapi/api/recipe/service/RecipeViewerService.java)

## Availability

- Auto service: no default implementation in `core`
- Intended as extension point for plugin-specific recipe UIs

## Method reference

- `openFakeViewer(Player, CustomRecipe)`: opens custom/fake GUI recipe preview.
- `openVanillaPreview(Player, CustomRecipe)`: opens vanilla-style recipe preview.

## Technical notes

- Core module contains recipe UI classes, but no direct `RecipeViewerService` registration.
- Public plugin projects should provide their own implementation and registration.

## Practical example

```java
public final class MyRecipeViewerService implements RecipeViewerService {
  @Override
  public void openFakeViewer(Player player, CustomRecipe recipe) {
    // Open plugin-specific fake recipe UI.
  }

  @Override
  public void openVanillaPreview(Player player, CustomRecipe recipe) {
    // Open vanilla-compatible preview UI.
  }
}
```

Navigation: [`docs/services/recipe-registry-service.md`](docs/services/recipe-registry-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/skin-service.md`](docs/services/skin-service.md)

