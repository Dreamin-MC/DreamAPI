package fr.dreamin.dreamapi.core.scanner;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class ClassScanner {

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
    final Set<Class<?>> classes = new HashSet<>();
    final var path = scanPackage.replace('.', '/');
    final var classLoader = plugin.getClass().getClassLoader();
    final var context = new ScanContext(scanPackage, recursive, classes, classLoader);
    final var resources = classLoader.getResources(path);
    plugin.getLogger().info(String.format("[ClassScanner] Starting scan in path: %s (from package: %s), has resources? %b", path, scanPackage, resources.hasMoreElements()));

    while (resources.hasMoreElements()) {
      scanResource(resources.nextElement(), context);
    }

    plugin.getLogger().info(String.format("[ClassScanner] Scan completed: %d classes found", classes.size()));
    return classes;
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private static void validateParentPackageLevels(final int parentPackageLevels) {
    if (parentPackageLevels < 0)
      throw new IllegalArgumentException("parentPackageLevels must be >= 0");
  }

  private static void scanResource(final @NotNull URL resource, final @NotNull ScanContext context) throws IOException, ClassNotFoundException {
    final var protocol = resource.getProtocol();

    if ("file".equals(protocol)) {
      scanFileResource(resource, context);
      return;
    }

    if ("jar".equals(protocol))
      scanJarResource(resource, context);
  }

  private static void scanFileResource(final @NotNull URL resource, final @NotNull ScanContext context) throws ClassNotFoundException {
    final var directory = new File(URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8));
    if (directory.exists())
      findClassesInDirectory(context.scanPackage(), directory, context);
  }

  private static void scanJarResource(final @NotNull URL resource, final @NotNull ScanContext context) throws IOException, ClassNotFoundException {
    final var jarConnection = (JarURLConnection) resource.openConnection();
    final var jarFile = jarConnection.getJarFile();
    scanJarEntries(jarFile.entries(), context);
  }

  private static void scanJarEntries(final @NotNull Enumeration<java.util.jar.JarEntry> entries, final @NotNull ScanContext context) throws ClassNotFoundException {
    while (entries.hasMoreElements()) {
      final var entry = entries.nextElement();
      final var name = entry.getName();
      if (!entry.isDirectory() && name.endsWith(".class")) {
        final var className = name.replace('/', '.').substring(0, name.length() - 6);
        if (matchesPackageScope(className, context.scanPackage(), context.recursive()))
          context.classes().add(Class.forName(className, false, context.classLoader()));
      }
    }
  }

  private static void findClassesInDirectory(final @NotNull String packageName, final @NotNull File directory, final @NotNull ScanContext context) throws ClassNotFoundException {
    for (final var file : Objects.requireNonNull(directory.listFiles())) {
      if (file.isDirectory() && context.recursive())
        findClassesInDirectory(String.format("%s.%s", packageName, file.getName()), file, context);
      else if (file.getName().endsWith(".class")) {
        final var className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
        if (matchesPackageScope(className, packageName, context.recursive()))
          context.classes().add(Class.forName(className, false, context.classLoader()));
      }
    }
  }

  private record ScanContext(
    @NotNull String scanPackage,
    boolean recursive,
    @NotNull Set<Class<?>> classes,
    @NotNull ClassLoader classLoader
  ) {}

  private static boolean matchesPackageScope(final @NotNull String className, final @NotNull String scanPackage, final boolean recursive) {
    final var prefix = scanPackage + ".";
    if (!className.startsWith(prefix))
      return false;

    if (recursive)
      return true;

    final var relativeName = className.substring(prefix.length());
    return !relativeName.contains(".");
  }

  private static @NotNull String trimPackage(final @NotNull String packageName, final int parentPackageLevels) {
    String current = packageName;

    for (int i = 0; i < parentPackageLevels; i++) {
      final int dot = current.lastIndexOf('.');
      if (dot < 0)
        throw new IllegalArgumentException(String.format("Cannot trim %d levels from package '%s'", parentPackageLevels, packageName));
      current = current.substring(0, dot);
    }

    return current;
  }
}