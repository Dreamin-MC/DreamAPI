# Module: plugin-base

The `plugin-base` module exposes `DreamPlugin`, the base class that bootstraps DreamAPI in Paper plugins.

## Responsibilities

- Initialize provider if missing
- Scan plugin classes
- Load services from load mode and `@EnableServices`
- Run annotation processors for commands, listeners, and recipes
- Provide helper methods for service and item/recipe access

## Key class

- [`plugin-base/src/main/java/fr/dreamin/dreamapi/plugin/DreamPlugin.java`](plugin-base/src/main/java/fr/dreamin/dreamapi/plugin/DreamPlugin.java)

## Recommended usage

Extend `DreamPlugin` in your plugin main class and implement:

- `onDreamEnable()`
- `onDreamDisable()`

## Practical Example

```java
import fr.dreamin.dreamapi.plugin.DreamPlugin;

public final class MyPlugin extends DreamPlugin {
  @Override
  public void onDreamEnable() {
	setDebugCmd(true);
	setServiceCmd(true);
  }

  @Override
  public void onDreamDisable() {}
}
```

