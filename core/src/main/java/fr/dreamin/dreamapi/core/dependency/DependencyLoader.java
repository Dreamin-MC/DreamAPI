package fr.dreamin.dreamapi.core.dependency;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.dependency.DependencyType;
import fr.dreamin.dreamapi.api.dependency.RequiresDependency;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public final class DependencyLoader {

  public static boolean canLoad(
    final @NotNull Class<?> clazz
  ) {
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
      if (strict) throw new IllegalStateException(msg);
      plugin.getLogger().warning(msg);
    }

    if (!hard && loadAfter && !DependencyReader.isSoft(name)) {
      final var msg = String.format(
        "Plugin '%s' is soft dependency but not declared load AFTER (paper-plugin.yml)!",
        name
      );

      if (strict) throw new IllegalStateException(msg);
      plugin.getLogger().warning(msg);

    }

    return true;
  }

}
