# Module: core

The `core` module provides runtime implementations for API interfaces.

## What is inside

- Concrete service implementations
- Service loader/DI runtime
- NMS-backed visual and tablist utilities
- Recipe, item, world, and localization engines

## Key classes

- `core/src/main/java/fr/dreamin/dreamapi/core/ApiProviderImpl.java`
- `core/src/main/java/fr/dreamin/dreamapi/core/service/ServiceAnnotationProcessor.java`

## Notes

Core contains internal services that are useful but not always exposed via `api` (for example `TeamService`, `DayCycleService`, `PlayerDebugService`).

## Practical Example

```java
import fr.dreamin.dreamapi.core.team.TeamService;
import fr.dreamin.dreamapi.plugin.DreamPlugin;

TeamService teamService = DreamPlugin.getService(TeamService.class);
```

