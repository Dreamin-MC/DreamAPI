# HologramService

Navigation: [`docs/services/glowing-service.md`](/docs/services/glowing-service.md) | [`docs/services/index.md`](/docs/services/index.md) | [`docs/services/item-registry-service.md`](/docs/services/item-registry-service.md)

## Purpose

Creates, spawns, persists, and manages holograms.

## Source links

- API: [`api/src/main/java/fr/dreamin/dreamapi/api/hologram/service/HologramService.java`](/api/src/main/java/fr/dreamin/dreamapi/api/hologram/service/HologramService.java)
- Core: [`core/src/main/java/fr/dreamin/dreamapi/core/hologram/service/HologramServiceImpl.java`](/core/src/main/java/fr/dreamin/dreamapi/core/hologram/service/HologramServiceImpl.java)

## Availability

- Auto service: yes
- Default load: `LoadMode.ALL`, `LoadMode.DATA`

## Method reference

Creation and spawn:

- `create(String id)`: creates and registers an in-memory hologram.
- `spawn(String id, Location)` / `spawn(Hologram, Location)`: spawns hologram instance.

Lookup and lifecycle:

- `getHologram(String id)`: optional lookup by id.
- `delete(String id)` / `delete(Hologram)`: despawns and removes one hologram.
- `deleteAll()`: removes all holograms.
- `register(Hologram)`: registers externally created hologram instance.
- `getAll()`: returns all registered holograms.

Persistence:

- `save(String id, File)` / `save(Hologram, File)`: explicit path save.
- `save(String id)` / `save(Hologram)`: default folder save.
- `load(File)`: loads one hologram file.
- `loadAll()`: loads all hologram files from default data folder.
- `shutdown()`: stops animation engine and despawns managed holograms.

## Technical notes

- Default persistence path is `<pluginData>/holograms`.
- Saves trigger `HologramSaveEvent` before write.

## Practical example

```java
HologramService holograms = DreamPlugin.getService(HologramService.class);
var holo = holograms.create("welcome");
holograms.spawn(holo, player.getLocation());
```

Navigation: [`docs/services/glowing-service.md`](/docs/services/glowing-service.md) | [`docs/services/index.md`](/docs/services/index.md) | [`docs/services/item-registry-service.md`](/docs/services/item-registry-service.md)

