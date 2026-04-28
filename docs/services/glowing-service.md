# GlowingService

Navigation: `docs/services/game-service.md` | `docs/services/index.md` | `docs/services/hologram-service.md`

## Purpose

Provides per-viewer glow control for entities and blocks, with timers and animations.

## Source links

- API: `api/src/main/java/fr/dreamin/dreamapi/api/glowing/service/GlowingService.java`
- Core: `core/src/main/java/fr/dreamin/dreamapi/core/glowing/service/GlowingServiceImpl.java`

## Availability

- Auto service: yes
- Default load: `LoadMode.ALL`, `LoadMode.VISUAL`

## Method reference

Entity glow:

- `glowEntity(entity, color, viewers...)`: starts entity glow.
- `glowEntity(entity, color, durationTicks, viewers...)`: starts timed glow.
- `stopEntity(entity, viewers...)`: stops glow for provided viewers, or all viewers if none.

Block glow:

- `glowBlock(block, color, viewers...)`: starts block glow.
- `glowBlock(block, color, durationTicks, viewers...)`: timed block glow.
- `stopBlock(block, viewers...)`: stops block glow.

Animation:

- `glowEntityAnimated(...)` / `glowBlockAnimated(...)`: runs `GlowAnimation`.
- Timed overloads stop automatically after duration.

Conditional and area utilities:

- `glowEntitiesMatching(condition, color, checkInterval, viewer)`: periodic match-based glow.
- `stopConditionalGlowing(viewer)`: stops conditional task.
- `glowEntitiesInRadius(...)` / `glowBlocksInRadius(...)`: bulk area highlighting.
- `glowEntityInCrosshair(...)` / `glowBlockInCrosshair(...)`: line-of-sight utilities.
- `glowVisibleEntities(...)` / `glowVisibleBlocks(...)`: visibility-based bulk glow.

Viewer and query methods:

- `clearForViewer(viewer)`: clears all viewer glow state.
- `reapplyForViewer(viewer)`: re-sends current state to one viewer.
- `reapplyTargetPlayerForAllViewers(target)`: re-sends target-player glow mappings.
- `getGlowingEntities(viewer)` / `getGlowingBlocks(viewer)`: read current viewer state.
- `getStats()`: aggregated runtime counters.

## Technical notes

- Core tracks reverse indexes (viewer -> targets and target -> viewers).
- Join/death/block-break listeners keep state consistent.

## Practical example

```java
GlowingService glowing = DreamPlugin.getService(GlowingService.class);
glowing.glowEntity(targetEntity, ChatColor.AQUA, viewer);
glowing.glowBlock(block, ChatColor.GREEN, 100L, viewer);
```

## Common pitfalls

- Empty viewers in `stop*` methods means "stop for all known viewers".
- Air blocks are ignored by implementation.

Navigation: `docs/services/game-service.md` | `docs/services/index.md` | `docs/services/hologram-service.md`

