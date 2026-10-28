package fr.dreamin.dreamapi.plugin;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.LoadMode;
import fr.dreamin.dreamapi.api.annotations.EnableServices;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.core.ApiProviderImpl;
import fr.dreamin.dreamapi.core.cmd.BroadcastCmd;
import fr.dreamin.dreamapi.core.cmd.DebugCmd;
import fr.dreamin.dreamapi.core.cmd.GlobalSuggestCmd;
import fr.dreamin.dreamapi.core.cmd.ServiceCmd;
import fr.dreamin.dreamapi.core.event.scanner.ListenerAnnotationProcessor;
import fr.dreamin.dreamapi.core.logger.DebugServiceImpl;
import fr.dreamin.dreamapi.core.logger.PlayerDebugServiceImpl;
import fr.dreamin.dreamapi.core.scanner.ClassScanner;
import fr.dreamin.dreamapi.core.service.ServiceAnnotationProcessor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * <p>
 * Abstract base class for all Paper plugins built on top of DreamAPI.
 * </p>
 *
 * <p>
 * Automatically initializes the {@link DreamAPI} core if not already initialized
 * and provides lifecycle hooks for plugin developers:
 * {@link #onDreamEnable()} and {@link #onDreamDisable()}.
 * </p>
 *
 * @author Dreamin
 * @since 1.0.0
 */
public abstract class DreamPlugin extends JavaPlugin {

  @Getter
  public static DreamPlugin instance;

  @Getter
  public static ServiceAnnotationProcessor serviceManager;

  @Getter
  public static Set<Class<?>> preScannedClasses;

  @Getter
  private PaperCommandManager<CommandSender> manager;
  private AnnotationParser<CommandSender> annotationParser;

  /**
   * Reference to the active {@link DreamAPI.IApiProvider} instance.
   */
  @Getter
  protected DreamAPI.IApiProvider dreamAPI;

  @Getter
  protected @Nullable Object serviceInspector;

  @Getter @Setter
  protected @NotNull BroadcastContext broadcastContext;

  @Getter @Setter
  protected boolean
    broadcastCmd = false,
    glowingCmd = false,
    nmsVisualCmd = false,
    itemRegistryCmd = false,
    debugCmd = false,
    serviceCmd = false,
    langCmd = false,
    guiCmd = false,
    worldBorderCmd = false;

  // ##############################################################
  // -------------------- JAVAPLUGIN METHODS ----------------------
  // ##############################################################

  @Override
  public void onEnable() {
    if (!DreamAPI.isInitialized()) {
      getLogger().info("DreamAPI provider not found. Initializing core implementation...");
      new ApiProviderImpl(this);
    }

  final var instance = this;

    if (isClassPresent("fr.dreamin.dreamapi.api.item.ItemKeys")) {
      try {
        final var itemKeysClass = Class.forName("fr.dreamin.dreamapi.api.item.ItemKeys", false, getClass().getClassLoader());
        itemKeysClass.getMethod("init", Plugin.class).invoke(null, this);
      } catch (Throwable t) {
        getLogger().warning("[DreamAPI] Could not initialize ItemKeys: " + t.getMessage());
      }
    }

    try {
    final var preScannedClasses = ClassScanner.getClasses(this, this.getClass().getPackageName(), true, getClassScanParentPackageLevels());
    } catch (IOException | ClassNotFoundException e) {
      getLogger().severe(String.format("[DreamAPI] Failed to scan classes: %s", e.getMessage()));
      throw new RuntimeException(e);
    }

    this.broadcastContext = BroadcastContext.builder().build();

    this.dreamAPI = DreamAPI.getAPI();

  final var serviceManager = new ServiceAnnotationProcessor(this, preScannedClasses);
    loadServices();
    serviceManager.process();

    if (isClassPresent("fr.dreamin.dreamapi.core.service.ui.DreamServiceInspector")
      && isClassPresent("xyz.xenondevs.invui.gui.Gui")) {
      try {
        final var cls = Class.forName("fr.dreamin.dreamapi.core.service.ui.DreamServiceInspector", false, getClass().getClassLoader());
        this.serviceInspector = cls.getDeclaredConstructor(Plugin.class, ServiceAnnotationProcessor.class)
          .newInstance(this, serviceManager);
      } catch (Throwable t) {
        getLogger().warning("[DreamAPI] Could not initialize DreamServiceInspector: " + t.getMessage());
      }
    }

    runOptionalAnnotationProcessors();

    initCmds();
    runOptionalCmdAnnotationProcessor();

    new ListenerAnnotationProcessor(this, serviceManager, preScannedClasses)
      .process();

    onDreamEnable();
    loadCommands();

    getLogger().info(
      String.format("DreamAPI initialized successfully with provider: %s",
        this.dreamAPI.getClass().getSimpleName())
    );
  }

  protected int getClassScanParentPackageLevels() {
    return 0;
  }

  @Override
  public void onDisable() {
    onDreamDisable();
  }

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  public abstract void onDreamEnable();

  public abstract void onDreamDisable();

  // ###############################################################
  // ---------------------- INTERNAL METHODS -----------------------
  // ###############################################################

  public static <T> T getService(Class<T> serviceClass) {
    final var sm = Bukkit.getServicesManager();
    final var service = sm.load(serviceClass);
    if (service == null)
      throw new IllegalStateException("Service " + serviceClass.getName() + " is not loaded");
    return service;
  }

  public static void callEvent(final @NotNull Event event) {
    Bukkit.getPluginManager().callEvent(event);
  }

  public static void registerEvent(final @NotNull Listener listener) {
    Bukkit.getPluginManager().registerEvents(listener, DreamAPI.getAPI().plugin());
  }


  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public @NotNull <T extends DreamService> Optional<T> findDreamService(final @NotNull Class<T> clazz) {
    return Optional.ofNullable(serviceManager.getDreamService(clazz));
  }

  public <T extends DreamService> @NotNull T getDreamService(@NotNull Class<T> clazz) {
    final var service = serviceManager.getDreamService(clazz);
    if (service == null)
      throw new IllegalStateException(String.format(
        "DreamService '%s' is not loaded or not registered.",
        clazz.getSimpleName()
      ));
    return service;
  }

  public void registerCommand(final @NotNull Object commandHandler) {
    if (this.annotationParser != null)
      this.annotationParser.parse(commandHandler);
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void runOptionalCmdAnnotationProcessor() {
    if (this.annotationParser != null && isClassPresent("fr.dreamin.dreamapi.core.cmd.scanner.CmdAnnotationProcessor")) {
      try {
        final var procClass = Class.forName("fr.dreamin.dreamapi.core.cmd.scanner.CmdAnnotationProcessor", false, getClass().getClassLoader());
        final var ctor = procClass.getDeclaredConstructor(
          Plugin.class,
          AnnotationParser.class,
          ServiceAnnotationProcessor.class,
          Set.class
        );
        final var proc = ctor.newInstance(this, this.annotationParser, serviceManager, preScannedClasses);
        procClass.getMethod("process").invoke(proc);
      } catch (Throwable e) {
        getLogger().log(Level.SEVERE, "[DreamAPI] Failed to run CmdAnnotationProcessor", e);
      }
    }
  }

  private void runOptionalAnnotationProcessors() {
    if (isServiceLoadedByName("fr.dreamin.dreamapi.core.item.service.ItemRegistryServiceImpl")
      && isClassPresent("fr.dreamin.dreamapi.core.item.scanner.ItemAnnotationProcessor")) {
      try {
        final var serviceClass = Class.forName("fr.dreamin.dreamapi.api.item.ItemRegistryService", false, getClass().getClassLoader());
        final var procClass = Class.forName("fr.dreamin.dreamapi.core.item.scanner.ItemAnnotationProcessor", false, getClass().getClassLoader());
        final var ctor = procClass.getDeclaredConstructor(
          Plugin.class,
          serviceClass,
          ServiceAnnotationProcessor.class,
          Set.class
        );
        final var proc = ctor.newInstance(this, Bukkit.getServicesManager().load(serviceClass), serviceManager, preScannedClasses);
        procClass.getMethod("process").invoke(proc);
      } catch (Throwable e) {
        getLogger().log(Level.SEVERE, "[DreamAPI] Failed to run ItemAnnotationProcessor", e);
      }
    }

    if (isServiceLoadedByName("fr.dreamin.dreamapi.core.recipe.service.RecipeCategoryRegistryServiceImpl")
      && isClassPresent("fr.dreamin.dreamapi.core.recipe.scanner.RecipeAnnotationProcessor")) {
      try {
        final var serviceClass = Class.forName("fr.dreamin.dreamapi.api.recipe.service.RecipeRegistryService", false, getClass().getClassLoader());
        final var procClass = Class.forName("fr.dreamin.dreamapi.core.recipe.scanner.RecipeAnnotationProcessor", false, getClass().getClassLoader());
        final var ctor = procClass.getDeclaredConstructor(
          Plugin.class,
          serviceClass,
          ServiceAnnotationProcessor.class,
          Set.class
        );
        final var proc = ctor.newInstance(this, Bukkit.getServicesManager().load(serviceClass), serviceManager, preScannedClasses);
        procClass.getMethod("process").invoke(proc);
      } catch (Throwable e) {
        getLogger().log(Level.SEVERE, "[DreamAPI] Failed to run RecipeAnnotationProcessor", e);
      }
    }
  }

  private void loadServices() {
    final var annotation = getClass().getAnnotation(EnableServices.class);
    final var services = new LinkedHashSet<Class<? extends DreamService>>();

    if (annotation != null) {
      for (final var m : annotation.mode()) {
        for (final var className : LOAD_MODE_SERVICE_NAMES.getOrDefault(m, Set.of())) {
          resolveServiceClass(className).ifPresent(services::add);
        }
      }

      services.addAll(Set.of(annotation.include()));
      services.removeAll(Set.of(annotation.exclude()));
    } else {
      for (final var className : LOAD_MODE_SERVICE_NAMES.get(LoadMode.ALL)) {
        resolveServiceClass(className).ifPresent(services::add);
      }
    }

    for (final var service : services)
      serviceManager.loadServiceFromClass(service);

    if (isLuckPermsAvailable()) {
      resolveServiceClass("fr.dreamin.dreamapi.core.luckperms.LuckPermsServiceImpl")
        .ifPresent(serviceManager::loadServiceFromClass);
    }
  }

  @SuppressWarnings("unchecked")
  private Optional<Class<? extends DreamService>> resolveServiceClass(final @NotNull String className) {
    try {
      final var clazz = Class.forName(className, false, getClass().getClassLoader());
      if (DreamService.class.isAssignableFrom(clazz)) {
        return Optional.of((Class<? extends DreamService>) clazz);
      }
    } catch (ClassNotFoundException | LinkageError ignored) {
      // Sub-library not included in plugin dependencies or secondary dependencies missing
    }
    return Optional.empty();
  }

  private boolean isClassPresent(final @NotNull String className) {
    try {
      Class.forName(className, false, getClass().getClassLoader());
      return true;
    } catch (Throwable ignored) {
      return false;
    }
  }

  private boolean isServiceLoadedByName(final @NotNull String className) {
    return resolveServiceClass(className)
      .map(serviceManager::isLoaded)
      .orElse(false);
  }

  private void ensureServiceLoadedByName(final @NotNull String className) {
    resolveServiceClass(className).ifPresent(cls -> {
      if (!serviceManager.isLoaded(cls)) {
        serviceManager.loadServiceFromClass(cls);
      }
    });
  }

  private boolean isLuckPermsAvailable() {
    try {
      final var lpClass = Class.forName("net.luckperms.api.LuckPerms", false, getClass().getClassLoader());
      if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null)
        return false;

      return Bukkit.getServicesManager().getRegistration((Class) lpClass) != null;
    } catch (Throwable ignored) {
      return false;
    }
  }

  private void initCmds() {
    if (!isClassPresent("cloud.commandframework.paper.PaperCommandManager")) {
      return;
    }
    try {
      this.manager = new PaperCommandManager<>(
        this,
        CommandExecutionCoordinator.simpleCoordinator(),
        Function.identity(),
        Function.identity()
      );

      if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION))
        manager.registerAsynchronousCompletions();

      this.annotationParser = new AnnotationParser<>(manager, CommandSender.class, p -> SimpleCommandMeta.empty());
    } catch (Throwable e) {
      this.getLogger().log(Level.SEVERE, "Unable to register commands", e);
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }

  private void loadOptionalCommand(final @NotNull String[] cmdClassNames, final @Nullable Runnable beforeLoad) {
    if (this.annotationParser == null) return;
    for (final var cmdClassName : cmdClassNames) {
      try {
        final var cmdClass = Class.forName(cmdClassName, false, getClass().getClassLoader());
        if (beforeLoad != null)
          beforeLoad.run();
        final var cmdInstance = cmdClass.getDeclaredConstructor().newInstance();
        this.annotationParser.parse(cmdInstance);
        return;
      } catch (ClassNotFoundException | LinkageError ignored) {
        // Optional module not present or class moved
      } catch (Exception e) {
        getLogger().log(Level.WARNING, "[DreamAPI] Failed to register optional command " + cmdClassName, e);
        return;
      }
    }
  }

  private void loadOptionalCommand(final @NotNull String cmdClassName, final @Nullable Runnable beforeLoad) {
    loadOptionalCommand(new String[]{cmdClassName}, beforeLoad);
  }

  private void loadCommands() {
    if (this.annotationParser == null) return;

    this.annotationParser.parse(new GlobalSuggestCmd());

    if (this.broadcastCmd)
      this.annotationParser.parse(new BroadcastCmd(this));

    if (this.itemRegistryCmd
      && isClassPresent("fr.dreamin.dreamapi.core.item.service.ItemRegistryServiceImpl")
      && isClassPresent("fr.dreamin.dreamapi.core.item.ui.ItemRegistryGUI")) {
      loadOptionalCommand(new String[]{
        "fr.dreamin.dreamapi.core.item.cmd.ItemRegistryCmd"
      }, () ->
        ensureServiceLoadedByName("fr.dreamin.dreamapi.core.item.service.ItemRegistryServiceImpl")
      );
    }

    if (this.glowingCmd && isClassPresent("fr.dreamin.dreamapi.core.glowing.service.GlowingServiceImpl")) {
      loadOptionalCommand(new String[]{
        "fr.dreamin.dreamapi.core.glowing.cmd.GlowingCmd"
      }, () ->
        ensureServiceLoadedByName("fr.dreamin.dreamapi.core.glowing.service.GlowingServiceImpl")
      );
    }

    if (this.nmsVisualCmd && isClassPresent("fr.dreamin.dreamapi.core.nms.visual.service.VisualServiceImpl")) {
      loadOptionalCommand(new String[]{
        "fr.dreamin.dreamapi.core.nms.cmd.VisualCmd"
      }, () ->
        ensureServiceLoadedByName("fr.dreamin.dreamapi.core.nms.visual.service.VisualServiceImpl")
      );
    }

    if (this.guiCmd && isClassPresent("fr.dreamin.dreamapi.core.gui.service.GuiServiceImpl")) {
      loadOptionalCommand(new String[]{
        "fr.dreamin.dreamapi.core.gui.cmd.GUICmd"
      }, () ->
        ensureServiceLoadedByName("fr.dreamin.dreamapi.core.gui.service.GuiServiceImpl")
      );
    }

    if (this.debugCmd) {
      if (!serviceManager.isLoaded(PlayerDebugServiceImpl.class))
        serviceManager.loadServiceFromClass(PlayerDebugServiceImpl.class);

      if (!serviceManager.isLoaded(DebugServiceImpl.class))
        serviceManager.loadServiceFromClass(DebugServiceImpl.class);

      this.annotationParser.parse(new DebugCmd());
    }

    if (this.langCmd && isClassPresent("fr.dreamin.dreamapi.core.lang.service.LangServiceImpl")) {
      loadOptionalCommand(new String[]{
        "fr.dreamin.dreamapi.core.lang.cmd.LangCmd"
      }, () ->
        ensureServiceLoadedByName("fr.dreamin.dreamapi.core.lang.service.LangServiceImpl")
      );
    }

    if (this.worldBorderCmd && isClassPresent("fr.dreamin.dreamapi.core.worldborder.service.WorldBorderServiceImpl")) {
      loadOptionalCommand(new String[]{
        "fr.dreamin.dreamapi.core.worldborder.cmd.WorldBorderCmd"
      }, () -> {
        ensureServiceLoadedByName("fr.dreamin.dreamapi.core.world.impl.WorldServiceImpl");
        ensureServiceLoadedByName("fr.dreamin.dreamapi.core.worldborder.service.WorldBorderServiceImpl");
      });
    }

    if (this.serviceCmd)
      this.annotationParser.parse(new ServiceCmd(this, serviceManager, this.serviceInspector));

    if (isServiceLoadedByName("fr.dreamin.dreamapi.core.navigate.service.NavigateServiceImpl")) {
      loadOptionalCommand(new String[]{
        "fr.dreamin.dreamapi.core.navigate.cmd.NavigateCmd"
      }, null);
    }
  }

  // ###############################################################
  // ----------------------- STATIC METHODS ------------------------
  // ###############################################################

  private static final Map<LoadMode, Set<String>> LOAD_MODE_SERVICE_NAMES = Map.of(
    LoadMode.ALL, Set.of(
      "fr.dreamin.dreamapi.core.logger.PlayerDebugServiceImpl",
      "fr.dreamin.dreamapi.core.logger.DebugServiceImpl",
      "fr.dreamin.dreamapi.core.recipe.service.RecipeRegistryServiceImpl",
      "fr.dreamin.dreamapi.core.recipe.service.RecipeCategoryRegistryServiceImpl",
      "fr.dreamin.dreamapi.core.item.service.ItemRegistryServiceImpl",
      "fr.dreamin.dreamapi.core.animation.service.AnimationServiceImpl",
      "fr.dreamin.dreamapi.core.world.impl.WorldServiceImpl",
      "fr.dreamin.dreamapi.core.time.day.impl.DayCycleServiceImpl",
      "fr.dreamin.dreamapi.core.team.TeamServiceImpl",
      "fr.dreamin.dreamapi.core.nms.visual.service.VisualServiceImpl",
      "fr.dreamin.dreamapi.core.glowing.service.GlowingServiceImpl",
      "fr.dreamin.dreamapi.core.cuboid.service.CuboidServiceImpl",
      "fr.dreamin.dreamapi.core.hologram.service.HologramServiceImpl",
      "fr.dreamin.dreamapi.core.lang.service.LangServiceImpl",
      "fr.dreamin.dreamapi.core.worldborder.service.WorldBorderServiceImpl",
      "fr.dreamin.dreamapi.core.gui.service.GuiServiceImpl",
      "fr.dreamin.dreamapi.core.navigate.service.NavigateServiceImpl"
    ),

    LoadMode.MINIMAL, Set.of(
      "fr.dreamin.dreamapi.core.logger.PlayerDebugServiceImpl",
      "fr.dreamin.dreamapi.core.logger.DebugServiceImpl"
    ),

    LoadMode.DATA, Set.of(
      "fr.dreamin.dreamapi.core.item.service.ItemRegistryServiceImpl",
      "fr.dreamin.dreamapi.core.recipe.service.RecipeRegistryServiceImpl",
      "fr.dreamin.dreamapi.core.recipe.service.RecipeCategoryRegistryServiceImpl",
      "fr.dreamin.dreamapi.core.hologram.service.HologramServiceImpl"
    ),

    LoadMode.GAMEPLAY, Set.of(
      "fr.dreamin.dreamapi.core.world.impl.WorldServiceImpl",
      "fr.dreamin.dreamapi.core.time.day.impl.DayCycleServiceImpl",
      "fr.dreamin.dreamapi.core.team.TeamServiceImpl"
    ),

    LoadMode.VISUAL, Set.of(
      "fr.dreamin.dreamapi.core.nms.visual.service.VisualServiceImpl",
      "fr.dreamin.dreamapi.core.glowing.service.GlowingServiceImpl",
      "fr.dreamin.dreamapi.core.cuboid.service.CuboidServiceImpl",
      "fr.dreamin.dreamapi.core.animation.service.AnimationServiceImpl"
    ),

    LoadMode.DEBUG, Set.of(
      "fr.dreamin.dreamapi.core.logger.PlayerDebugServiceImpl",
      "fr.dreamin.dreamapi.core.logger.DebugServiceImpl"
    ),

    LoadMode.NONE, Set.of()
  );

}
