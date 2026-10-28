package fr.dreamin.dreamapi.test.e2e.tier1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Modular Architecture & Gradle Multi-Project Tests (Tiers 1 & 2)")
public class ModularArchitectureTier1Test {

  private static final Path PROJECT_ROOT = findProjectRoot();

  private static final List<String> EXPECTED_22_SUBPROJECTS = List.of(
    "core", "nms", "item", "gui", "recipe", "cuboid", "particle",
    "hologram", "glowing", "worldborder", "world", "navigate", "lang",
    "dialog", "skin", "database", "http", "command", "animation",
    "hooks", "all", "example-plugin"
  );

  private static final List<String> EXPECTED_20_LIBRARIES = List.of(
    "core", "nms", "item", "gui", "recipe", "cuboid", "particle",
    "hologram", "glowing", "worldborder", "world", "navigate", "lang",
    "dialog", "skin", "database", "http", "command", "animation",
    "hooks"
  );

  private static Path findProjectRoot() {
    var current = Path.of("").toAbsolutePath();
    while (current != null) {
      if (Files.exists(current.resolve("settings.gradle")) && Files.exists(current.resolve("build.gradle"))) {
        return current;
      }
      current = current.getParent();
    }
    return Path.of("O:/Dreamin/projects/java/DreamAPI").toAbsolutePath();
  }

  // =========================================================================
  // TIER 1: Feature Coverage (Gradle Layout, 22 Subprojects, Publishing)
  // =========================================================================

  @Nested
  @DisplayName("Tier 1: Feature Coverage")
  class Tier1FeatureCoverage {

    @Test
    @DisplayName("TC-T1-MS-01: settings.gradle contains all 22 subprojects and excludes plugin-base")
    void testSettingsGradleContainsAll22Subprojects() throws IOException {
      final var settingsPath = PROJECT_ROOT.resolve("settings.gradle");
      assertTrue(Files.exists(settingsPath), "settings.gradle must exist at project root");

      final var content = Files.readString(settingsPath);
      assertFalse(content.contains("\"plugin-base\""), "settings.gradle must not include plugin-base");
      for (final var subproject : EXPECTED_22_SUBPROJECTS)
        assertTrue(content.contains("\"" + subproject + "\""),
          "settings.gradle must include subproject: " + subproject);
    }

    @Test
    @DisplayName("TC-T1-MS-02: All subproject directories exist on disk")
    void testAllSubprojectDirectoriesExist() {
      for (final var subproject : EXPECTED_22_SUBPROJECTS) {
        final var dir = PROJECT_ROOT.resolve(subproject);
        assertTrue(Files.isDirectory(dir),
          "Directory for subproject '" + subproject + "' must exist at: " + dir);
      }
    }

    @Test
    @DisplayName("TC-T1-MS-03: Root build.gradle configures dreamapi-<module> publication naming")
    void testRootBuildGradleConfiguresMavenPublishArtifactName() throws IOException {
      final var rootBuildPath = PROJECT_ROOT.resolve("build.gradle");
      final var content = Files.readString(rootBuildPath);

      assertTrue(content.contains("archivesName.set(\"dreamapi-${project.name}\")")
        || content.contains("dreamapi-${project.name}"),
        "Root build.gradle must configure archivesName with prefix 'dreamapi-' for JitPack");
      assertTrue(content.contains("artifactId = \"dreamapi-${project.name}\""),
        "Root build.gradle must configure artifactId as dreamapi-${project.name}");
    }

    @Test
    @DisplayName("TC-T1-MS-04: No build.gradle files reference retired ':api' subproject")
    void testNoObsoleteApiProjectReferences() throws IOException {
      final var buildFiles = new ArrayList<Path>();
      try (var stream = Files.walk(PROJECT_ROOT)) {
        stream.filter(p -> p.getFileName().toString().equals("build.gradle"))
          .forEach(buildFiles::add);
      }

      for (final var buildFile : buildFiles) {
        final var content = Files.readString(buildFile);
        assertFalse(content.contains("project(\":api\")") || content.contains("project(':api')"),
          "File " + buildFile + " must not reference obsolete :api project");
      }
    }

    @Test
    @DisplayName("TC-T1-MS-05: Merged Java package structure under fr.dreamin.dreamapi")
    void testPublicApiAndImplementationPackageStructure() {
      final var coreJava = PROJECT_ROOT.resolve("core/src/main/java/fr/dreamin/dreamapi");
      assertTrue(Files.exists(coreJava.resolve("api")),
        "Core must contain api package at " + coreJava.resolve("api"));
      assertTrue(Files.exists(coreJava.resolve("core")),
        "Core must contain core implementation package at " + coreJava.resolve("core"));
    }

    @Test
    @DisplayName("TC-T1-MS-06: No circular dependencies between Gradle subprojects")
    void testNoCircularDependenciesInProjectGraph() throws IOException {
      final var graph = buildProjectDependencyGraph();

      for (final var node : graph.keySet()) {
        final var visited = new HashSet<String>();
        final var recStack = new HashSet<String>();
        assertFalse(hasCycle(node, graph, visited, recStack),
          "Circular dependency detected starting from project :" + node);
      }
    }
  }

