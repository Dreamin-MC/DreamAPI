package fr.dreamin.dreamapi.core.npc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NPC Citizens Decoupling Adversarial Test in hooks subproject")
public class NpcCitizensDecouplingTest {

  @Test
  @DisplayName("Check if Citizens is present on test runtime classpath")
  void checkCitizensOnClasspath() {
    var citizensPresent = false;
    try {
      Class.forName("net.citizensnpcs.api.CitizensAPI");
      citizensPresent = true;
    } catch (ClassNotFoundException | LinkageError ignored) {
    }
    System.out.println("Citizens present on hooks test classpath? " + citizensPresent);
    assertFalse(citizensPresent, "Citizens must NOT be present on hooks test runtime classpath");
  }

  @Test
  @DisplayName("Check if NPC.class can be loaded and initialized when Citizens is absent")
  void checkNpcClassLoading() {
    assertDoesNotThrow(() -> {
      final var npcClass = Class.forName("fr.dreamin.dreamapi.core.npc.NPC", true, getClass().getClassLoader());
      assertNotNull(npcClass);
    }, "NPC.class must initialize without NoClassDefFoundError when Citizens is absent");
  }

  @Test
  @DisplayName("Check if NPC.NavigationType is usable")
  void checkNavigationType() {
    assertDoesNotThrow(() -> {
      final var types = NPC.NavigationType.values();
      assertEquals(2, types.length);
      assertEquals(NPC.NavigationType.DEFAULT, NPC.NavigationType.valueOf("DEFAULT"));
      assertEquals(NPC.NavigationType.STRAIGHT_LINEAR, NPC.NavigationType.valueOf("STRAIGHT_LINEAR"));
    });
  }

