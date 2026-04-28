# DialogService (Experimental)

Navigation: [`docs/services/debug-service.md`](docs/services/debug-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/game-service.md`](docs/services/game-service.md)

## Purpose

Wrapper around Paper dialog APIs for notice/confirm/form experiences.

## Source links

- API: [`api/src/main/java/fr/dreamin/dreamapi/api/dialog/DialogService.java`](api/src/main/java/fr/dreamin/dreamapi/api/dialog/DialogService.java)
- Core: [`core/src/main/java/fr/dreamin/dreamapi/core/dialog/DialogServiceImpl.java`](core/src/main/java/fr/dreamin/dreamapi/core/dialog/DialogServiceImpl.java)

## Availability

- Auto service: no (current implementation is not registered as Dream service)
- Status: experimental / partial

## Method reference

Top-level methods:

- `notice(Player, title, buttonLabel)`: simple one-button notice dialog.
- `confirm(Player, title, yes, no, callback)`: yes/no confirm flow (currently partial in core).
- `builder()`: returns a fluent `DialogBuilder`.

`DialogBuilder` methods:

- `title(String)`: dialog title.
- `message(String)`: appends a message body line.
- `bool(key, label, initial)`: adds boolean input.
- `text(key, label, initial, maxLength)`: adds text input.
- `number(key, label, min, max, initial)`: adds numeric input.
- `singleOption(key, label, entries...)`: single-choice option input.
- `onConfirm(callback)`: handles validated submit values.
- `onCancel(callback)`: handles cancellation.
- `show(Player)`: displays built dialog.

## Technical notes

- In current `DialogServiceImpl`, `notice(...)` is usable.
- `confirm(...)`, `singleOption(...)`, and `show(...)` are not fully implemented.

## Practical example

```java
DialogService dialogs = new DialogServiceImpl();
dialogs.notice(player, "Server Rules", "Understood");
```

Navigation: [`docs/services/debug-service.md`](docs/services/debug-service.md) | [`docs/services/index.md`](docs/services/index.md) | [`docs/services/game-service.md`](docs/services/game-service.md)

