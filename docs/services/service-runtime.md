# Service Runtime Reference

Navigation: [`docs/services/day-cycle-service.md`](/docs/services/day-cycle-service.md) | [`docs/services/index.md`](/docs/services/index.md) | `-`

## Purpose

Explains service loading internals (`@DreamAutoService`, injection, lifecycle).

## Source links

- Annotation: [`api/src/main/java/fr/dreamin/dreamapi/api/services/DreamAutoService.java`](/api/src/main/java/fr/dreamin/dreamapi/api/services/DreamAutoService.java)
- Lifecycle interface: [`api/src/main/java/fr/dreamin/dreamapi/api/services/DreamService.java`](/api/src/main/java/fr/dreamin/dreamapi/api/services/DreamService.java)
- Loader: [`core/src/main/java/fr/dreamin/dreamapi/core/service/ServiceAnnotationProcessor.java`](/core/src/main/java/fr/dreamin/dreamapi/core/service/ServiceAnnotationProcessor.java)

## Main behaviors

- Dependency-aware load order via topological sort.
- Constructor resolution with optional `@Inject`.
- Registration in Bukkit `ServicesManager`.
- Listener auto-registration for `Listener` services.
- Lifecycle states: `LOADING`, `LOADED`, `RELOADING`, `CLOSED`, `FAILED`.

## Public runtime methods

- `process()`: scans and loads all annotated services.
- `loadServiceFromClass(Class<?>)`: loads one implementation class.
- `loadService(DreamService)`: executes `onLoad` and status updates.
- `reloadService(DreamService)` / `reloadAllServices()`
- `closeService(DreamService)` / `closeAllServices()`
- `resetService(DreamService)`
- `createInjectedInstance(Class<T>)`: creates non-service class with same injection logic.
- `getDreamService(Class<T>)`: returns loaded implementation instance.
- `getAllLoadedServices()`: immutable view of loaded map.
- `isLoaded(Class<? extends DreamService>)`: checks loaded state.
- `exportServiceDiagram(File)`: writes PlantUML-style diagram snapshot.

## Practical example

```java
ServiceAnnotationProcessor services = DreamPlugin.getServiceManager();
services.loadServiceFromClass(MyCustomServiceImpl.class);
```

Navigation: [`docs/services/day-cycle-service.md`](/docs/services/day-cycle-service.md) | [`docs/services/index.md`](/docs/services/index.md) | `-`

