# PlayerDebugService (Core)

Navigation: [`docs/services/persistent-worldborder-service.md`](/docs/services/persistent-worldborder-service.md) | [`docs/services/index.md`](/docs/services/index.md) | [`docs/services/team-service.md`](/docs/services/team-service.md)

## Purpose

Tracks debugger-target relations for player-scoped debug flows.

## Source links

- Interface: [`core/src/main/java/fr/dreamin/dreamapi/core/logger/PlayerDebugService.java`](/core/src/main/java/fr/dreamin/dreamapi/core/logger/PlayerDebugService.java)
- Implementation: [`core/src/main/java/fr/dreamin/dreamapi/core/logger/PlayerDebugServiceImpl.java`](/core/src/main/java/fr/dreamin/dreamapi/core/logger/PlayerDebugServiceImpl.java)

## Availability

- Auto service: yes
- Default load: `LoadMode.ALL`, `LoadMode.MINIMAL`, `LoadMode.DEBUG`

## Method reference

- `startDebug(Player target, Player executor)`: starts debug stream from executor to target.
- `stopDebug(Player target, Player executor)`: removes one debug relation.
- `isDebugging(Player target, Player executor)`: checks relation state.
- `getDebuggers(Player target)`: audience of active debuggers for target player.
- `getDebuggers(UUID targetId)`: same lookup using UUID.

## Technical notes

- Audience methods only include online players currently available through Bukkit.

## Practical example

```java
PlayerDebugService playerDebug = DreamPlugin.getService(PlayerDebugService.class);
playerDebug.startDebug(targetPlayer, executorPlayer);
```

Navigation: [`docs/services/persistent-worldborder-service.md`](/docs/services/persistent-worldborder-service.md) | [`docs/services/index.md`](/docs/services/index.md) | [`docs/services/team-service.md`](/docs/services/team-service.md)

