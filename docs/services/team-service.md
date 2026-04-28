# TeamService (Core)

Navigation: [`docs/services/player-debug-service.md`](/docs/services/player-debug-service.md) | [`docs/services/index.md`](/docs/services/index.md) | [`docs/services/day-cycle-service.md`](/docs/services/day-cycle-service.md)

## Purpose

Provides scoreboard team management for entities and player display styling.

## Source links

- Interface: [`core/src/main/java/fr/dreamin/dreamapi/core/team/TeamService.java`](/core/src/main/java/fr/dreamin/dreamapi/core/team/TeamService.java)
- Implementation: [`core/src/main/java/fr/dreamin/dreamapi/core/team/TeamServiceImpl.java`](/core/src/main/java/fr/dreamin/dreamapi/core/team/TeamServiceImpl.java)

## Availability

- Auto service: yes
- Default load: `LoadMode.ALL`, `LoadMode.GAMEPLAY`

## Method reference

Team lifecycle:

- `createOrGetTeam(Entity)`: creates/reuses team bound to entity key.
- `removeTeam(Entity)`: removes entity-generated team.
- `clearAllTeams()`: unregisters all teams.
- `getTeam(UUID)` / `getTeamByName(String)`: team lookups.
- `getAllTeams()`: returns all scoreboard teams.

Member operations:

- `addEntityToTeam(Team, Entity)`
- `removeEntityFromTeam(Team, Entity)`
- `isInTeam(Team, Entity)`

Style and behavior:

- `setNametagVisible(Team, boolean)`
- `setCollision(Team, boolean)`
- `setFriendlyFire(Team, boolean)`
- `setPrefix(Team, Component)`
- `setSuffix(Team, Component)`
- `setColor(Team, NamedTextColor)`

Utilities:

- `updatePlayerDisplay(Player, prefix, suffix)`: updates display using legacy serializer.
- `createCustomTeam(String)`, `removeTeamByName(String)`
- `cleanupEmptyTeams()`, `exists(String)`

## Practical example

```java
TeamService teams = DreamPlugin.getService(TeamService.class);
var team = teams.createOrGetTeam(player);
teams.setPrefix(team, Component.text("[VIP] "));
```

Navigation: [`docs/services/player-debug-service.md`](/docs/services/player-debug-service.md) | [`docs/services/index.md`](/docs/services/index.md) | [`docs/services/day-cycle-service.md`](/docs/services/day-cycle-service.md)

