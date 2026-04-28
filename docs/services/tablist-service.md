# TabListService

Navigation: [`docs/services/skin-service.md`](docs/services/skin-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/visual-service.md`](docs/services/visual-service.md)

## Purpose

Controls tab-list visibility and behavior per viewer.

## Source links

- API: [`api/src/main/java/fr/dreamin/dreamapi/api/nms/tablist/service/TabListService.java`](api/src/main/java/fr/dreamin/dreamapi/api/nms/tablist/service/TabListService.java)
- Core: [`core/src/main/java/fr/dreamin/dreamapi/core/nms/tablist/service/TabListServiceImpl.java`](core/src/main/java/fr/dreamin/dreamapi/core/nms/tablist/service/TabListServiceImpl.java)

## Availability

- Auto service: yes
- Default load: `LoadMode.ALL`, `LoadMode.VISUAL`

## Method reference

Mode control:

- `setMode(Player, TabListMode)`: sets explicit mode for one player.
- `getMode(Player)`: returns effective mode.
- `clearMode(Player)`: removes explicit override.
- `hasCustomMode(Player)`: checks override presence.
- `refresh(Player)`: reapplies current mode state.

Global defaults:

- `getDefaultMode()` / `setDefaultMode(TabListMode)`: default fallback mode.
- `isAutoEnabled()` / `setAutoEnabled(boolean)`: auto-apply default mode on join.

Cleanup tuning:

- `getTabCleanupDelayTicks()` / `setTabCleanupDelayTicks(long)`: delayed cleanup configuration.

Convenience methods:

- `setEmpty(Player)`: shortcut for `TabListMode.EMPTY`.
- `setHidden(Player)`: shortcut for `TabListMode.HIDDEN`.
- `reset(Player)`: shortcut for `clearMode(Player)`.

## Technical notes

- Core uses packet interception for hidden/empty behavior.
- Cleanup delay helps keep tab masking stable after sync packets.

## Practical example

```java
TabListService tab = DreamPlugin.getService(TabListService.class);
tab.setAutoEnabled(true);
tab.setHidden(player);
```

Navigation: [`docs/services/skin-service.md`](docs/services/skin-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/visual-service.md`](docs/services/visual-service.md)

