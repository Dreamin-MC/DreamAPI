# DreamAPI Documentation

Welcome to the public documentation for DreamAPI.

## Start Here

1. Read the architecture overview: [`docs/architecture.md`](docs/architecture.md)
2. Pick your module entry point:
   - [`docs/modules/api.md`](docs/modules/api.md)
   - [`docs/modules/core.md`](docs/modules/core.md)
   - [`docs/modules/plugin-base.md`](docs/modules/plugin-base.md)
   - [`docs/modules/example-plugin.md`](docs/modules/example-plugin.md)
3. Browse services (folder guide): [`docs/services/README.md`](docs/services/README.md)
4. Copy practical snippets: [`docs/examples/service-recipes.md`](docs/examples/service-recipes.md)

## Audience

This documentation is written for plugin developers integrating DreamAPI on Paper.
It focuses on practical service usage and links every feature to concrete classes in the repository.

## Source of truth

- Root API entrypoint: [`api/src/main/java/fr/dreamin/dreamapi/api/DreamAPI.java`](api/src/main/java/fr/dreamin/dreamapi/api/DreamAPI.java)
- Service loader/runtime: [`core/src/main/java/fr/dreamin/dreamapi/core/service/ServiceAnnotationProcessor.java`](core/src/main/java/fr/dreamin/dreamapi/core/service/ServiceAnnotationProcessor.java)
- Plugin bootstrap base: [`plugin-base/src/main/java/fr/dreamin/dreamapi/plugin/DreamPlugin.java`](plugin-base/src/main/java/fr/dreamin/dreamapi/plugin/DreamPlugin.java)

## Practical Example

```java
import fr.dreamin.dreamapi.plugin.DreamPlugin;
import fr.dreamin.dreamapi.api.glowing.service.GlowingService;

public final class MyPlugin extends DreamPlugin {
  @Override
  public void onDreamEnable() {
    GlowingService glowing = DreamPlugin.getService(GlowingService.class);
    getLogger().info("Service loaded: " + glowing.getClass().getSimpleName());
  }

  @Override
  public void onDreamDisable() {}
}
```

