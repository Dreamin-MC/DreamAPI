# Module: example-plugin

`example-plugin` is a runnable showcase of DreamAPI integration.

## What it demonstrates

- Extending `DreamPlugin`
- Enabling admin command packs
- Basic startup/shutdown hooks

## Key classes

- [`example-plugin/src/main/java/fr/dreamin/example/ExamplePlugin.java`](example-plugin/src/main/java/fr/dreamin/example/ExamplePlugin.java)
- [`example-plugin/src/main/resources/paper-plugin.yml`](example-plugin/src/main/resources/paper-plugin.yml)

## Runtime notes

The example plugin targets the same Paper API line configured in Gradle (`26.1.2`).

## Practical Example

```java
@Override
public void onDreamEnable() {
  setBroadcastCmd(true);
  setItemRegistryCmd(true);
  setDebugCmd(true);
  setServiceCmd(true);
}
```

