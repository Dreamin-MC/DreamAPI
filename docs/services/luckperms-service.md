# LuckPermsService

Navigation: [`docs/services/lang-service.md`](/docs/services/lang-service.md) | [`docs/services/index.md`](/docs/services/index.md) | [`docs/services/recipe-category-registry-service.md`](/docs/services/recipe-category-registry-service.md)

## Purpose

Provides LuckPerms-backed metadata and permission operations with DreamAPI integration.

## Source links

- API: [`api/src/main/java/fr/dreamin/dreamapi/api/luckperms/LuckPermsService.java`](/api/src/main/java/fr/dreamin/dreamapi/api/luckperms/LuckPermsService.java)
- Core: [`core/src/main/java/fr/dreamin/dreamapi/core/luckperms/LuckPermsServiceImpl.java`](/core/src/main/java/fr/dreamin/dreamapi/core/luckperms/LuckPermsServiceImpl.java)
- Dependency: [`core/src/main/java/fr/dreamin/dreamapi/core/team/TeamServiceImpl.java`](/core/src/main/java/fr/dreamin/dreamapi/core/team/TeamServiceImpl.java)

## Availability

- Auto service: yes
- Depends on: `TeamServiceImpl`
- Loaded conditionally when LuckPerms plugin and service are available

## Method reference

Service control:

- `isEnabled()` / `setEnabled(boolean)`: toggles integration behavior.
- `getMsgKickError()` / `setMsgKickError(Component)`: login error message customization.

Metadata lookups (async):

- `getPrimaryGroup(UUID)`
- `getPrefix(UUID)`
- `getSuffix(UUID)`

Permission writes (async):

- `setPermission(UUID, permission, value)`
- `removePermission(UUID, permission)`
- `applyTemporaryPermission(UUID, permission, value, Duration)`
- `schedulePermissionChange(UUID, Runnable action, Duration delay)`

Visual refresh:

- `refreshPlayerVisuals(Player)`: updates team/display from current metadata.

## Technical notes

- Core implementation customizes chat rendering when enabled.
- Most methods are asynchronous (`CompletableFuture`), so avoid blocking main thread.

## Practical example

```java
LuckPermsService lp = DreamPlugin.getService(LuckPermsService.class);

lp.getPrefix(player.getUniqueId()).thenAccept(prefix -> {
  player.sendMessage("Your prefix: " + prefix);
});
```

Navigation: [`docs/services/lang-service.md`](/docs/services/lang-service.md) | [`docs/services/index.md`](/docs/services/index.md) | [`docs/services/recipe-category-registry-service.md`](/docs/services/recipe-category-registry-service.md)

