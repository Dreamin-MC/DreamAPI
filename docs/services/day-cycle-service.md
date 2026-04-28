# DayCycleService (Core)

Navigation: [`docs/services/team-service.md`](docs/services/team-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/service-runtime.md`](docs/services/service-runtime.md)

## Purpose

Manages simulated day/night cycles per world.

## Source links

- Interface: [`core/src/main/java/fr/dreamin/dreamapi/core/time/day/impl/DayCycleService.java`](core/src/main/java/fr/dreamin/dreamapi/core/time/day/impl/DayCycleService.java)
- Implementation: [`core/src/main/java/fr/dreamin/dreamapi/core/time/day/impl/DayCycleServiceImpl.java`](core/src/main/java/fr/dreamin/dreamapi/core/time/day/impl/DayCycleServiceImpl.java)

## Availability

- Auto service: yes
- Default load: `LoadMode.ALL`, `LoadMode.GAMEPLAY`

## Method reference

Registration:

- `addWorld(World, sunrise, sunset)`
- `addWorld(World, sunrise, sunset, end)`
- `addWorld(World, SimulatedDayCycle)`

Lifecycle:

- `removeWorld(String worldName)`
- `clearAll()`
- `getCycle(String)` / `exists(String)` / `all()`

Global controls:

- `startAll()` / `stopAll()` / `pauseAll()` / `resumeAll()`
- `forEach(Consumer<SimulatedDayCycle>)`

Global callbacks:

- `onGlobalStart(Runnable)`
- `onGlobalStop(Runnable)`
- `onGlobalSunrise(Runnable)`
- `onGlobalSunset(Runnable)`
- `onGlobalMidnight(Runnable)`

## Practical example

```java
DayCycleService dayCycle = DreamPlugin.getService(DayCycleService.class);
dayCycle.addWorld(world, new SimulateTime(6, 0, 0), new SimulateTime(18, 0, 0));
dayCycle.startAll();
```

Navigation: [`docs/services/team-service.md`](docs/services/team-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/service-runtime.md`](docs/services/service-runtime.md)

