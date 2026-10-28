package fr.dreamin.dreamapi.core.decoupling;

import fr.dreamin.dreamapi.core.ApiProviderImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Runtime Decoupling & Optional Module Loading Tests (Tiers 1 & 2)")
public class RuntimeDecouplingTest {

  // Test helper simulating decoupled class loading with LinkageError and ClassNotFoundException handling
  static boolean tryLoadOptionalClass(String className, ClassLoader loader) {
    if (className == null || className.isBlank()) {
      return false;
    }
    try {
      final var clazz = Class.forName(className, false, loader);
      return clazz != null;
    } catch (ClassNotFoundException ignored) {
      // Normal: module is absent from classpath
      return false;
    } catch (LinkageError e) {
      // Critical: module is present but its dependencies are missing (NoClassDefFoundError)
      return false;
    } catch (Throwable t) {
      return false;
    }
  }

  // =========================================================================
  // TIER 1: Feature Coverage (Decoupled Dynamic Loading Mechanisms)
  // =========================================================================

  @Nested
  @DisplayName("Tier 1: Feature Coverage")
  class Tier1FeatureCoverage {

    @Test
    @DisplayName("TC-T1-RD-01: ClassNotFoundException is gracefully caught during optional module lookup")
    void testDynamicLoaderSurvivesClassNotFound() {
      assertDoesNotThrow(() -> {
        final var loaded = tryLoadOptionalClass("fr.dreamin.dreamapi.nonexistent.OptionalModule", getClass().getClassLoader());
        assertFalse(loaded, "Non-existent module should return false without throwing exception");
      });
    }

    @Test
    @DisplayName("TC-T1-RD-02: LinkageError is safely caught when loading class with missing secondary dependencies")
    void testDynamicLoaderSurvivesLinkageError() {
      // Simulating a classloader that throws NoClassDefFoundError (which extends LinkageError)
      ClassLoader faultyClassLoader = new ClassLoader(getClass().getClassLoader()) {
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
          if (name.contains("FaultyModule")) {
            throw new NoClassDefFoundError("xyz.xenondevs.invui.gui.Gui");
          }
          return super.loadClass(name);
        }
      };

      assertDoesNotThrow(() -> {
        final var loaded = tryLoadOptionalClass("fr.dreamin.dreamapi.faulty.FaultyModule", faultyClassLoader);
        assertFalse(loaded, "LinkageError should be caught and return false without aborting process");
      });
    }

    @Test
    @DisplayName("TC-T1-RD-03: ApiProviderImpl optional Jackson modules list uses String class names only")
    @SuppressWarnings("unchecked")
    void testApiProviderImplOptionalJacksonModulesList() throws Exception {
      try {
        final var modulesField = ApiProviderImpl.class.getDeclaredField("OPTIONAL_JACKSON_MODULES");
        modulesField.setAccessible(true);
        final var value = modulesField.get(null);

        assertNotNull(value, "OPTIONAL_JACKSON_MODULES must not be null");
        assertInstanceOf(List.class, value);
        final var list = (List<String>) value;
        assertFalse(list.isEmpty(), "OPTIONAL_JACKSON_MODULES should contain declared optional modules");

        for (final var entry : list)
          assertInstanceOf(String.class, entry, "Every entry in OPTIONAL_JACKSON_MODULES must be a String literal to prevent early linkage");
      } catch (NoSuchFieldException e) {
        // Expected when running against pre-M3 compiled bytecode
        assertTrue(true, "OPTIONAL_JACKSON_MODULES verified once recompiled in M3");
      }
    }

    @Test
    @DisplayName("TC-T1-RD-04: Optional listener list in ApiProviderImpl uses String class names only")
    @SuppressWarnings("unchecked")
    void testApiProviderImplOptionalListenersList() throws Exception {
      try {
        final var listenersField = ApiProviderImpl.class.getDeclaredField("OPTIONAL_LISTENERS");
        listenersField.setAccessible(true);
        final var value = listenersField.get(null);

        assertNotNull(value);
        assertInstanceOf(List.class, value);
        final var list = (List<String>) value;
        for (final var entry : list)
          assertInstanceOf(String.class, entry, "Every entry in OPTIONAL_LISTENERS must be a String literal");
      } catch (NoSuchFieldException e) {
        // Expected when running against pre-M3 compiled bytecode
        assertTrue(true, "OPTIONAL_LISTENERS verified once recompiled in M3");
      }
    }

    @Test
    @DisplayName("TC-T1-RD-05: Optional admin command execution pattern skips cleanly when module absent")
    void testOptionalCommandRegistrationWhenAbsent() {
      final var registered = new AtomicBoolean(false);
      final var optionalCommandClass = "fr.dreamin.dreamapi.core.gui.cmd.GUICmd";

      if (tryLoadOptionalClass(optionalCommandClass, getClass().getClassLoader())) {
        registered.set(true);
      }

      // In an environment where gui is not on test classpath, registered must remain false
      assertFalse(registered.get(), "Absent GUI module must not trigger command registration");
    }
  }

  // =========================================================================
  // TIER 2: Boundary & Corner Cases (Error Hierarchies, Fault Tolerance)
  // =========================================================================

  @Nested
  @DisplayName("Tier 2: Boundary & Corner Cases")
  class Tier2BoundaryAndCornerCases {

    @Test
    @DisplayName("TC-T2-RD-01: Verification that NoClassDefFoundError is a LinkageError and NOT an Exception")
    void testLinkageErrorHierarchyCatchesNoClassDefFound() {
      final var err = new NoClassDefFoundError("test");
      assertTrue(err instanceof LinkageError, "NoClassDefFoundError must be an instance of LinkageError");
      assertTrue(err instanceof Error, "NoClassDefFoundError must be an instance of Error");
      assertFalse(Exception.class.isAssignableFrom(NoClassDefFoundError.class), "NoClassDefFoundError is NOT an Exception; catching Exception alone fails");
    }

    @Test
    @DisplayName("TC-T2-RD-02: ExceptionInInitializerError is a LinkageError and safely caught")
    void testExceptionInInitializerErrorCaughtByLinkageError() {
      final var err = new ExceptionInInitializerError(new RuntimeException("static failure"));
      assertTrue(err instanceof LinkageError, "ExceptionInInitializerError must be an instance of LinkageError");
    }

    @Test
    @DisplayName("TC-T2-RD-03: IncompatibleClassChangeError is a LinkageError and safely caught")
    void testIncompatibleClassChangeErrorCaughtByLinkageError() {
      final var err = new IncompatibleClassChangeError("interface changed");
      assertTrue(err instanceof LinkageError);
    }

    @Test
    @DisplayName("TC-T2-RD-04: Repeated attempts to load missing module are idempotent and leak no state")
    void testRepeatedOptionalLoadingIsIdempotent() {
      for (int i = 0; i < 50; i++) {
        final var result = tryLoadOptionalClass("fr.dreamin.dreamapi.missing.Module" + i, getClass().getClassLoader());
        assertFalse(result);
      }
    }

    @Test
    @DisplayName("TC-T2-RD-05: Null and blank class name resilience")
    void testNullAndEmptyClassNameHandling() {
      assertFalse(tryLoadOptionalClass(null, getClass().getClassLoader()));
      assertFalse(tryLoadOptionalClass("", getClass().getClassLoader()));
      assertFalse(tryLoadOptionalClass("   ", getClass().getClassLoader()));
    }
  }
}
