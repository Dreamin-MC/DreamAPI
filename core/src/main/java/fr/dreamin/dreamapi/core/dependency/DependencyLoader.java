package fr.dreamin.dreamapi.core.dependency;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.dependency.DependencyType;
import fr.dreamin.dreamapi.api.dependency.RequiresDependency;
import fr.dreamin.dreamapi.api.dependency.RequiresVersion;
import fr.dreamin.dreamapi.api.util.VersionUtils;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public final class DependencyLoader {

  public static boolean checkVersion(final @NotNull Class<?> clazz) {
    Objects.requireNonNull(clazz, "Class cannot be null");
    var req = clazz.getAnnotation(RequiresVersion.class);
    if (req == null) {
      final var pkg = clazz.getPackage();
      if (pkg != null)
        req = pkg.getAnnotation(RequiresVersion.class);
    }
    if (req == null)
      return true;

    return VersionUtils.isWithinRange(req.min(), req.max());
  }

  public static boolean canLoad(
    final @NotNull Class<?> clazz
  ) {
    if (!checkVersion(clazz))
      return false;

    final var plugin = DreamAPI.getAPI().plugin();

    final var dep = clazz.getAnnotation(RequiresDependency.class);
    if (dep == null) return true;

    final var name = dep.value();
    final var hard = dep.hard();
    final var strict = dep.strict();
    final var loadAfter = dep.loadAfter();

    final var installed = Bukkit.getPluginManager().isPluginEnabled(name);
    if (!installed) return false;

    final var type = DependencyReader.getDeclaredType(name);

    if (hard && type != DependencyType.HARD) {
      final var msg = String.format(
        "Plugin %s is marked as hard dependency but not declared as such in plugin.yml or paper-plugin.yml!",
        name
      );

      plugin.getLogger().warning(msg);
      return false;
    }

    if (!hard && loadAfter && !DependencyReader.isSoft(name)) {
      final var msg = String.format(
        "Plugin '%s' is soft dependency but not declared load AFTER in plugin.yml or paper-plugin.yml!",
        name
      );

      plugin.getLogger().warning(msg);
      return false;
    }

    return true;
  }

}