  // =========================================================================
  // TIER 2: Boundary & Corner Cases (Aggregator, Decoupled Deps, Versioning)
  // =========================================================================

  @Nested
  @DisplayName("Tier 2: Boundary & Corner Cases")
  class Tier2BoundaryAndCornerCases {

    @Test
    @DisplayName("TC-T2-MS-01: 'all' aggregator build.gradle includes all 20 library subprojects")
    void testAllAggregatorAggregatesAll20Libraries() throws IOException {
      final var allBuild = PROJECT_ROOT.resolve("all/build.gradle");
      assertTrue(Files.exists(allBuild), "all/build.gradle must exist");

      final var content = Files.readString(allBuild);
      assertFalse(content.contains("project(\":plugin-base\")"), "all/build.gradle must not include plugin-base");
      for (final var lib : EXPECTED_20_LIBRARIES)
        assertTrue(content.contains("project(\":" + lib + "\")")
          || content.contains("project(':" + lib + "')"),
          "all/build.gradle must aggregate library: " + lib);
    }

    @Test
    @DisplayName("TC-T2-MS-02: plugin-base is completely eliminated under Requirement R0")
    void testPluginBaseEliminated() {
      final var pbDir = PROJECT_ROOT.resolve("plugin-base");
      assertFalse(Files.exists(pbDir), "plugin-base directory must not exist under Requirement R0");
    }

    @Test
    @DisplayName("TC-T2-MS-03: nms does not depend on worldborder (circular dependency prevention)")
    void testNmsDoesNotDependOnWorldborder() throws IOException {
      final var nmsBuild = PROJECT_ROOT.resolve("nms/build.gradle");
      if (Files.exists(nmsBuild)) {
        final var content = Files.readString(nmsBuild);
        assertFalse(content.contains("worldborder"),
          "nms must not depend on worldborder; WorldBorderAction is defined in nms to avoid this cycle");
      }
    }

    @Test
    @DisplayName("TC-T2-MS-04: Root build.gradle specifies version for junit-platform-launcher")
    void testRootBuildGradleJunitPlatformLauncherVersion() throws IOException {
      final var rootBuild = PROJECT_ROOT.resolve("build.gradle");
      final var content = Files.readString(rootBuild);
      assertTrue(content.contains("testRuntimeOnly(\"org.junit.platform:junit-platform-launcher:$junitVersion\")")
        || content.contains("junit-platform-launcher:$junitVersion")
        || content.contains("junit-platform-launcher:"),
        "Root build.gradle must declare version for junit-platform-launcher");
    }

    @Test
    @DisplayName("TC-T2-MS-05: Subproject group and version conventions")
    void testSubprojectGroupAndVersionConventions() throws IOException {
      final var rootBuild = PROJECT_ROOT.resolve("build.gradle");
      final var content = Files.readString(rootBuild);

      assertTrue(content.contains("group = \"fr.dreamin\""), "Root build.gradle must specify group 'fr.dreamin'");
      assertTrue(content.contains("versions.properties") || content.contains("version = \"0.6.0\"") || content.contains("version = \"0.5.8\""),
        "Root build.gradle must specify version via versions.properties or explicit version");
    }

    @Test
    @DisplayName("TC-T2-MS-06: example-plugin excluded from maven-publish")
    void testExamplePluginExcludedFromMavenPublish() throws IOException {
      final var rootBuild = PROJECT_ROOT.resolve("build.gradle");
      final var content = Files.readString(rootBuild);

      assertTrue(content.contains("if (project.name != \"example-plugin\")")
        || content.contains("project.name != 'example-plugin'"),
        "example-plugin should be excluded from Maven publication in root build.gradle");
    }
  }

  // Dependency graph helper
  private Map<String, Set<String>> buildProjectDependencyGraph() throws IOException {
    final var graph = new HashMap<String, Set<String>>();
    final var depPattern = Pattern.compile("(?:api|implementation|compileOnly)\\s*\\(?\\s*project\\([\"']:([a-zA-Z0-9_-]+)[\"']\\)");

    for (final var subproject : EXPECTED_22_SUBPROJECTS) {
      final var deps = new HashSet<String>();
      final var buildFile = PROJECT_ROOT.resolve(subproject).resolve("build.gradle");
      if (Files.exists(buildFile)) {
        final var content = Files.readString(buildFile);
        final var matcher = depPattern.matcher(content);
        while (matcher.find()) {
          deps.add(matcher.group(1));
        }
      }
      graph.put(subproject, deps);
    }
    return graph;
  }

  private boolean hasCycle(String current, Map<String, Set<String>> graph, Set<String> visited, Set<String> recStack) {
    if (recStack.contains(current)) return true;
    if (visited.contains(current)) return false;

    visited.add(current);
    recStack.add(current);

    final var neighbors = graph.getOrDefault(current, Collections.emptySet());
    for (final var neighbor : neighbors)
      if (hasCycle(neighbor, graph, visited, recStack)) return true;

    recStack.remove(current);
    return false;
  }
}
