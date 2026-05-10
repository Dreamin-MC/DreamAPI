# GUI Service

## Purpose

`GuiService` tracks GUI lifecycle per player and exposes navigation history that can be reused for back-navigation patterns.

Source:
- API contract: [`api/src/main/java/fr/dreamin/dreamapi/api/gui/service/GuiService.java`](/api/src/main/java/fr/dreamin/dreamapi/api/gui/service/GuiService.java)
- Session model: [`api/src/main/java/fr/dreamin/dreamapi/api/gui/model/GuiSession.java`](/api/src/main/java/fr/dreamin/dreamapi/api/gui/model/GuiSession.java)
- Runtime implementation: [`core/src/main/java/fr/dreamin/dreamapi/core/gui/service/GuiServiceImpl.java`](/core/src/main/java/fr/dreamin/dreamapi/core/gui/service/GuiServiceImpl.java)
- Base GUI flow: [`api/src/main/java/fr/dreamin/dreamapi/api/gui/model/GuiInterface.java`](/api/src/main/java/fr/dreamin/dreamapi/api/gui/model/GuiInterface.java)
- Utility items: [`core/src/main/java/fr/dreamin/dreamapi/core/gui/item/GuiItems.java`](/core/src/main/java/fr/dreamin/dreamapi/core/gui/item/GuiItems.java)

## How It Works

### 1) Open flow from `GuiInterface`

When you call `open(player)` on a GUI extending `GuiInterface`:

1. `createWindowBuilder(player)` builds an InvUI `Window.Builder`.
2. Window options configured on the GUI instance are applied:
   - title or title supplier
   - cursor/inventory visualizers
   - open/outside-click/window-state handlers
   - fallback window supplier
   - window modifiers
3. A `GuiWindowOpenEvent` is fired.
4. On success, the window opens and `GuiService.recordOpen(player, gui)` is called.

### 2) Close flow

When the window closes:

1. `GuiWindowCloseEvent` is fired.
2. `GuiService.recordClose(player, gui, reason)` closes the current session entry.

### 3) History model

`GuiServiceImpl` stores:

- `currentSessions`: the active session per player
- `history`: a deque per player with newest session at index `0`

`getPreviousGui(playerId)` returns:

- `null` if no previous session exists
- the GUI from history index `1` (the GUI opened just before the current one)

This makes back-navigation deterministic and independent from GUI class wiring.

## Create a GUI

Minimal custom GUI:

```java
public final class MyMenu extends GuiInterface {

  @Override
  public Component name(@NotNull Player player) {
    return Component.text("My Menu");
  }

  @Override
  public Gui guiUpper(@NotNull Player player) {
    return Gui.builder()
      .setStructure(1, 1, "X")
      .addIngredient('X', Item.empty())
      .build();
  }
}
```

Open it:

```java
new MyMenu().open(player);
```

## Window Options You Can Configure

`GuiInterface` now centralizes most InvUI `Window.Builder` options:

- `setTitle(Component)` / `setTitleSupplier(Supplier<Component>)`
- `setOpenHandlers(...)` / `addOpenHandler(...)`
- `setOutsideClickHandlers(...)` / `addOutsideClickHandler(...)`
- `setWindowStateChangeHandlers(...)` / `addWindowStateChangeHandler(...)`
- `setFallbackWindow(...)`
- `setModifiers(...)` / `addModifier(...)`
- `setCursorVisualizer(...)`
- `setInventoryVisualizer(...)`

Use these on the GUI instance before `open(player)`.

## Back Navigation With `GuiItems.BACK`

`GuiItems.BACK(...)` supports three navigation modes:

1. Explicit target GUI: `BACK(GuiInterface)`
2. Automatic history by player object: `BACK(Player)` and overloads
3. Automatic history by player id: `BACK(UUID)` and overloads

Automatic mode asks `GuiService` for `getPreviousGui(playerId)` and opens it if present.

Example:

```java
.addIngredient('B', GuiItems.BACK(player).build())
```

If no previous GUI exists, the click is ignored.

## Testing In `example-plugin`

Use the dedicated test commands:

- `/gui cursorhandler`
- `/gui openhandler`
- `/gui titlesupplier`
- `/gui inventoryvisualizer` (and alias `/gui inventoryvisuyalizer`)
- `/gui visualizertest` (cursor + chained inventory visualizer)
- `/gui outsideclickhandlers`
- `/gui backservice` (2-step flow using `GuiItems.BACK(...)` + `GuiService` state messages)

Command source: [`example-plugin/src/main/java/fr/dreamin/example/cmd/GUICmd.java`](/example-plugin/src/main/java/fr/dreamin/example/cmd/GUICmd.java)

## Practical Notes

- `GuiService` is designed as a runtime service. `GuiInterface` guards calls so GUI opening still works when service wiring is unavailable.
- `GuiSession` stores both metadata (`guiName`, `guiClass`) and direct GUI reference (`gui`) to enable instant reopen/back behavior.
- History is append-on-open, close updates session state only.
- `titleSupplier` is primarily evaluated when opening the window; use re-open or explicit updates if you expect live-changing titles.
- `inventoryVisualizer` is only applied to `SlotElement.InventoryLink` elements (player/inventory-linked slots), not arbitrary static GUI ingredients.
- `cursorVisualizer` affects the cursor rendering while the window is managed by InvUI; behavior may differ depending on window/inventory implementation.

## Related Docs

- Service directory: [`docs/services/index.md`](/docs/services/index.md)
- Service runtime internals: [`docs/services/service-runtime.md`](/docs/services/service-runtime.md)
- Recipes GUI example: [`core/src/main/java/fr/dreamin/dreamapi/core/recipe/ui/RecipeCategoryGUI.java`](/core/src/main/java/fr/dreamin/dreamapi/core/recipe/ui/RecipeCategoryGUI.java)


