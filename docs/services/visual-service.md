# VisualService

Navigation: [`docs/services/tablist-service.md`](docs/services/tablist-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/world-service.md`](docs/services/world-service.md)

## Purpose

Provides packet-driven client-only visuals (fake entities, fake blocks, per-player time).

## Source links

- API: [`api/src/main/java/fr/dreamin/dreamapi/api/nms/visual/service/VisualService.java`](api/src/main/java/fr/dreamin/dreamapi/api/nms/visual/service/VisualService.java)
- Core: [`core/src/main/java/fr/dreamin/dreamapi/core/nms/visual/service/VisualServiceImpl.java`](core/src/main/java/fr/dreamin/dreamapi/core/nms/visual/service/VisualServiceImpl.java)

## Availability

- Auto service: yes
- Default load: `LoadMode.ALL`, `LoadMode.VISUAL`

## Method reference

Fake entities:

- `spawnFakeEntity(type, location, viewers...)`: sends fake spawn packet and returns handle.
- `removeFakeEntity(entity, viewers...)`: removes fake entity for viewers.
- `updateFakeEntity(entity, metadataMutator, viewers...)`: applies metadata updates.

Fake blocks:

- `showFakeBlock(location, material, viewers...)`: sends fake block state.
- `hideFakeBlock(location, viewers...)`: restores real block for viewers.

Player time:

- `setFrozenTime(player, time)`: sets fixed client-side time.
- `resetTime(player)`: restores normal time sync.

Viewer state and queries:

- `clearForViewer(viewer)`: removes all fake visuals for one viewer.
- `reapplyForViewer(viewer)`: re-sends tracked visuals.
- `getFakeEntities(viewer)`: current fake entity handles.
- `getFakeBlocks(viewer)`: current fake block map.

## Technical notes

- Methods can throw `ReflectiveOperationException`; handle failures safely.
- In current core implementation, several fake metadata setters are placeholders.

## Practical example

```java
VisualService visual = DreamPlugin.getService(VisualService.class);
visual.showFakeBlock(player.getLocation(), Material.DIAMOND_BLOCK, player);
visual.setFrozenTime(player, 18000L);
```

Navigation: [`docs/services/tablist-service.md`](docs/services/tablist-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/world-service.md`](docs/services/world-service.md)