  @Test
  @DisplayName("Adversarial: ClassLoader strictly blocking net.citizensnpcs.* allows NPC.class definition and initialization")
  void checkAdversarialBlockingClassLoader() throws Exception {
    final var parentLoader = getClass().getClassLoader();
    final var interceptedCitizens = new AtomicBoolean(false);

    ClassLoader blockingLoader = new ClassLoader(parentLoader) {
      @Override
      public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.startsWith("net.citizensnpcs.")) {
          interceptedCitizens.set(true);
          throw new ClassNotFoundException("Adversarial block: Citizens class blocked: " + name);
        }
        if ("fr.dreamin.dreamapi.core.npc.NPC".equals(name)) {
          final var loaded = findLoadedClass(name);
          if (loaded != null) return loaded;
          try (InputStream is = parentLoader.getResourceAsStream("fr/dreamin/dreamapi/core/npc/NPC.class")) {
            if (is == null) throw new ClassNotFoundException("Cannot find bytecode for NPC.class");
            final var bytes = is.readAllBytes();
            final var defined = defineClass(name, bytes, 0, bytes.length);
            if (resolve)
              resolveClass(defined);
            return defined;
          } catch (Exception e) {
            throw new ClassNotFoundException("Failed to define NPC.class", e);
          }
        }
        return super.loadClass(name, resolve);
      }
    };

    assertDoesNotThrow(() -> {
      final var npcClass = Class.forName("fr.dreamin.dreamapi.core.npc.NPC", true, blockingLoader);
      assertNotNull(npcClass);
      assertSame(blockingLoader, npcClass.getClassLoader());
    }, "NPC.class should define and initialize cleanly under a classloader that strictly blocks Citizens");

    assertFalse(interceptedCitizens.get(), "JVM bytecode verifier must not attempt to load any net.citizensnpcs.* classes when linking NPC.class");
  }

  @Test
  @DisplayName("Adversarial: CitizensRegistryHolder inner class is NOT loaded eagerly during NPC.class initialization")
  void checkHolderClassNotLoadedEagerly() throws Exception {
    class TrackingClassLoader extends ClassLoader {
      TrackingClassLoader(ClassLoader parent) {
        super(parent);
      }
      public boolean isLoaded(String name) {
        return findLoadedClass(name) != null;
      }
    }

    final var trackingLoader = new TrackingClassLoader(getClass().getClassLoader());
    final var npcClass = Class.forName("fr.dreamin.dreamapi.core.npc.NPC", true, trackingLoader);
    assertNotNull(npcClass);

    assertFalse(trackingLoader.isLoaded("fr.dreamin.dreamapi.core.npc.NPC$CitizensRegistryHolder"),
      "NPC$CitizensRegistryHolder must remain unloaded when NPC.class is loaded and initialized");
  }

  @Test
  @DisplayName("Adversarial: Reflection boundary - member introspection throws NoClassDefFoundError while classloading succeeds")
  void checkReflectionInspectionResilience() throws Exception {
    final var npcClass = Class.forName("fr.dreamin.dreamapi.core.npc.NPC", true, getClass().getClassLoader());
    assertNotNull(npcClass);

    // In JVM native reflection, getDeclaredFields0 eagerly resolves field types.
    // Since net.citizensnpcs.api.npc.NPCRegistry is absent, this throws NoClassDefFoundError.
    assertThrows(NoClassDefFoundError.class, npcClass::getDeclaredFields,
      "Field reflection on NPC.class triggers resolution of net.citizensnpcs.api.npc.NPCRegistry and throws NoClassDefFoundError");

    // Similarly, getDeclaredConstructors0 attempts to resolve parameters (including net.citizensnpcs.api.npc.NPC).
    assertThrows(NoClassDefFoundError.class, npcClass::getDeclaredConstructors,
      "Constructor reflection on NPC.class triggers resolution of net.citizensnpcs.api.npc.NPC and throws NoClassDefFoundError");

    // Similarly, getDeclaredMethods0 attempts to resolve return/param types (e.g. build() -> net.citizensnpcs.api.npc.NPC).
    assertThrows(NoClassDefFoundError.class, npcClass::getDeclaredMethods,
      "Method reflection on NPC.class triggers resolution of net.citizensnpcs.api.npc.NPC and throws NoClassDefFoundError");
  }

  @Test
  @DisplayName("Adversarial: Bytecode inspection verifies MemoryNPCDataStore and NPCDataStore are absent from NPC.class")
  void checkBytecodeAbsenceOfDataStoreInNpcClass() throws Exception {
    try (InputStream npcIs = getClass().getClassLoader().getResourceAsStream("fr/dreamin/dreamapi/core/npc/NPC.class")) {
      assertNotNull(npcIs, "NPC.class bytecode resource must exist");
      final var npcBytes = npcIs.readAllBytes();
      final var npcBytecodeString = new String(npcBytes, StandardCharsets.ISO_8859_1);

      assertFalse(npcBytecodeString.contains("MemoryNPCDataStore"),
        "NPC.class constant pool and bytecode must NOT contain 'MemoryNPCDataStore'");
      assertFalse(npcBytecodeString.contains("net/citizensnpcs/api/npc/NPCDataStore"),
        "NPC.class constant pool and bytecode must NOT contain 'net/citizensnpcs/api/npc/NPCDataStore'");
      assertFalse(npcBytecodeString.contains("createAnonymousNPCRegistry"),
        "NPC.class constant pool and bytecode must NOT contain 'createAnonymousNPCRegistry'");
    }

    try (InputStream holderIs = getClass().getClassLoader().getResourceAsStream("fr/dreamin/dreamapi/core/npc/NPC$CitizensRegistryHolder.class")) {
      assertNotNull(holderIs, "NPC$CitizensRegistryHolder.class bytecode resource must exist");
      final var holderBytes = holderIs.readAllBytes();
      final var holderBytecodeString = new String(holderBytes, StandardCharsets.ISO_8859_1);

      assertTrue(holderBytecodeString.contains("MemoryNPCDataStore"),
        "NPC$CitizensRegistryHolder.class must encapsulate 'MemoryNPCDataStore'");
      assertTrue(holderBytecodeString.contains("createAnonymousNPCRegistry"),
        "NPC$CitizensRegistryHolder.class must encapsulate 'createAnonymousNPCRegistry'");
    }
  }
}

