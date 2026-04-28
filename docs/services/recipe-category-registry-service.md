# RecipeCategoryRegistryService

Navigation: `docs/services/luckperms-service.md` | `docs/services/index.md` | `docs/services/recipe-registry-service.md`

## Purpose

Stores and resolves recipe categories used by recipe features.

## Source links

- API: `api/src/main/java/fr/dreamin/dreamapi/api/recipe/service/RecipeCategoryRegistryService.java`
- Core: `core/src/main/java/fr/dreamin/dreamapi/core/recipe/service/RecipeCategoryRegistryServiceImpl.java`

## Availability

- Auto service: yes
- Default load: `LoadMode.ALL`, `LoadMode.DATA`

## Method reference

- `registerCategory(RecipeCategory)`: adds or replaces one category id.
- `getCategory(String id)`: category lookup by id (case-insensitive in core impl).
- `getAllCategories()`: returns all registered categories.

## Technical notes

- Core registers default categories in `onLoad(...)`.

## Practical example

```java
RecipeCategoryRegistryService categories = DreamPlugin.getService(RecipeCategoryRegistryService.class);
var weapons = categories.getCategory("weapons");
```

Navigation: `docs/services/luckperms-service.md` | `docs/services/index.md` | `docs/services/recipe-registry-service.md`

