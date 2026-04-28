# PersistentWorldBorderService

Navigation: [`docs/services/worldborder-service.md`](docs/services/worldborder-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/player-debug-service.md`](docs/services/player-debug-service.md)

## Purpose

Specialized type marker for persistent world-border behavior.

## Source links

- API: [`api/src/main/java/fr/dreamin/dreamapi/api/worldborder/model/PersistentWorldBorderService.java`](api/src/main/java/fr/dreamin/dreamapi/api/worldborder/model/PersistentWorldBorderService.java)
- Base API: [`api/src/main/java/fr/dreamin/dreamapi/api/worldborder/model/WorldBorderService.java`](api/src/main/java/fr/dreamin/dreamapi/api/worldborder/model/WorldBorderService.java)
- Core implementation: [`core/src/main/java/fr/dreamin/dreamapi/core/worldborder/service/WorldBorderServiceImpl.java`](core/src/main/java/fr/dreamin/dreamapi/core/worldborder/service/WorldBorderServiceImpl.java)

## Availability

- Inherits all `WorldBorderService` methods
- Implemented by the same core service implementation

## Method reference

- No additional methods are declared in current interface.
- Use `WorldBorderService` method set for runtime behavior.

## Technical notes

- Core persistence is handled in player PDC via `WorldBorderData`.

## Practical example

```java
WorldBorderService service = DreamPlugin.getService(WorldBorderService.class);
service.setBorder(player, 150.0);
```

Navigation: [`docs/services/worldborder-service.md`](docs/services/worldborder-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/player-debug-service.md`](docs/services/player-debug-service.md)

