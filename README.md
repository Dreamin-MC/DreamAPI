# DreamAPI

[![Version](https://img.shields.io/badge/version-0.6.0-blue.svg)](https://github.com/Dreamin-MC/DreamAPI)
[![JitPack](https://jitpack.io/v/Dreamin-MC/DreamAPI.svg)](https://jitpack.io/#Dreamin-MC/DreamAPI)
[![Java](https://img.shields.io/badge/java-25-orange.svg)](https://www.oracle.com/java/)
[![Paper](https://img.shields.io/badge/paper-26.1.2-green.svg)](https://papermc.io/)
[![Wiki](https://img.shields.io/badge/documentation-GitHub_Wiki-brightgreen.svg)](https://github.com/Dreamin-MC/DreamAPI/wiki)
[![License](https://img.shields.io/badge/license-GPL--3.0-lightgrey.svg)](LICENSE)

---

### Overview

**DreamAPI** is a high-performance, modular API and runtime development framework for Minecraft Paper 26.1.2+ servers built on Java 25. It provides a lightweight Inversion of Control (IoC) service container (`@DreamAutoService`, `@Inject`), packet-driven visual and NMS systems, fluent builders, and 21 specialized library modules.

Complete, up-to-date documentation is available directly on the **[GitHub Wiki](https://github.com/Dreamin-MC/DreamAPI/wiki)**.

---

### Key Features

- **Decoupled 21-Module Architecture**: Consume only what your plugin needs or use the umbrella `dreamapi-all` artifact.
- **Inversion of Control & DI**: Dependency injection via `@Inject` and auto-discovery via `@DreamAutoService` with topological dependency resolution.
- **Base Plugin Foundation**: Zero-boilerplate bootstrapping via `DreamPlugin` (now natively part of `core`).
- **Multi-Version Compatibility**: Runtime version gating across Paper 26.1.2, 26.2, 26.3, and 26.4+ using `@RequiresVersion` and `VersionUtils`.
- **Packet-Level Visuals**: Client-side fake blocks, fake entities, per-player glowing outlines, and virtual world borders without modifying server world state.
- **Rich Gameplay Tooling**: Fluent builders for items, recipes, paged/filterable inventory GUIs, Display Entity holograms, 3D cuboids, and particle animations.
- **Infrastructure & Utilities**: HikariCP connection pooling (MySQL/PostgreSQL), async HTTP/2 client (Apache HttpClient 5 + Jackson), Adventure localization, and Cloud Paper command integration.

---

### Project Structure (22 Subprojects)

```text
DreamAPI/
├── core/             // Base plugin (DreamPlugin), IoC container, scheduler, math, versioning
├── nms/              // Packet fake entities, fake blocks, tablist
├── item/             // Fluent ItemBuilder and @DreamItem registry
├── gui/              // InvUI GUI framework with pagination and history
├── recipe/           // Custom crafting, furnace, and smithing recipes
├── cuboid/           // 3D volume regions and enter/leave detection
├── particle/         // Particle shapes, morphing, and easing functions
├── hologram/         // Modern Display Entity holograms (Text/Item)
├── glowing/          // Packet-based glowing outlines with color animations
├── worldborder/      // Per-player virtual worldborder simulation
├── world/            // Dynamic worlds, rollback templates, minigame state machine
├── navigate/         // Custom A* pathfinding and entity waypoint navigation
├── lang/             // Adventure translation bundles and JSON localization
├── dialog/           // Interactive branching NPC dialogue trees
├── skin/             // Asynchronous Mojang and Mineskin profile application
├── database/         // HikariCP connection pool for MySQL & PostgreSQL
├── http/             // Async HTTP/2 client with Jackson serialization
├── command/          // Cloud Paper command processor (@DreamCmd)
├── animation/        // Cutscene timeline sequencer and camera movements
├── hooks/            // Bridges for LuckPerms, Citizens 2, ModelEngine, BKCommonLib
├── all/              // Published umbrella artifact bundling all 20 feature modules + core
└── example-plugin/   // Reference Paper plugin demonstrating usage
```

---

### Services & Documentation Index

All documentation is published and maintained on the **[DreamAPI GitHub Wiki](https://github.com/Dreamin-MC/DreamAPI/wiki)**:

**Framework Guides**
- [Wiki Home](https://github.com/Dreamin-MC/DreamAPI/wiki/Home)
- [Installation & JitPack Guide](https://github.com/Dreamin-MC/DreamAPI/wiki/Installation-and-JitPack)
- [Quick Start in 5 Minutes](https://github.com/Dreamin-MC/DreamAPI/wiki/Quick-Start)
- [Architecture & Modules](https://github.com/Dreamin-MC/DreamAPI/wiki/Architecture-and-Modules)
- [Multi-Version Support](https://github.com/Dreamin-MC/DreamAPI/wiki/Multi-Version-Support)
- [Basic Concepts](https://github.com/Dreamin-MC/DreamAPI/wiki/Basic-Concepts)

**Visual & NMS Modules**
- [`AnimationService` (Cutscenes & Camera)](https://github.com/Dreamin-MC/DreamAPI/wiki/Animation)
- [`GlowingService` (Per-Viewer Glowing)](https://github.com/Dreamin-MC/DreamAPI/wiki/Glowing)
- [`HologramService` (Display Entities)](https://github.com/Dreamin-MC/DreamAPI/wiki/Hologram)
- [`SkinService` (Async Skins & Profiles)](https://github.com/Dreamin-MC/DreamAPI/wiki/Skin)
- [`VisualService` & `TabListService`](https://github.com/Dreamin-MC/DreamAPI/wiki/Nms)
- [`WorldBorderService` (Virtual Borders)](https://github.com/Dreamin-MC/DreamAPI/wiki/Worldborder)
- [`ParticleAnimation` & Morphing](https://github.com/Dreamin-MC/DreamAPI/wiki/Particle)

**Gameplay & Data Modules**
- [`Cuboid` & `CuboidService`](https://github.com/Dreamin-MC/DreamAPI/wiki/Cuboid)
- [`WorldService` & `GameService`](https://github.com/Dreamin-MC/DreamAPI/wiki/World)
- [`ItemBuilder` & `ItemRegistryService`](https://github.com/Dreamin-MC/DreamAPI/wiki/Item)
- [`RecipeRegistryService` & Builders](https://github.com/Dreamin-MC/DreamAPI/wiki/Recipe)
- [`GuiService` & InvUI Paged GUIs](https://github.com/Dreamin-MC/DreamAPI/wiki/Gui)
- [`NavigateService` (A* Pathfinding)](https://github.com/Dreamin-MC/DreamAPI/wiki/Navigate)

**Infrastructure & Utilities**
- [`Core` Engine & `DreamPlugin`](https://github.com/Dreamin-MC/DreamAPI/wiki/Core)
- [`Command` Processing (`@DreamCmd`)](https://github.com/Dreamin-MC/DreamAPI/wiki/Command)
- [`Database` (HikariCP SQL Pool)](https://github.com/Dreamin-MC/DreamAPI/wiki/Database)
- [`HTTP` (Async API Client)](https://github.com/Dreamin-MC/DreamAPI/wiki/Http)
- [`Lang` (Adventure Translations)](https://github.com/Dreamin-MC/DreamAPI/wiki/Lang)
- [`Dialog` (NPC Conversations)](https://github.com/Dreamin-MC/DreamAPI/wiki/Dialog)
- [`Hooks` (LuckPerms, Citizens, etc.)](https://github.com/Dreamin-MC/DreamAPI/wiki/Hooks)
- [`All` Umbrella Library](https://github.com/Dreamin-MC/DreamAPI/wiki/All)

---

### Developer Integration (JitPack)

#### 1. Add JitPack Repository

**Gradle (Kotlin DSL)**
```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}
```

**Gradle (Groovy DSL)**
```groovy
repositories {
    mavenCentral()
    maven { url = "https://jitpack.io" }
}
```

**Maven (`pom.xml`)**
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

#### 2. Declare Dependencies

You can depend on the complete umbrella library or select individual modules:

**Option A: Umbrella Library (All 21 Modules)**
```groovy
dependencies {
    compileOnly "io.papermc.paper:paper-api:26.1.2.build.+"
    implementation "com.github.Dreamin-MC.DreamAPI:dreamapi-all:0.6.0"
}
```

**Option B: Granular Modules (Recommended for Production)**
```groovy
dependencies {
    compileOnly "io.papermc.paper:paper-api:26.1.2.build.+"
    implementation "com.github.Dreamin-MC.DreamAPI:dreamapi-core:0.6.0"
    implementation "com.github.Dreamin-MC.DreamAPI:dreamapi-item:0.6.0"
    implementation "com.github.Dreamin-MC.DreamAPI:dreamapi-gui:0.6.0"
    implementation "com.github.Dreamin-MC.DreamAPI:dreamapi-glowing:0.6.0"
}
```

---

### Contributing

Contributions are welcome! Before submitting a pull request:
1. Ensure your code complies with our Java 25 style conventions (`final var` priority, braceless single-statement blocks, top-level imports).
2. Verify that `./gradlew compileJava compileTestJava test` passes.
3. Keep module boundaries clean and update documentation on the wiki.

---

### License

DreamAPI is distributed under the **GNU General Public License v3.0 (GPL-3.0)**. See `LICENSE`.

---

### Support & Community

- **Documentation**: [GitHub Wiki](https://github.com/Dreamin-MC/DreamAPI/wiki)
- **Issues & Bug Reports**: [GitHub Issues](https://github.com/Dreamin-MC/DreamAPI/issues)
- **Discord Community**: [Dreamin Discord](https://discord.dreamin.fr)
