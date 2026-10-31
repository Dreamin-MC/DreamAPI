package fr.dreamin.dreamapi.core.scanner;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;

public final class ClassScanner {

  private static final String INDEX_RESOURCE = "META-INF/dreamapi-classes.txt";

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /** Scans for all classes in a package, optionally including subpackages. */
  public static Set<Class<?>> getClasses(final @NotNull Plugin plugin, final @NotNull String packageName, final boolean recursive) throws IOException, ClassNotFoundException {
    return getClasses(plugin, packageName, recursive, 0);
  }

  /**
   * Scans for all classes from a package, optionally including subpackages,
   * with the ability to trim parent package levels before scanning.
   */
  public static Set<Class<?>> getClasses(final @NotNull Plugin plugin, final @NotNull String packageName, final boolean recursive, final int parentPackageLevels) throws IOException, ClassNotFoundException {
    validateParentPackageLevels(parentPackageLevels);

    final var scanPackage = trimPackage(packageName, parentPackageLevels);
    final var classes = new HashSet<Class<?>>();
    final var classLoader = plugin.getClass().getClassLoader();

    // Fast-path 1: Check precompiled index file in META-INF
    if (loadFromIndex(classLoader, scanPackage, recursive, classes, plugin)) {
      plugin.getLogger().info(String.format("[ClassScanner] Index loaded: %d classes found for package %s", classes.size(), scanPackage));
      return classes;
    }

    final var path = scanPackage.replace('.', '/');
    final var pathPrefix = path.isEmpty() ? "" : path + "/";
    final var context = new ScanContext(scanPackage, pathPrefix, recursive, classes, classLoader, plugin);
    final var resources = classLoader.getResources(path);
    plugin.getLogger().info(String.format("[ClassScanner] Starting scan in path: %s (from package: %s), has resources? %b", path, scanPackage, resources.hasMoreElements()));

    while (resources.hasMoreElements())
      scanResource(resources.nextElement(), context);

    plugin.getLogger().info(String.format("[ClassScanner] Scan completed: %d classes found", classes.size()));
    return classes;
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private static boolean loadFromIndex(final @NotNull ClassLoader classLoader, final @NotNull String scanPackage, final boolean recursive, final @NotNull Set<Class<?>> classes, final @NotNull Plugin plugin) {
    final var is = classLoader.getResourceAsStream(INDEX_RESOURCE);
    if (is == null)
      return false;

    try (final var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      var line = reader.readLine();
      while (line != null) {
        final var trimmed = line.trim();
        if (!trimmed.isEmpty() && !trimmed.startsWith("#") && matchesPackageScope(trimmed, scanPackage, recursive))
          loadClassSafely(trimmed, classLoader, classes, plugin);
        line = reader.readLine();
      }
      return !classes.isEmpty();
    } catch (IOException e) {
      plugin.getLogger().warning("[ClassScanner] Error reading precompiled index: " + e.getMessage());
      return false;
    }
  }

  private static void validateParentPackageLevels(final int parentPackageLevels) {
    if (parentPackageLevels < 0)
      throw new IllegalArgumentException("parentPackageLevels must be >= 0");
  }

  private static void scanResource(final @NotNull URL resource, final @NotNull ScanContext context) throws IOException {
    final var protocol = resource.getProtocol();

    if ("file".equals(protocol)) {
      scanFileResource(resource, context);
      return;
    }

    if ("jar".equals(protocol))
      scanJarResource(resource, context);
  }

  private static void scanFileResource(final @NotNull URL resource, final @NotNull ScanContext context) {
    final var directory = new File(URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8));
    if (directory.exists())
      findClassesInDirectory(context.scanPackage(), directory, context);
  }

  private static void scanJarResource(final @NotNull URL resource, final @NotNull ScanContext context) throws IOException {
    final var jarConnection = (JarURLConnection) resource.openConnection();
    final var jarFile = jarConnection.getJarFile();
    scanJarEntries(jarFile.entries(), context);
  }

  private static void scanJarEntries(final @NotNull Enumeration<JarEntry> entries, final @NotNull ScanContext context) {
    final var prefix = context.pathPrefix();
    final var recursive = context.recursive();

    while (entries.hasMoreElements()) {
      final var entry = entries.nextElement();
      final var name = entry.getName();

      if (entry.isDirectory() || !name.endsWith(".class"))
        continue;
      if (!prefix.isEmpty() && !name.startsWith(prefix))
        continue;
      if (!recursive && !prefix.isEmpty() && name.indexOf('/', prefix.length()) != -1)
        continue;

      final var className = name.substring(0, name.length() - 6).replace('/', '.');
      if (matchesPackageScope(className, context.scanPackage(), recursive))
        loadClassSafely(className, context.classLoader(), context.classes(), context.plugin());
    }
  }

  private static void findClassesInDirectory(final @NotNull String packageName, final @NotNull File directory, final @NotNull ScanContext context) {
    final var files = directory.listFiles();
    if (files == null)
      return;

    for (final var file : files) {
      if (file.isDirectory() && context.recursive()) {
        findClassesInDirectory(packageName + '.' + file.getName(), file, context);
      } else if (file.getName().endsWith(".class")) {
        final var className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
        if (matchesPackageScope(className, packageName, context.recursive()))
          loadClassSafely(className, context.classLoader(), context.classes(), context.plugin());
      }
    }
  }

  private static void loadClassSafely(final @NotNull String className, final @NotNull ClassLoader classLoader, final @NotNull Set<Class<?>> classes, final @NotNull Plugin plugin) {
    try {
      classes.add(Class.forName(className, false, classLoader));
    } catch (ClassNotFoundException | LinkageError e) {
      plugin.getLogger().fine(String.format("[ClassScanner] Skipped unresolvable class %s: %s", className, e.getMessage()));
    }
  }

  private record ScanContext(
    @NotNull String scanPackage,
    @NotNull String pathPrefix,
    boolean recursive,
    @NotNull Set<Class<?>> classes,
    @NotNull ClassLoader classLoader,
    @NotNull Plugin plugin
  ) {}

  private static boolean matchesPackageScope(final @NotNull String className, final @NotNull String scanPackage, final boolean recursive) {
    if (scanPackage.isEmpty())
      return true;
    final var prefix = scanPackage + ".";
    if (!className.startsWith(prefix))
      return false;

    if (recursive)
      return true;

    final var relativeName = className.substring(prefix.length());
    return !relativeName.contains(".");
  }

  private static @NotNull String trimPackage(final @NotNull String packageName, final int parentPackageLevels) {
    var current = packageName;

    for (var i = 0; i < parentPackageLevels; i++) {
      final var dot = current.lastIndexOf('.');
      if (dot < 0)
        throw new IllegalArgumentException(String.format("Cannot trim %d levels from package '%s'", parentPackageLevels, packageName));
      current = current.substring(0, dot);
    }

    return current;
  }
}