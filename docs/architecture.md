# Architecture

## High-level design

DreamAPI is split into four Gradle modules:

- `api`: interfaces, contracts, models, annotations
- `core`: runtime implementations and service infrastructure
- `plugin-base`: `DreamPlugin` base class for auto-bootstrap
- `example-plugin`: integration showcase

## Service lifecycle

DreamAPI services follow `DreamService` lifecycle states:

- `UNLOADED`
- `LOADING`
- `LOADED`
- `RELOADING`
- `CLOSED`
- `FAILED`

Source:
- [`api/src/main/java/fr/dreamin/dreamapi/api/services/DreamService.java`](/api/src/main/java/fr/dreamin/dreamapi/api/services/DreamService.java)

## Auto service registration

Services annotated with `@DreamAutoService` are discovered and loaded with dependency ordering.

Core behavior:
- Topological sort for service dependencies
- Constructor resolution + `@Inject` support
- Bukkit `ServicesManager` registration
- Listener auto-registration for `Listener` services

Source:
- [`api/src/main/java/fr/dreamin/dreamapi/api/services/DreamAutoService.java`](/api/src/main/java/fr/dreamin/dreamapi/api/services/DreamAutoService.java)
- [`core/src/main/java/fr/dreamin/dreamapi/core/service/ServiceAnnotationProcessor.java`](/core/src/main/java/fr/dreamin/dreamapi/core/service/ServiceAnnotationProcessor.java)

## Plugin bootstrap

`DreamPlugin` initializes DreamAPI provider, scans classes, loads services, and sets up command/listener processors.

Source:
- [`plugin-base/src/main/java/fr/dreamin/dreamapi/plugin/DreamPlugin.java`](/plugin-base/src/main/java/fr/dreamin/dreamapi/plugin/DreamPlugin.java)
- [`core/src/main/java/fr/dreamin/dreamapi/core/ApiProviderImpl.java`](/core/src/main/java/fr/dreamin/dreamapi/core/ApiProviderImpl.java)

## Load modes

Service profiles are exposed through `LoadMode` and `@EnableServices`.

Source:
- [`api/src/main/java/fr/dreamin/dreamapi/api/LoadMode.java`](/api/src/main/java/fr/dreamin/dreamapi/api/LoadMode.java)
- [`api/src/main/java/fr/dreamin/dreamapi/api/annotations/EnableServices.java`](/api/src/main/java/fr/dreamin/dreamapi/api/annotations/EnableServices.java)

## Practical Example

```java
import fr.dreamin.dreamapi.api.LoadMode;
import fr.dreamin.dreamapi.api.annotations.EnableServices;
import fr.dreamin.dreamapi.plugin.DreamPlugin;

@EnableServices(mode = {LoadMode.MINIMAL})
public final class MyPlugin extends DreamPlugin {
  @Override
  public void onDreamEnable() {}

  @Override
  public void onDreamDisable() {}
}
```

