# DreamAPI

[![Version](https://img.shields.io/badge/version-0.2.8-blue.svg)](https://github.com/Dreamin-MC/DreamAPI)
[![JitPack](https://jitpack.io/v/Dreamin-MC/DreamAPI.svg)](https://jitpack.io/#Dreamin-MC/DreamAPI)
[![Java](https://img.shields.io/badge/java-25-orange.svg)](https://www.oracle.com/java/)
[![Paper](https://img.shields.io/badge/paper-26.1.2-green.svg)](https://papermc.io/)
[![License](https://img.shields.io/badge/license-GPL--3.0-lightgrey.svg)](LICENSE)

---

### Overview

**DreamAPI** is a modular API and runtime foundation for the Dreamin ecosystem.
It targets modern Paper servers and provides reusable systems for commands, recipes, items,
visual effects, world tools, localization, and service-driven architecture.

---

### Key Features

- **Multi-module architecture** with `api`, `core`, `plugin-base`, and `example-plugin`.
- **Service-oriented runtime** with dependency-aware loading (`@DreamAutoService`) and lifecycle hooks.
- **Rich gameplay tooling** including recipes, item registry, glowing, tab list, world border, and cuboids.
- **NMS visual utilities** for fake entities, fake blocks, and client-side visual state.
- **Localization stack** based on Adventure translators and JSON language files.
- **Plugin bootstrap layer** via `DreamPlugin` with automatic service, listener, and command wiring.

---

### Project Goal

DreamAPI aims to provide a public, production-focused foundation for Paper plugin development by:

- reducing boilerplate through shared abstractions,
- standardizing service patterns across projects,
- enabling faster prototyping with reusable runtime components,
- keeping plugin internals maintainable with clear module boundaries.

---

### Project Structure

```text
DreamAPI/
  api/            // Public interfaces, models, and contracts
  core/           // Runtime implementations
  plugin-base/    // DreamPlugin base class and bootstrap helpers
  example-plugin/ // Reference plugin using DreamAPI
```

---

### Services Documentation

For plugin integration, service pages are usually the most useful entry point.

**Visual & NMS**

- [`AnimationService`](/docs/services/animation-service.md)
- [`GlowingService`](/docs/services/glowing-service.md)
- [`HologramService`](/docs/services/hologram-service.md)
- [`SkinService`](/docs/services/skin-service.md)
- [`TabListService`](/docs/services/tablist-service.md)
- [`VisualService`](/docs/services/visual-service.md)
- [`WorldBorderService`](/docs/services/worldborder-service.md)

**Gameplay & Data**

- [`CuboidService`](/docs/services/cuboid-service.md)
- [`GameService`](/docs/services/game-service.md)
- [`ItemRegistryService`](/docs/services/item-registry-service.md)
- [`RecipeRegistryService`](/docs/services/recipe-registry-service.md)
- [`RecipeCategoryRegistryService`](/docs/services/recipe-category-registry-service.md)
- [`RecipeViewerService`](/docs/services/recipe-viewer-service.md)
- [`WorldService`](/docs/services/world-service.md)

**Utility & Integration**

- [`DebugService`](/docs/services/debug-service.md)
- [`LangService`](/docs/services/lang-service.md)
- [`LuckPermsService`](/docs/services/luckperms-service.md)
- [`DialogService` (experimental)](/docs/services/dialog-service.md)

**Core/Internal Services**

- [`PlayerDebugService`](/docs/services/player-debug-service.md)
- [`TeamService`](/docs/services/team-service.md)
- [`DayCycleService`](/docs/services/day-cycle-service.md)
- [`Service Runtime`](/docs/services/service-runtime.md)

Full directory: [`docs/services/index.md`](/docs/services/index.md)

---

### Developer Integration

#### Adding as a dependency

**Maven**
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.github.Dreamin-MC</groupId>
  <artifactId>DreamAPI</artifactId>
  <version>0.2.8:all</version>
</dependency>
```

**Gradle (Kotlin DSL)**
```kotlin
repositories {
  maven("https://jitpack.io")
}

dependencies {
  compileOnly("com.github.Dreamin-MC:DreamAPI:0.2.8:all")
}
```

**Gradle (Groovy DSL)**
```groovy
repositories {
  maven { url = "https://jitpack.io" }
}

dependencies {
  compileOnly "com.github.Dreamin-MC:DreamAPI:0.2.8:all"
}
```

---

### Dreamin Ecosystem

DreamAPI is the shared technical base used by Dreamin modules and plugins.

| Project | Description |
|---------|-------------|
| DreamAPI | Core modular API/runtime foundation |

---

### Project Status

| Status | Current Version | Target Runtime |
|--------|-----------------|----------------|
| Active development | `v0.2.8` | Java 25 + Paper `26.1.2.build.+` |

| Version Line | Runtime Notes |
|--------------|---------------|
| `v0.2.x` | Current development line |
| `v1.0.0` | Planned stable line with API freeze |

> DreamAPI is still evolving. Breaking changes may happen before `v1.0.0`.
> For production servers, pin exact versions and test upgrades first.

---

### Sub-Modules

- **`api`**: public contracts and interfaces
- **`core`**: service implementations and runtime internals
- **`plugin-base`**: `DreamPlugin` bootstrap class
- **`example-plugin`**: integration reference project

---

### Documentation

- **Documentation hub**: [`docs/index.md`](/docs/index.md)
- **Architecture**: [`docs/architecture.md`](/docs/architecture.md)
- **Services folder guide**: [`docs/services/README.md`](/docs/services/README.md)
- **Service recipes**: [`docs/examples/service-recipes.md`](/docs/examples/service-recipes.md)
- **Module docs**:
  - [`docs/modules/api.md`](/docs/modules/api.md)
  - [`docs/modules/core.md`](/docs/modules/core.md)
  - [`docs/modules/plugin-base.md`](/docs/modules/plugin-base.md)
  - [`docs/modules/example-plugin.md`](/docs/modules/example-plugin.md)
- **Service documentation (one page per service)**: [`docs/services/index.md`](/docs/services/index.md)

If you are integrating DreamAPI publicly, start with:
1. [`docs/index.md`](/docs/index.md)
2. [`docs/architecture.md`](/docs/architecture.md)
3. [`docs/services/README.md`](/docs/services/README.md)
4. [`docs/examples/service-recipes.md`](/docs/examples/service-recipes.md)

---

### Contributing

Contributions are welcome. Before opening a pull request:

- follow the existing code style and package conventions,
- preserve backward compatibility where possible,
- add or update tests when behavior changes,
- update documentation for public-facing API changes,
- validate on the current Paper runtime line.

---

### External Dependencies

DreamAPI integrates with a set of known libraries, including:

- [InvUI](https://github.com/NichtStudioCode/InvUI)
- [LuckPerms API](https://github.com/LuckPerms/LuckPerms)
- [Citizens API](https://github.com/CitizensDev/Citizens2)
- [BKCommonLib](https://github.com/bergerhealer/BKCommonLib)
- [Apache HttpClient 5](https://hc.apache.org/)
- [Jackson](https://github.com/FasterXML/jackson)

Dependency scopes may differ by module (`implementation` vs `compileOnly`), see each module build file for exact runtime expectations.

---

### License

This project is distributed under the **GPL-3.0** license.
See `LICENSE`.

---

## Support

- **Issues**: [GitHub Issues](https://github.com/Dreamin-MC/DreamAPI/issues)
- **Discord**: [Dreamin Discord](https://discord.gg/dreamin)
