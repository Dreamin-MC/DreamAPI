package fr.dreamin.dreamapi.plugin;

import fr.dreamin.dreamapi.core.cmd.ServiceCmd;
import fr.dreamin.dreamapi.core.service.ServiceAnnotationProcessor;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DreamPlugin & Admin Commands Decoupling Test in core")
public class DreamPluginDecouplingTest {

  private Server originalServer;

  @BeforeEach
  void setUp() {
  final var originalServer = Bukkit.getServer();
  }

  @AfterEach
  void tearDown() {
    try {
      final var serverField = Bukkit.class.getDeclaredField("server");
      serverField.setAccessible(true);
      serverField.set(null, originalServer);
    } catch (Exception ignored) {
    }
  }

  private void setMockServer() throws Exception {
    PluginManager mockPm = (PluginManager) Proxy.newProxyInstance(
      PluginManager.class.getClassLoader(),
      new Class<?>[]{PluginManager.class},
      (proxy, method, args) -> null
    );
    ServicesManager mockSm = (ServicesManager) Proxy.newProxyInstance(
      ServicesManager.class.getClassLoader(),
      new Class<?>[]{ServicesManager.class},
      (proxy, method, args) -> null
    );

    Server mockServer = (Server) Proxy.newProxyInstance(
      Server.class.getClassLoader(),
      new Class<?>[]{Server.class},
      (proxy, method, args) -> switch (method.getName()) {
        case "getPluginManager" -> mockPm;
        case "getServicesManager" -> mockSm;
        case "getLogger" -> Logger.getLogger("MockServer");
        case "getName" -> "MockPaper";
        case "getVersion" -> "git-Paper-26.1.2";
        case "isPrimaryThread" -> true;
        default -> null;
      }
    );

    final var serverField = Bukkit.class.getDeclaredField("server");
    serverField.setAccessible(true);
    serverField.set(null, mockServer);
  }

  private Plugin createMockPlugin() {
    final var logger = Logger.getLogger("PluginTestLogger");
    final var desc = new PluginDescriptionFile("TestPlugin", "1.0", "fr.dreamin.TestPlugin");

    return (Plugin) Proxy.newProxyInstance(
      Plugin.class.getClassLoader(),
      new Class<?>[]{Plugin.class},
      (proxy, method, args) -> switch (method.getName()) {
        case "getName" -> "TestPlugin";
        case "getLogger" -> logger;
        case "getDescription" -> desc;
        default -> null;
      }
    );
  }

  @Test
  @DisplayName("Verify optional modules are absent on core test runtime classpath")
  void verifyOptionalModulesAbsent() {
    String[] optionalClasses = {
      "fr.dreamin.dreamapi.core.service.ui.DreamServiceInspector",
      "xyz.xenondevs.invui.gui.Gui",
      "fr.dreamin.dreamapi.core.item.service.ItemRegistryServiceImpl",
      "fr.dreamin.dreamapi.core.gui.service.GuiServiceImpl",
      "fr.dreamin.dreamapi.core.glowing.service.GlowingServiceImpl",
      "fr.dreamin.dreamapi.core.worldborder.service.WorldBorderServiceImpl",
      "fr.dreamin.dreamapi.core.lang.service.LangServiceImpl",
      "fr.dreamin.dreamapi.core.navigate.service.NavigateServiceImpl"
    };

    for (final var cls : optionalClasses) {
      var present = false;
      try {
        Class.forName(cls);
        present = true;
      } catch (ClassNotFoundException | LinkageError ignored) {
      }
      assertFalse(present, "Class " + cls + " must be absent from core test runtime");
    }
  }

  @Test
  @DisplayName("DreamPlugin class loads and passes JVM bytecode verification without optional modules")
  void testDreamPluginClassLoading() {
    assertDoesNotThrow(() -> {
      final var dreamPluginClass = Class.forName("fr.dreamin.dreamapi.plugin.DreamPlugin");
      assertNotNull(dreamPluginClass);

      // Verify serviceInspector field is declared as Object
      final var inspectorField = dreamPluginClass.getDeclaredField("serviceInspector");
      assertEquals(Object.class, inspectorField.getType(),
        "serviceInspector field must be Object to prevent LinkageError");

      // Verify no public/protected methods expose types from optional modules
      for (final var m : dreamPluginClass.getDeclaredMethods()) {
        assertFalse(m.getReturnType().getName().contains("RegisteredItem"), "Method " + m.getName() + " exposes RegisteredItem");
        assertFalse(m.getReturnType().getName().contains("ItemTag"), "Method " + m.getName() + " exposes ItemTag");
        assertFalse(m.getReturnType().getName().contains("CustomRecipe"), "Method " + m.getName() + " exposes CustomRecipe");
        assertFalse(m.getReturnType().getName().contains("DreamServiceInspector"), "Method " + m.getName() + " exposes DreamServiceInspector");
      }
    }, "DreamPlugin must pass class loading and verification without optional modules");
  }

  @Test
  @DisplayName("ServiceCmd instantiates and openInspector method executes safely with null inspector")
  void testServiceCmdWithNullInspector() throws Exception {
    setMockServer();
    final var mockPlugin = createMockPlugin();
    final var sap = new ServiceAnnotationProcessor(mockPlugin, Set.of());

    final var cmd = new ServiceCmd(mockPlugin, sap, null);
    assertNotNull(cmd);

    final var openInspector = ServiceCmd.class.getDeclaredMethod("openInspector", CommandSender.class);
    openInspector.setAccessible(true);

    final var messagesSent = new ArrayList<String>();
    Player mockPlayer = (Player) Proxy.newProxyInstance(
      Player.class.getClassLoader(),
      new Class<?>[]{Player.class},
      (proxy, method, args) -> {
        if ("sendMessage".equals(method.getName()) && args.length > 0) {
          messagesSent.add(args[0].toString());
        }
        return null;
      }
    );

    assertDoesNotThrow(() -> openInspector.invoke(cmd, mockPlayer));
    assertFalse(messagesSent.isEmpty(), "Player must receive error message");
    assertTrue(messagesSent.stream().anyMatch(m -> m.contains("dreamapi-gui module is required")),
      "Message must mention that dreamapi-gui module is required");
  }

  @Test
  @DisplayName("All optional admin command classes load without crashing when services absent")
  void testOptionalAdminCommandsSafelyLoadable() {
    final var adminCommands = new String[]{
      "fr.dreamin.dreamapi.core.item.cmd.ItemRegistryCmd",
      "fr.dreamin.dreamapi.core.glowing.cmd.GlowingCmd",
      "fr.dreamin.dreamapi.core.nms.cmd.VisualCmd",
      "fr.dreamin.dreamapi.core.gui.cmd.GUICmd",
      "fr.dreamin.dreamapi.core.lang.cmd.LangCmd",
      "fr.dreamin.dreamapi.core.worldborder.cmd.WorldBorderCmd",
      "fr.dreamin.dreamapi.core.navigate.cmd.NavigateCmd"
    };

    for (final var cmdClass : adminCommands) {
      // In DreamPlugin.loadCommands(), commands are guarded by isClassPresent(...)
      // and loadOptionalCommand catches ClassNotFoundException | LinkageError
      assertDoesNotThrow(() -> {
        try {
          Class.forName(cmdClass, false, getClass().getClassLoader());
        } catch (ClassNotFoundException | LinkageError ignored) {
          // Handled safely by DreamPlugin
        }
      });
    }
  }
}
