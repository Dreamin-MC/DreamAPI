package fr.dreamin.dreamapi.plugin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DreamPlugin Static State Management Tests (Feature F4)")
class DreamPluginStaticStateTest {

  @Test
  @DisplayName("Static fields exist and can be safely managed")
  void testStaticFieldsIntegrity() {
    assertNull(DreamPlugin.getInstance());
    assertNull(DreamPlugin.getServiceManager());
    assertNull(DreamPlugin.getPreScannedClasses());

    DreamPlugin.preScannedClasses = new HashSet<>(Set.of(String.class, Integer.class));
    assertNotNull(DreamPlugin.getPreScannedClasses());
    assertEquals(2, DreamPlugin.getPreScannedClasses().size());

    DreamPlugin.preScannedClasses.clear();
    DreamPlugin.preScannedClasses = null;
    assertNull(DreamPlugin.getPreScannedClasses());
  }

  @Test
  @DisplayName("Static getters expose the assigned static references")
  void testStaticGetters() {
    final var classes = new HashSet<Class<?>>(Set.of(Double.class));
    DreamPlugin.preScannedClasses = classes;

    assertEquals(classes, DreamPlugin.getPreScannedClasses());
    DreamPlugin.preScannedClasses = null;
  }
}
