# SkinService

Navigation: [`docs/services/recipe-viewer-service.md`](docs/services/recipe-viewer-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/tablist-service.md`](docs/services/tablist-service.md)

## Purpose

Handles player skin apply/reset and named skin registry.

## Source links

- API: [`api/src/main/java/fr/dreamin/dreamapi/api/skin/service/SkinService.java`](api/src/main/java/fr/dreamin/dreamapi/api/skin/service/SkinService.java)
- Core: [`core/src/main/java/fr/dreamin/dreamapi/core/skin/service/SkinServiceImpl.java`](core/src/main/java/fr/dreamin/dreamapi/core/skin/service/SkinServiceImpl.java)

## Availability

- Auto service: yes
- Default load: manual include (not present in built-in load-mode map)

## Method reference

- `setSkin(Player, Skin)`: applies explicit skin value/signature.
- `fetchSkinFromName(String)`: async Mojang lookup by player name.
- `setSkinFromName(Player, String)`: lookup + apply workflow.
- `resetSkin(Player)`: resets player skin to default profile state.
- `getCurrentSkin(Player)`: returns cached skin for a player.
- `registerNamedSkin(String, Skin)`: stores reusable named skin entry.
- `getNamedSkin(String)`: reads named skin entry.
- `setNamedSkin(Player, String)`: applies registered named skin.

## Technical notes

- Core uses Mojang profile APIs and refreshes player visibility after updates.
- Name-based lookup is asynchronous.

## Practical example

```java
SkinService skins = DreamPlugin.getService(SkinService.class);
skins.setSkinFromName(player, "Notch");

skins.fetchSkinFromName("jeb_").thenAccept(skin -> {
  skin.ifPresent(found -> skins.registerNamedSkin("staff_jeb", found));
});
```

Navigation: [`docs/services/recipe-viewer-service.md`](docs/services/recipe-viewer-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/tablist-service.md`](docs/services/tablist-service.md)

