# WorldService

Navigation: [`docs/services/visual-service.md`](/docs/services/visual-service.md) | [`docs/services/index.md`](/docs/services/index.md) | [`docs/services/worldborder-service.md`](/docs/services/worldborder-service.md)

## Purpose

Manages world clone/reset/delete/create pipelines and temporary-world lifecycle.

## Source links

- API: [`api/src/main/java/fr/dreamin/dreamapi/api/world/service/WorldService.java`](/api/src/main/java/fr/dreamin/dreamapi/api/world/service/WorldService.java)
- Core: [`core/src/main/java/fr/dreamin/dreamapi/core/world/impl/WorldServiceImpl.java`](/core/src/main/java/fr/dreamin/dreamapi/core/world/impl/WorldServiceImpl.java)

## Availability

- Auto service: yes
- Default load: `LoadMode.ALL`, `LoadMode.GAMEPLAY`

## Method reference

World operations:

- `resetWorld(worldLabel, templateLabel, callback)`: replaces world with template copy.
- `cloneWorld(sourceWorldLabel, cloneWorldLabel, callback)`: clones one world to a new label.
- `deleteWorld(worldLabel, callback)`: unloads and deletes world directory.
- `createWorld(WorldDefinition, callback)`: creates world from definition object.

Temporary world management:

- `markTemporary(worldLabel, Duration)`: schedules auto-delete.
- `cancelAutoDelete(worldLabel)`: removes scheduled auto-delete task.
- `extendAutoDelete(worldLabel, Duration)`: reschedules auto-delete.
- `isTemporary(worldLabel)`: checks temporary scheduling state.

## Technical notes

- File operations are async; world creation/loading callbacks return on main thread.
- Template reset expects `<worldContainer>/templates/<templateLabel>`.

## Practical example

```java
WorldService worldService = DreamPlugin.getService(WorldService.class);
worldService.cloneWorld("arena_template", "arena_live", world -> {
  if (world != null) {
    getLogger().info("World cloned: " + world.getName());
  }
});
```

## Common pitfalls

- Using world methods without callback checks can hide async failures.
- Cloning/deleting active player worlds without flow checks can break gameplay.

Navigation: [`docs/services/visual-service.md`](/docs/services/visual-service.md) | [`docs/services/index.md`](/docs/services/index.md) | [`docs/services/worldborder-service.md`](/docs/services/worldborder-service.md)

