package fr.dreamin.dreamapi.core.event.scanner;

import fr.dreamin.dreamapi.api.dependency.RequiresVersion;
import fr.dreamin.dreamapi.api.event.annotation.DreamEvent;
import fr.dreamin.dreamapi.api.event.annotation.RegisterListener;
import fr.dreamin.dreamapi.core.service.ServiceAnnotationProcessor;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ListenerAnnotationProcessor Event Registration & Multi-Interface Tests (Feature F5)")
class ListenerAnnotationProcessorTest {

  @DreamEvent
  public static class MultiInterfaceListener implements Listener, AutoCloseable {
    @Override
    public void close() {}
  }

  @RegisterListener
  public static class AliasListener implements Listener {}

  @RegisterListener
  @RequiresVersion(min = "99.0")
  public static class FutureVersionListener implements Listener {}

  private Server originalServer;
  private final ArrayList<Listener> registeredListeners = new ArrayList<>();

  @BeforeEach
  void setUp() throws Exception {
    registeredListeners.clear();
    originalServer = Bukkit.getServer();

    final var mockPm = (PluginManager) Proxy.newProxyInstance(
      PluginManager.class.getClassLoader(),
      new Class<?>[]{PluginManager.class},
      (_, method, args) -> {
        if ("registerEvents".equals(method.getName()) && args != null && args.length >= 1) {
          if (args[0] instanceof Listener l)
            registeredListeners.add(l);
          return null;
        }
        return null;
      }
    );

    final var mockServer = (Server) Proxy.newProxyInstance(
      Server.class.getClassLoader(),
      new Class<?>[]{Server.class},
      (_, method, args) -> {
        if ("getPluginManager".equals(method.getName()))
          return mockPm;
        if ("getLogger".equals(method.getName()))
          return Logger.getLogger("MockServer");
        return null;
      }
    );

    final var serverField = Bukkit.class.getDeclaredField("server");
    serverField.setAccessible(true);
    serverField.set(null, mockServer);
  }

  @AfterEach
  void tearDown() throws Exception {
    final var serverField = Bukkit.class.getDeclaredField("server");
    serverField.setAccessible(true);
    serverField.set(null, originalServer);
  }

  private Plugin createMockPlugin() {
    final var logger = Logger.getLogger("TestPluginLogger");
    return (Plugin) Proxy.newProxyInstance(
      Plugin.class.getClassLoader(),
      new Class<?>[]{Plugin.class},
      (_, method, _) -> {
        if ("getLogger".equals(method.getName()))
          return logger;
        if ("getName".equals(method.getName()))
          return "TestPlugin";
        return null;
      }
    );
  }

  @Test
  @DisplayName("Multi-interface listener registers successfully without throwing IllegalStateException")
  void testMultiInterfaceListenerRegistration() {
    final var plugin = createMockPlugin();
    final var classes = Set.<Class<?>>of(MultiInterfaceListener.class);
    final var serviceManager = new ServiceAnnotationProcessor(plugin, classes);
    final var processor = new ListenerAnnotationProcessor(plugin, serviceManager, classes);

    assertDoesNotThrow(processor::process);
    assertFalse(registeredListeners.isEmpty());
    assertTrue(registeredListeners.stream().anyMatch(l -> l instanceof MultiInterfaceListener));
  }

  @Test
  @DisplayName("RegisterListener annotation alias is detected and registers events")
  void testRegisterListenerAlias() {
    final var plugin = createMockPlugin();
    final var classes = Set.<Class<?>>of(AliasListener.class);
    final var serviceManager = new ServiceAnnotationProcessor(plugin, classes);
    final var processor = new ListenerAnnotationProcessor(plugin, serviceManager, classes);

    processor.process();
    assertTrue(registeredListeners.stream().anyMatch(l -> l instanceof AliasListener));
  }

  @Test
  @DisplayName("Listener with unmet version requirements is skipped by version gating")
  void testVersionGatedListenerSkipped() {
    final var plugin = createMockPlugin();
    final var classes = Set.<Class<?>>of(FutureVersionListener.class);
    final var serviceManager = new ServiceAnnotationProcessor(plugin, classes);
    final var processor = new ListenerAnnotationProcessor(plugin, serviceManager, classes);

    processor.process();
    assertTrue(registeredListeners.stream().noneMatch(l -> l instanceof FutureVersionListener));
  }
}
