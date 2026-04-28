# ItemRegistryService

Navigation: `docs/services/hologram-service.md` | `docs/services/index.md` | `docs/services/lang-service.md`

## Purpose

Registers custom item definitions and routes item actions to handlers.

## Source links

- API: `api/src/main/java/fr/dreamin/dreamapi/api/item/ItemRegistryService.java`
- Core: `core/src/main/java/fr/dreamin/dreamapi/core/item/service/ItemRegistryServiceImpl.java`

## Availability

- Auto service: yes
- Default load: `LoadMode.ALL`, `LoadMode.DATA`

## Method reference

Registration and handlers:

- `registers(Collection<ItemDefinition>)`: bulk registration.
- `register(ItemDefinition)`: single item registration by id.
- `addHandler(String id, ItemAction, ItemHandler)`: adds handler to one item id.
- `addHandler(ItemTag, ItemAction, ItemHandler)`: adds handler to all items matching tag.

Queries and retrieval:

- `getAllRegisteredItems()`: all `RegisteredItem` objects.
- `getAllRegisteredItemStacks()`: all item stacks.
- `get(String id)` / `get(ItemStack item)`: resolve registered item by id or item metadata.
- `getAllRegisteredItems(ItemTag)` / `getAllRegisteredItemStacks(ItemTag)`: filtered lookups.
- `getItem(String id)`: cloned `ItemStack` instance or `null`.
- `isRegistered(String id)` / `isRegistered(ItemStack)`: existence checks.

## Technical notes

- Core implementation maps many Bukkit/Paper events to `ItemAction` values.
- Item identity lookup relies on persistent data key (`ItemKeys.ITEM_ID`).

## Practical example

```java
ItemRegistryService items = DreamPlugin.getService(ItemRegistryService.class);

if (!items.isRegistered("starter_pickaxe")) {
  getLogger().warning("starter_pickaxe is missing in registry");
}
```

## Common pitfalls

- Duplicate ids throw on `register(...)`.
- `get(ItemStack)` returns `null` when metadata key is absent.

Navigation: `docs/services/hologram-service.md` | `docs/services/index.md` | `docs/services/lang-service.md`

