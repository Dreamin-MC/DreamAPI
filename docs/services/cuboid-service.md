# CuboidService

Navigation: [`docs/services/animation-service.md`](docs/services/animation-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/debug-service.md`](docs/services/debug-service.md)

## Purpose

Tracks cuboid zones and emits enter/leave logic for moving entities.

## Source links

- API: [`api/src/main/java/fr/dreamin/dreamapi/api/cuboid/service/CuboidService.java`](api/src/main/java/fr/dreamin/dreamapi/api/cuboid/service/CuboidService.java)
- Core: [`core/src/main/java/fr/dreamin/dreamapi/core/cuboid/service/CuboidServiceImpl.java`](core/src/main/java/fr/dreamin/dreamapi/core/cuboid/service/CuboidServiceImpl.java)

## Availability

- Auto service: yes
- Default load: `LoadMode.ALL`, `LoadMode.VISUAL`

## Method reference

- `register(Cuboid cuboid)`: adds a cuboid to active tracked regions.
- `unregister(Cuboid cuboid)`: removes a cuboid from tracking.
- `isAutoRegister()`: returns automatic registration mode flag.
- `autoRegister(boolean value)`: sets auto-registration mode.
- `clear()`: removes all tracked cuboids and per-entity cache.
- `getCuboids()`: returns the full tracked cuboid set.
- `getCuboidsOf(UUID uuid)`: returns cuboids currently associated with one entity/player.

## Technical notes

- Implementation listens to move and teleport events.
- It dispatches dedicated events like `CuboidPlayerEnterEvent` and can cancel movement
  if enter/leave event handlers cancel the flow.

## Practical example

```java
CuboidService cuboids = DreamPlugin.getService(CuboidService.class);
cuboids.register(spawnCuboid);

if (cuboids.getCuboids().contains(spawnCuboid)) {
  getLogger().info("Spawn cuboid is active");
}
```

Navigation: [`docs/services/animation-service.md`](docs/services/animation-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/debug-service.md`](docs/services/debug-service.md)

