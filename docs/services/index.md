# Services Index

Use this page as the main directory for all DreamAPI services.

## Fast Links

- Folder guide: `docs/services/README.md`
- Usage recipes: `docs/examples/service-recipes.md`
- Runtime internals: `docs/services/service-runtime.md`

## Service Discovery Table

| Service | Category | Typical Usage | Default Load |
|--------|----------|---------------|--------------|
| `animation-service.md` | visual/gameplay | build camera intros and transitions | ALL, GAMEPLAY |
| `cuboid-service.md` | gameplay | trigger enter/leave zone logic | ALL, VISUAL |
| `debug-service.md` | utility | async logging, categories, writers | ALL, MINIMAL, DEBUG |
| `dialog-service.md` | experimental | Paper dialogs wrappers | manual/experimental |
| `game-service.md` | gameplay | switch game states and tick loops | manual include |
| `glowing-service.md` | visual | per-viewer glow effects | ALL, VISUAL |
| `hologram-service.md` | visual/data | spawn/save/load holograms | ALL, DATA |
| `item-registry-service.md` | data/gameplay | custom item actions and handlers | ALL, DATA |
| `lang-service.md` | utility | localization and translations | ALL |
| `luckperms-service.md` | utility/integration | prefix/suffix and permission integration | conditional |
| `recipe-category-registry-service.md` | data | recipe category lookups | ALL, DATA |
| `recipe-registry-service.md` | data/gameplay | register custom recipes with conditions | ALL, DATA |
| `recipe-viewer-service.md` | ui | recipe preview contract | no default impl |
| `skin-service.md` | visual | apply/reset skins | manual include |
| `tablist-service.md` | visual | tab list visibility modes | ALL, VISUAL |
| `visual-service.md` | visual/nms | fake entities/blocks and frozen time | ALL, VISUAL |
| `world-service.md` | gameplay/data | clone/reset/delete worlds | ALL, GAMEPLAY |
| `worldborder-service.md` | visual/gameplay | personal border behavior and pulse | ALL |
| `persistent-worldborder-service.md` | visual/gameplay | persistent world border abstraction | via worldborder impl |
| `player-debug-service.md` | core/internal | target-debugger mapping | ALL, MINIMAL, DEBUG |
| `team-service.md` | core/internal | scoreboard teams and display updates | ALL, GAMEPLAY |
| `day-cycle-service.md` | core/internal | simulated day/night loops | ALL, GAMEPLAY |
| `service-runtime.md` | internals | service loader and lifecycle internals | runtime utility |

## Category shortcuts

- Visual: `docs/services/animation-service.md`, `docs/services/glowing-service.md`, `docs/services/hologram-service.md`, `docs/services/tablist-service.md`, `docs/services/visual-service.md`, `docs/services/worldborder-service.md`
- Data/Gameplay: `docs/services/item-registry-service.md`, `docs/services/recipe-registry-service.md`, `docs/services/recipe-category-registry-service.md`, `docs/services/world-service.md`, `docs/services/game-service.md`
- Utility: `docs/services/lang-service.md`, `docs/services/debug-service.md`, `docs/services/luckperms-service.md`
- Core/Internal: `docs/services/player-debug-service.md`, `docs/services/team-service.md`, `docs/services/day-cycle-service.md`, `docs/services/service-runtime.md`

## Quick Start Usage

```java
import fr.dreamin.dreamapi.plugin.DreamPlugin;
import fr.dreamin.dreamapi.api.glowing.service.GlowingService;

GlowingService glowing = DreamPlugin.getService(GlowingService.class);
```

Source:
- `plugin-base/src/main/java/fr/dreamin/dreamapi/plugin/DreamPlugin.java`




