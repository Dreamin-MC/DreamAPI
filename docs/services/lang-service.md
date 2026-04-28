# LangService

Navigation: [`docs/services/item-registry-service.md`](docs/services/item-registry-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/luckperms-service.md`](docs/services/luckperms-service.md)

## Purpose

Loads translation files and resolves locale-aware text.

## Source links

- API: [`api/src/main/java/fr/dreamin/dreamapi/api/lang/service/LangService.java`](api/src/main/java/fr/dreamin/dreamapi/api/lang/service/LangService.java)
- Core: [`core/src/main/java/fr/dreamin/dreamapi/core/lang/service/LangServiceImpl.java`](core/src/main/java/fr/dreamin/dreamapi/core/lang/service/LangServiceImpl.java)

## Availability

- Auto service: yes
- Default load: `LoadMode.ALL`

## Method reference

Loading:

- `load()`: scans default lang folder and loads supported files.
- `load(File)`: loads one file.

Locale and translation lookups:

- `getLocale(UUID)`: player locale cache lookup.
- `getTranslation(Player, key)` / `getTranslation(Locale, key)`: direct translation lookup.
- `findTranslation(Locale, key)` / `findTranslation(UUID, key)`: fallback lookup helper.

Feature toggles:

- `enableItem(boolean)` / `isEnableItem()`: enable/disable item translation updates.
- `enableGUI(boolean)` / `isEnableGUI()`: enable/disable GUI translation updates.

Translator store:

- `getTranslator(String)` / `createTranslator(label, namespace, value)`: translator source operations.

File/state management:

- `getLangFiles()`, `getLoadedFiles()`, `getLangFile(String)`: inspection methods.
- `unload(String)`: unloads one file key.
- `reload(String)`: unload + reload one file key.
- `add(String)`: loads one file key if present on disk.
- `reset()`: clears stores and player locale cache.

Utilities:

- `isSupportedLangFile(File)`: file extension support check.
- `buildFileKey(langFolder, file)`: computes normalized file key.

## Technical notes

- Default folder is `<pluginData>/lang`.
- Core monitors player locale changes and can refresh item/gui translations.

## Practical example

```java
LangService lang = DreamPlugin.getService(LangService.class);
lang.load();

String msg = lang.getTranslation(player, "welcome.message")
  .orElse("Welcome!");
player.sendMessage(msg);
```

## Common pitfalls

- File key is normalized path, not always plain filename.
- Missing namespace/value in JSON file causes load errors.

Navigation: [`docs/services/item-registry-service.md`](docs/services/item-registry-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/luckperms-service.md`](docs/services/luckperms-service.md)

