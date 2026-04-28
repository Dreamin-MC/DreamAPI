# AnimationService

Navigation: `-` | `docs/services/index.md` | `docs/services/cuboid-service.md`

## Purpose

Creates player cinematics through a fluent builder.

## Source links

- API: `api/src/main/java/fr/dreamin/dreamapi/api/animation/AnimationService.java`
- Core: `core/src/main/java/fr/dreamin/dreamapi/core/animation/AnimationServiceImpl.java`

## Availability

- Auto service: yes (`@DreamAutoService`)
- Default load: `LoadMode.ALL`, `LoadMode.GAMEPLAY`

## Method reference

- `cinematic(String name)`: starts a cinematic builder for a named sequence.

Builder methods worth knowing:

- `camera(start, end, duration[, interpolation])`: appends one camera segment.
- `returnToStart(boolean)`: teleports player back to initial point when finished.
- `endAt(Location)`: explicit final location.
- `gameMode(GameMode)`: force a game mode during the cinematic.
- `copyInventory(boolean)`: controls inventory copy behavior.
- `reconnectBehavior(...)`: behavior when player reconnects during playback.
- `onStart(...)`, `onEnd(...)`, `onSegmentChange(...)`: lifecycle callbacks.
- `build()`: creates the final `Cinematic` object.

## Technical notes

- The service itself builds cinematic objects; your plugin controls when to execute them.
- Segment interpolation type directly affects camera feel (linear vs other modes).

## Practical example

```java
AnimationService animation = DreamPlugin.getService(AnimationService.class);

var cinematic = animation.cinematic("spawn_intro")
  .camera(p1, p2, Duration.ofSeconds(2))
  .camera(p2, p3, Duration.ofSeconds(2))
  .onStart(player -> player.sendMessage("Intro started"))
  .onEnd(player -> player.sendMessage("Intro finished"))
  .build();
```

Navigation: `-` | `docs/services/index.md` | `docs/services/cuboid-service.md`

