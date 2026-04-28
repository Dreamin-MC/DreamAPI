# GameService

Navigation: `docs/services/dialog-service.md` | `docs/services/index.md` | `docs/services/glowing-service.md`

## Purpose

Handles game-state transitions and per-tick state forwarding.

## Source links

- API: `api/src/main/java/fr/dreamin/dreamapi/api/game/GameService.java`
- Core: `core/src/main/java/fr/dreamin/dreamapi/core/game/GameServiceImpl.java`

## Availability

- Auto service: yes
- Default load: not in built-in `LoadMode` map, include it explicitly if needed

## Method reference

- `switchState(GameState newState)`: exits previous state, unregisters old listeners,
  enters new state, and registers new listeners.
- `getCurrentState()`: returns current active game state, or `null`.
- `tick(int currentTick)`: forwards tick values to current state.

## When to use

- You model gameplay as explicit states (`Lobby`, `Running`, `End`).
- You need deterministic enter/exit transitions.

## Setup checklist

1. Include `GameServiceImpl` in `@EnableServices(include = ...)`.
2. Implement `GameState` with clear `enter/exit/tick` responsibilities.
3. Bridge Bukkit scheduler ticks to `game.tick(...)`.

## Practical example

```java
GameService game = DreamPlugin.getService(GameService.class);
game.switchState(new LobbyState());

new BukkitRunnable() {
  int t = 0;
  @Override public void run() {
    game.tick(t++);
  }
}.runTaskTimer(this, 1L, 1L);
```

## Common pitfalls

- Not including service in `@EnableServices` causes `getService(...)` failures.
- Heavy logic inside `tick(...)` can impact server TPS.

Navigation: `docs/services/dialog-service.md` | `docs/services/index.md` | `docs/services/glowing-service.md`

