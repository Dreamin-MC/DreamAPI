# WorldBorderService

Navigation: [`docs/services/world-service.md`](/docs/services/world-service.md) | [`docs/services/index.md`](/docs/services/index.md) | [`docs/services/persistent-worldborder-service.md`](/docs/services/persistent-worldborder-service.md)

## Purpose

Controls personal/world borders, transitions, pulse effects, and health overlay behavior.

## Source links

- API: [`api/src/main/java/fr/dreamin/dreamapi/api/worldborder/model/WorldBorderService.java`](/api/src/main/java/fr/dreamin/dreamapi/api/worldborder/model/WorldBorderService.java)
- API extension: [`api/src/main/java/fr/dreamin/dreamapi/api/worldborder/model/PersistentWorldBorderService.java`](/api/src/main/java/fr/dreamin/dreamapi/api/worldborder/model/PersistentWorldBorderService.java)
- Core: [`core/src/main/java/fr/dreamin/dreamapi/core/worldborder/service/WorldBorderServiceImpl.java`](/core/src/main/java/fr/dreamin/dreamapi/core/worldborder/service/WorldBorderServiceImpl.java)

## Availability

- Auto service: yes
- Default load: `LoadMode.ALL`

## Method reference

Border access:

- `getWorldBorder(Player)` / `getWorldBorder(World)`: resolves current border model.
- `getWorldBorderData(Player)`: returns persisted border data (if any).

Personal border updates:

- `resetWorldBorderToGlobal(Player)`: removes personal override and restores world border.
- `setBorder(Player, size)`: sets size around default center.
- `setBorder(Player, size, Vector|Location|Position)`: sets size + center.
- `setBorder(Player, size, milliSeconds)`: animated size transition.
- `setBorder(Player, size, time, TimeUnit)`: same with explicit time unit.

Effects:

- `sendRedScreenForSeconds(Player, Duration)`: temporary warning overlay.
- `pulseBorder(Player, minSize, maxSize, pulses, Duration)`: warning-distance pulse sequence.
- `setHealthOverlayEnabled(boolean)` / `isHealthOverlayEnabled()`: low-health overlay system.

## Technical notes

- Core persists border data in player PDC and reuses it on next lookup.
- Health overlay and pulse both modify warning distance; pulse has priority while active.

## Practical example

```java
WorldBorderService borders = DreamPlugin.getService(WorldBorderService.class);
borders.setBorder(player, 200.0);
borders.pulseBorder(player, 20.0, 120.0, 3, Duration.ofSeconds(1));
```

Navigation: [`docs/services/world-service.md`](/docs/services/world-service.md) | [`docs/services/index.md`](/docs/services/index.md) | [`docs/services/persistent-worldborder-service.md`](/docs/services/persistent-worldborder-service.md)

