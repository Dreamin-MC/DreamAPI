# Module: api

The `api` module contains all public contracts used by plugin developers.

## What is inside

- Service interfaces (for Bukkit service lookup)
- API models and events
- Shared annotations (`@Inject`, `@EnableServices`, `@DreamAutoService`)
- Utility classes and builders

## Key classes

- [`api/src/main/java/fr/dreamin/dreamapi/api/DreamAPI.java`](api/src/main/java/fr/dreamin/dreamapi/api/DreamAPI.java)
- [`api/src/main/java/fr/dreamin/dreamapi/api/services/DreamService.java`](api/src/main/java/fr/dreamin/dreamapi/api/services/DreamService.java)
- [`api/src/main/java/fr/dreamin/dreamapi/api/services/DreamAutoService.java`](api/src/main/java/fr/dreamin/dreamapi/api/services/DreamAutoService.java)

## Use this module when

You need compile-time contracts without depending directly on core internals.

## Practical Example

```java
import fr.dreamin.dreamapi.api.item.ItemRegistryService;
import fr.dreamin.dreamapi.plugin.DreamPlugin;

ItemRegistryService items = DreamPlugin.getService(ItemRegistryService.class);
boolean exists = items.isRegistered("starter_sword");
```

