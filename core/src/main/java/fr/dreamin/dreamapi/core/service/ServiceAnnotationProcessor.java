package fr.dreamin.dreamapi.core.service;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.api.annotations.Inject;
import fr.dreamin.dreamapi.core.logger.DreamLoggerImpl;
import fr.dreamin.dreamapi.api.logger.DebugService;
import fr.dreamin.dreamapi.api.logger.DreamLogger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Automatically loads and registers all Bukkit services annotated with {@link DreamAutoService}.
 * <p>
 * This loader supports:
 * <ul>
 *   <li>Topological sorting of service load order (dependencies).</li>
 *   <li>Constructor injection via {@link Inject}.</li>
 *   <li>Fallback constructor logic.</li>
 *   <li>Lifecycle management (onLoad / onReload / onClose).</li>
 * </ul>
 */
public final class ServiceAnnotationProcessor implements Listener {

  private final @NotNull Plugin plugin;

  /** Stores loaded DreamService instances by implementation class */
  private final Map<Class<?>, DreamService> loadedServices = new HashMap<>();

  private final @NotNull Set<Class<?>> annotatedClasses;

  public ServiceAnnotationProcessor(final @NotNull Plugin plugin, final @NotNull Set<Class<?>> annotatedClasses) {
    this.plugin = plugin;
    this.annotatedClasses = annotatedClasses;
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  // ====== LOADER ======

  /**
   * Loads all services annotated with {@link DreamAutoService} in the correct dependency order.
   */
  public void process() {
    final var log = this.plugin.getLogger();
    final var start = System.currentTimeMillis();

    // Step 1: Retrieve all annotated classes
    final var annotated = this.annotatedClasses.stream()
    .filter(c -> c.isAnnotationPresent(DreamAutoService.class))
    .collect(Collectors.toSet());

    // Step 2: Build dependency graph
    Map<Class<?>, List<Class<?>>> graph = new HashMap<>();
    for (Class<?> clazz : annotated) {
      DreamAutoService annotation = clazz.getAnnotation(DreamAutoService.class);
      graph.put(clazz, Arrays.asList(annotation.dependencies()));
    }

    // Step 3: Compute load order via topological sort
    List<Class<?>> loadOrder;
    try {
      loadOrder = topologicalSort(graph);
    } catch (IllegalStateException e) {
      log.severe(String.format("[DreamService] Circular dependency detected: %s", e.getMessage()));
      return;
    }

    int ok = 0;
    int fail = 0;

    // Step 4: Instantiate and register services in the correct order
    for(Class<?> implClass : loadOrder) {
      try {
        instantiateAndRegisterService(implClass);
        ok++;
      } catch (Exception e) {
        e.printStackTrace();
        log.severe(String.format("[DreamService] Failed to load service %s: %s", implClass.getSimpleName(), e.getMessage()));
        fail++;
      }
    }

    final var end = System.currentTimeMillis();
    log.info(String.format(
      "[DreamService] Loaded %d services (%d failed) in %dms",
      ok, fail, end - start
    ));
  }

  /**
   * Loads a single service from its implementation class.
   * <p>
   * This method:
   * <ul>
   *     <li>Checks for the {@link DreamAutoService} annotation.</li>
   *     <li>Respects lifecycle hooks (onLoad) and updates {@link DreamService.ServiceStatus}.</li>
   *     <li>Registers the service in Bukkit's {@link org.bukkit.plugin.ServicesManager}.</li>
   * </ul>
   *
   * @param implClass The implementation class of the service
   */
  public void loadServiceFromClass(final @NotNull Class<?> implClass) {
    final var log = this.plugin.getLogger();

    try {
      instantiateAndRegisterService(implClass);
    } catch (Exception e) {
      e.printStackTrace();
      log.severe(String.format("[DreamService] Failed to load service %s: %s",
        implClass.getSimpleName(), e.getMessage()));
    }
  }

  /**
   * Reloads all currently registered services implementing {@link DreamService}.
   */
  public void reloadAllServices() {
    this.loadedServices.values().forEach(this::reloadService);
  }

  /**
   * Closes all currently registered services implementing {@link DreamService}.
   */
  public void closeAllServices() {
    final var copy = List.copyOf(this.loadedServices.values());
    copy.forEach(this::closeService);

    this.loadedServices.clear();
  }

  /** Calls onClose() on a specific service and updates its status. */
  public void closeService(final @NotNull DreamService service) {
    try {
      setStatus(service, DreamService.ServiceStatus.CLOSED);
      service.onClose();

      // Unregister listener if the service is a Listener instance
      if (service instanceof Listener ls)
        HandlerList.unregisterAll(ls);

    } catch (Exception e) {
      this.plugin.getLogger().severe(String.format("[DreamService] Failed to close service %s: %s", service.getName(), e.getMessage()));
      e.printStackTrace();
    }
    this.loadedServices.remove(service.getClass());
  }

  /** Calls onLoad() on a specific service and updates its status. */
  public void loadService(final @NotNull DreamService service) {
    try {
      setStatus(service, DreamService.ServiceStatus.LOADING);
      service.onLoad(this.plugin);
      setStatus(service, DreamService.ServiceStatus.LOADED);
    } catch (Exception e) {
      setStatus(service, DreamService.ServiceStatus.FAILED);
      service.onFailed();
      plugin.getLogger().severe(String.format("[DreamService] Failed to load service %s: %s", service.getName(), e.getMessage()));
      e.printStackTrace();
    }
    this.loadedServices.put(service.getClass(), service);
  }

  /** Calls onReload() on a specific service if it can be reloaded. */
  public void reloadService(final @NotNull DreamService service) {
    if (!service.canReload()) {
      this.plugin.getLogger().warning(String.format("[DreamService] %s cannot be reloaded in current status: %s", service.getName(), service.getStatus()));
      return;
    }
    try {
      setStatus(service, DreamService.ServiceStatus.RELOADING);
      service.onReload();
      setStatus(service, DreamService.ServiceStatus.LOADED);
    } catch (Exception e) {
      setStatus(service, DreamService.ServiceStatus.FAILED);
      service.onFailed();
      this.plugin.getLogger().severe(String.format("[DreamService] Failed to reload service %s: %s", service.getName(), e.getMessage()));
      e.printStackTrace();
    }
  }

  /** Calls onReset() on a specific service. */
  public void resetService(final @NotNull DreamService service) {
    try {
      service.onReset();
    } catch (Exception e) {
      this.plugin.getLogger().severe(String.format("[DreamService] Failed to reset service %s: %s", service.getName(), e.getMessage()));
      e.printStackTrace();
    }
  }

  /**
   * Creates or instantiates a non-service class using the same
   * constructor resolution & injection logic as services.
   *
   * This does NOT register the class as a service.
   */
  public <T> T createInjectedInstance(final @NotNull Class<T> clazz) {
    try {
      final var ctor = resolveConstructor(clazz);
      final var args = resolveConstructorArgs(ctor);

      @SuppressWarnings("unchecked")
      final var instance = (T) ctor.newInstance(args);
      injectLoggerIntoService(instance);

      if (instance instanceof Listener ls)
        Bukkit.getPluginManager().registerEvents(ls, this.plugin);

      return instance;

    } catch (Exception e) {
      throw new RuntimeException(String.format("[DreamService] Failed to create injected instance of %s: %s", clazz.getName(), e.getMessage()), e);
    }
  }

  /**
   * Returns a DreamService instance by its implementation class.
   *
   * @param clazz The class implementing DreamService
   * @param <T>   The concrete service type
   * @return The loaded service instance, or null if not found
   */
  public <T extends DreamService> T getDreamService(@NotNull Class<T> clazz) {
    return clazz.cast(this.loadedServices.get(clazz));
  }

  /**
   * Returns an unmodifiable view of all loaded services.
   */
  public Map<Class<?>, DreamService> getAllLoadedServices() {
    return Collections.unmodifiableMap(this.loadedServices);
  }

  public void exportServiceDiagram(final @NotNull File output) {
    try (var writer = new PrintWriter(output)) {

      writer.println("@startuml");
      writer.println("skinparam classAttributeIconSize 0");

      for (final var entry : loadedServices.entrySet()) {
        final var cls = entry.getKey();
        final var svc = entry.getValue();

        final var statusColor = switch (svc.getStatus()) {
          case LOADED -> "##palegreen";
          case LOADING -> "##lightyellow";
          case FAILED -> "##salmon";
          case CLOSED -> "##lightgray";
          default -> "##white";
        };

        final var auto = cls.getAnnotation(DreamAutoService.class);
        final var priority = auto != null ? auto.priority().name() : "N/A";

        writer.printf("class %s %s {\n", cls.getSimpleName(), statusColor);
        writer.printf("  status: %s\n", svc.getStatus());
        writer.printf("  priority: %s\n", priority);
        writer.println("}");
      }

      for (final var entry : loadedServices.keySet()) {
        final var cls = entry;

        final var auto = cls.getAnnotation(DreamAutoService.class);
        if (auto == null) continue;

        for (final var dep : auto.dependencies()) {
          writer.printf("%s --> %s\n", cls.getSimpleName(), dep.getSimpleName());
        }
      }

      writer.println("@enduml");
    } catch (Exception e) {
      this.plugin.getLogger().severe("[DreamService] Failed to export UML: " + e.getMessage());
      e.printStackTrace();
    }
  }

  // ###############################################################
  // ----------------------- INTERNAL LOADER -----------------------
  // ###############################################################

  private DreamService instantiateAndRegisterService(final @NotNull Class<?> implClass) throws Exception {
    final var annotation = implClass.getAnnotation(DreamAutoService.class);
    if (annotation == null)
      throw new IllegalStateException(String.format("[DreamService] Class %s is not annotated with @DreamAutoService", implClass.getSimpleName()));

    final var iface = (Class<Object>) annotation.value();
    final var priority = annotation.priority();

    final var ctor = resolveConstructor(implClass);

    final var args = resolveConstructorArgs(ctor);

    final var instance = ctor.newInstance(args);
    if (!(instance instanceof DreamService ds))
      throw new IllegalStateException(String.format("[DreamService] Class %s does not implement DreamService", implClass.getSimpleName()));

    if (instance instanceof Listener ls)
      Bukkit.getPluginManager().registerEvents(ls, this.plugin);

    injectLoggerIntoService(instance);

    setStatus(ds, DreamService.ServiceStatus.LOADING);
    ds.onLoad(this.plugin);
    setStatus(ds, DreamService.ServiceStatus.LOADED);

    Bukkit.getServicesManager().register(iface, ds, this.plugin, priority);

    this.loadedServices.put(implClass, ds);

    return ds;
  }

  // ###############################################################
  // ---------------------- LOGGER INJECTION -----------------------
  // ###############################################################

  private void injectLoggerIntoService(final @NotNull Object instance) {
    for (final var field : instance.getClass().getDeclaredFields()) {
      if (field.getType() == DreamLogger.class) {
        final DebugService debugService;

        try {
          debugService = DreamAPI.getAPI().getService(DebugService.class);
        } catch (IllegalStateException ex) {
          return;
        }

        final var logger = new DreamLoggerImpl(
          this.plugin,
          debugService,
          instance.getClass().getSimpleName(),
          instance
        );

        field.setAccessible(true);
        try {
          field.set(instance, logger);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(String.format("[DreamService] Failed to inject logger for ", instance.getClass()));
        }
        break;
      }
    }

  }

  // ###############################################################
  // ---------------------- CONSTRUCTOR LOGIC ----------------------
  // ###############################################################

  private Constructor<?> resolveConstructor(final @NotNull Class<?> clazz) {
    final var constructors = clazz.getDeclaredConstructors();

    // 1) If a constructor has @Inject → use it
    for (final var c : constructors) {
      if (c.isAnnotationPresent(Inject.class)) {
        c.setAccessible(true);
        return c;
      }
    }

    // 2) If the class has @Inject → use the best constructor
    if (clazz.isAnnotationPresent(Inject.class)) {
      var best = constructors[0];
      for (final var c : constructors) {
        if (c.getParameterCount() > best.getParameterCount())
          best = c;
      }

      best.setAccessible(true);
      return best;
    }

    // 3) Fallback: constructor with only allowed params (Plugin, DreamService, DreamLogger)
    for (final var c : constructors) {
      var compatible = true;

      for (final var param : c.getParameterTypes()) {
        if (!Plugin.class.isAssignableFrom(param)
          && param != DreamLogger.class
          && !DreamService.class.isAssignableFrom(param)) {
          compatible = false;
          break;
        }
      }

      if (compatible) {
        c.setAccessible(true);
        return c;
      }
    }

    // 4) Fallback: no-arg constructor
    try {
      final var c = clazz.getDeclaredConstructor();
      c.setAccessible(true);
      return c;
    } catch (Exception e) {
      throw new RuntimeException("[DreamService] No suitable constructor found for " + clazz.getName());
    }
  }

  private Object[] resolveConstructorArgs(final @NotNull Constructor<?> constructor) {
    final var params = constructor.getParameterTypes();
    final var args = new Object[params.length];

    for (var i = 0; i < params.length; i++) {
      final var param = params[i];

      // Inject Plugin
      if (Plugin.class.isAssignableFrom(param)) {
        args[i] = this.plugin;
        continue;
      }

      if (param == DreamLogger.class) {
        args[i] = null;
        continue;
      }

      // Inject DreamService
      var resolved = false;
      for (final var service : loadedServices.values()) {
        if (param.isAssignableFrom(service.getClass())) {
          args[i] = service;
          resolved = true;
          break;
        }
      }

      if (!resolved) {
        throw new RuntimeException(
          String.format("[DreamService] Unable to resolve dependency: %s for constructor %s", param.getName(), constructor)
        );
      }
    }

    return args;
  }

  // ###############################################################
  // ---------------------- TOPOLOGICAL SORT -----------------------
  // ###############################################################

  /**
   * Topological sort to determine proper service load order based on dependencies.
   *
   * @param graph Dependency graph of services
   * @return List of classes sorted in load order
   */
  private List<Class<?>> topologicalSort(Map<Class<?>, List<Class<?>>> graph) {
    List<Class<?>> order = new ArrayList<>();
    Set<Class<?>> visited = new HashSet<>();
    Set<Class<?>> visiting = new HashSet<>();

    for (Class<?> node : graph.keySet()) {
      visit(node, graph, visited, visiting, order);
    }
    return order;
  }

  private void visit(Class<?> node, Map<Class<?>, List<Class<?>>> graph, Set<Class<?>> visited, Set<Class<?>> visiting, List<Class<?>> order) {
    if (visited.contains(node)) return;
    if (visiting.contains(node)) throw new IllegalStateException(node.getSimpleName());

    visiting.add(node);
    for (Class<?> dep : graph.getOrDefault(node, Collections.emptyList())) {
      visit(dep, graph, visited, visiting, order);
    }
    visiting.remove(node);
    visited.add(node);
    order.add(node);
  }

  // ###############################################################
  // --------------------------- STATUS ----------------------------
  // ###############################################################

  /**
   * Utility method to set the status of a DreaminService via reflection.
   * <p>
   * This is necessary because the interface has a default getStatus() method.
   */
  private void setStatus(DreamService service, DreamService.ServiceStatus status) {
    service.__setStatus(status);
  }

  // ###############################################################
  // ---------------------- LISTENER METHODS -----------------------
  // ###############################################################

  @EventHandler
  private void onPluginDisable(PluginDisableEvent event) {
    if (event.getPlugin() == this.plugin) closeAllServices();
  }

}
