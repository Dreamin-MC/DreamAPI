# ✨ DreamAPI

[![Version](https://img.shields.io/badge/version-0.0.9.1-blue.svg)](https://github.com/Dreamin-MC/DreamAPI)
[![](https://jitpack.io/v/Dreamin-MC/DreamAPI.svg)](https://jitpack.io/#Dreamin-MC/DreamAPI)
[![Java](https://img.shields.io/badge/java-21-orange.svg)](https://www.oracle.com/java/)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21.10-green.svg)](https://www.minecraft.net/)
[![License](https://img.shields.io/badge/license-GPL-lightgrey.svg)](LICENSE)

---

### 🧠 Overview

**DreamAPI** is a **comprehensive and modular API** designed as the **technical foundation** for the entire **Dreamin Ecosystem**.
Built for **Minecraft Paper 1.21.10**, it provides a rich set of tools and abstractions for creating **advanced plugins** with **minimal boilerplate** and **maximum flexibility**.

---

### ⚡ Key Features

- 🎯 **Multi-Module Architecture** — Composed of `api`, `core`, and `plugin-base` for clean separation.
- 🎨 **Rich API Surface** — Includes systems for animations, GUIs, commands, configs, databases, dialogs, events, and more.
- 🎮 **Game-Ready Components** — Built-in support for inventories, items, recipes, glowing effects, and game mechanics.
- 🔌 **Plugin Foundation** — Base classes and utilities to accelerate plugin development.
- 🌐 **HTTP & Database Integration** — Native support for external APIs and data persistence.
- 🧩 **Service Architecture** — Dependency injection and service management for scalable plugins.
- 🪶 **Performance Optimized** — Leveraging modern Java 21 features and Paper APIs.
- 🛠️ **Developer Experience** — Includes Lombok, annotations, and intuitive builders.

---

### 🧭 Project Goal

DreamAPI aims to provide a **production-ready foundation** for Minecraft plugin development by:
- offering **reusable components** for common plugin needs (GUIs, commands, configs, etc.),
- establishing **consistent patterns** and **best practices** across the Dreamin ecosystem,
- reducing **boilerplate code** through annotations and builders,
- enabling **rapid prototyping** with pre-built systems,
- ensuring **compatibility** and **interoperability** between all Dreamin modules.

DreamAPI powers **all projects in the Dreamin ecosystem**, including [DreamHud](https://github.com/Dreamin-MC/DreamHud), and is **battle-tested in production**.

---

### 🧱 Project Structure

```
📦 DreamAPI
┣ 📁 api/
┃ ┣ 📁 animation/        ─ Animation and interpolation systems
┃ ┣ 📁 cmd/              ─ Command framework and builders
┃ ┣ 📁 config/           ─ Configuration management
┃ ┣ 📁 database/         ─ Database abstractions and utilities
┃ ┣ 📁 dialog/           ─ Dialog and conversation systems
┃ ┣ 📁 event/            ─ Custom event handling
┃ ┣ 📁 game/             ─ Game mechanics and utilities
┃ ┣ 📁 glowing/          ─ Entity glowing effects
┃ ┣ 📁 gui/              ─ GUI creation framework
┃ ┣ 📁 http/             ─ HTTP client and REST utilities
┃ ┣ 📁 inventory/        ─ Advanced inventory management
┃ ┣ 📁 item/             ─ Item builders and utilities
┃ ┣ 📁 logger/           ─ Enhanced logging system
┃ ┣ 📁 recipe/           ─ Custom recipe management
┃ ┣ 📁 services/         ─ Service architecture and DI
┃ ┗ 📁 util/             ─ Common utilities and helpers
┣ 📁 core/               ─ Core implementations
┣ 📁 plugin-base/        ─ Base plugin class and utilities
┗ 📄 README.md
```

---

### 🎨 API Modules

DreamAPI is organized into **specialized modules** covering all aspects of plugin development:

| Module | Description                                       |
|--------|---------------------------------------------------|
| 🎬 **Animation** | Frame-based animations with interpolation support |
| ⌨️ **Command** | Modern command framework with auto-completion     |
| ⚙️ **Config** | YAML/JSON configuration with type-safe access     |
| 💾 **Database** | SQL and NoSQL database abstractions               |
| 💬 **Dialog** | In progress                                       |
| 🎯 **Event** | Custom event system with priority handling        |
| 🎮 **Game** | Game state management and mechanics               |
| ✨ **Glowing** | Per-player entity glowing with team support       |
| 🖼️ **GUI** | Inventory-based GUIs with InvUI integration       |
| 🌐 **HTTP** | HTTP client for REST API communication            |
| 🎒 **Inventory** | Advanced inventory serialization and management   |
| 🔧 **Item** | Fluent item builders with NBT support             |
| 📝 **Logger** | Colored logging with configurable levels          |
| 🍳 **Recipe** | Dynamic recipe creation and registration          |
| 🔌 **Services** | Dependency injection and lifecycle management     |
| 🛠️ **Utilities** | String, math, player, and world utilities         |

---

### 🧩 Developer Integration

#### 💻 Adding as a Dependency

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
    <version>0.0.9.1:all</version>
</dependency>
```

**Gradle (Kotlin DSL)**
```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.Dreamin-MC:DreamAPI:0.0.9.1:all")
}
```

**Gradle (Groovy)**
```groovy
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    compileOnly 'com.github.Dreamin-MC:DreamAPI:0.0.9.1:all'
}
```

---

### 🧃 Dreamin' Ecosystem

DreamAPI is the **cornerstone** of the **Dreamin Ecosystem**, providing shared functionality for all modules:

| Project | Description |
|---------|-------------|
| 🧠 **DreamAPI** | Central and modular API foundation *(you are here)* |
| 🌈 **DreamHud** | Dynamic HUD display system using bossbars |

More modules coming soon!

---

### 🛠️ Project Status

| Status | Version  | Compatibility |
|--------|----------|---------------|
| 🧪 In active development | `v0.0.9.1` | Paper 1.21.10 |
| 🔜 Beta Release | `v0.1.0` | Enhanced documentation & stability |
| 🚀 Stable Release | `v1.0.0` | Full API freeze & production-ready |

> ⚠️ *DreamAPI is under active development. Breaking changes may occur until v1.0.0.*
> 
> 📌 **Recommendation**: Pin to specific versions in production and test thoroughly before upgrading.

---

### 📦 Sub-Modules

DreamAPI consists of **four modules** that can be used independently or together:

- **`api`** — Pure interfaces and abstractions (no Paper dependency)
- **`core`** — Implementation of API contracts for Paper
- **`plugin-base`** — Base plugin class with lifecycle management

---

### 📚 Documentation

📘 **Documentation** *(coming soon)*
- API reference with Javadocs
- Integration guides and tutorials
- Architecture patterns and best practices
- Migration guides between versions

---

### 🤝 Contributing

Contributions are welcome! Before submitting a PR:

- Follow the existing **code style** (Lombok, builders, etc.)
- Ensure **backward compatibility** when possible
- Add **tests** for new features
- Update **documentation** and examples
- Test on **Paper 1.21.10+**

---

### 🔗 Dependencies

DreamAPI integrates with popular libraries for enhanced functionality:

- **[InvUI](https://github.com/NichtStudioCode/InvUI)** — Advanced inventory UIs
- **[LuckPerms API](https://github.com/LuckPerms/LuckPerms)** — Permission management
- **[Citizens API](https://github.com/CitizensDev/Citizens2)** — NPC support
- **[BKCommonLib](https://github.com/bergerhealer/BKCommonLib)** — Packet utilities
- **[Apache HttpClient 5](https://hc.apache.org/)** — HTTP communication
- **[Jackson](https://github.com/FasterXML/jackson)** — JSON processing

All external dependencies are marked as `compileOnly` — your plugin decides which to include.

---

### 📄 License

This project is distributed under the **GPL-3.0** license.
➡️ [See the LICENSE file](LICENSE)

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/Dreamin-MC/DreamAPI/issues)
- **Discord**: [Join our Discord](https://discord.gg/dreamin)

---

**Made with ❤️ by the Dreamin Studio**
