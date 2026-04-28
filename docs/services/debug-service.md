# DebugService

Navigation: [`docs/services/cuboid-service.md`](docs/services/cuboid-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/dialog-service.md`](docs/services/dialog-service.md)

## Purpose

Central asynchronous debug logger with category and writer routing.

## Source links

- API: [`api/src/main/java/fr/dreamin/dreamapi/api/logger/DebugService.java`](api/src/main/java/fr/dreamin/dreamapi/api/logger/DebugService.java)
- Core: [`core/src/main/java/fr/dreamin/dreamapi/core/logger/DebugServiceImpl.java`](core/src/main/java/fr/dreamin/dreamapi/core/logger/DebugServiceImpl.java)
- Dependency service: [`core/src/main/java/fr/dreamin/dreamapi/core/logger/PlayerDebugServiceImpl.java`](core/src/main/java/fr/dreamin/dreamapi/core/logger/PlayerDebugServiceImpl.java)

## Availability

- Auto service: yes
- Depends on: `PlayerDebugServiceImpl`
- Default load: `LoadMode.ALL`, `LoadMode.MINIMAL`, `LoadMode.DEBUG`

## Method reference

- `getWriters()`: returns currently registered debug writers.
- `setGlobalDebug(boolean)`: enables/disables global debug bypass.
- `isGlobalDebug()`: checks global debug state.
- `setCategory(String, boolean)`: toggles one category.
- `isCategoryEnabled(String)`: checks category gate.
- `getCategories()`: returns category map.
- `setRetentionDays(int)`: configures log retention in days.
- `getRetentionDays()`: returns current retention value.
- `cleanupOldLogs()`: removes old log files based on retention policy.
- `addWriter(DebugWriter)`: registers writer (enabled by default).
- `addWriter(DebugWriter, boolean enabled)`: registers writer with explicit state.
- `removeWriter(DebugWriter)` / `removeWriter(Class<? extends DebugWriter>)`: unregisters writer.
- `setWriterEnabled(Class<? extends DebugWriter>, boolean)`: toggles one writer.
- `isWriterEnabled(Class<? extends DebugWriter>)`: checks writer state.
- `log(LogEntry)`: queues a log entry for async processing.

## Technical notes

- Core implementation uses a worker thread and queue (`LinkedBlockingDeque`).
- `log(...)` does not guarantee immediate write; it is async by design.

## Practical example

```java
DebugService debug = DreamPlugin.getService(DebugService.class);
debug.setCategory("combat", true);
debug.setRetentionDays(7);
debug.cleanupOldLogs();
```

Navigation: [`docs/services/cuboid-service.md`](docs/services/cuboid-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/dialog-service.md`](docs/services/dialog-service.md)

