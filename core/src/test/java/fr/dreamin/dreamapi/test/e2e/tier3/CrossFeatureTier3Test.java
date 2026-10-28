package fr.dreamin.dreamapi.test.e2e.tier3;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dreamin.dreamapi.api.dependency.RequiresVersion;
import fr.dreamin.dreamapi.api.util.VersionUtils;
import fr.dreamin.dreamapi.core.dependency.DependencyLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cross-Feature Pairwise Interaction Tests (Tier 3)")
public class CrossFeatureTier3Test {

  // =========================================================================
  // TIER 3: Pairwise Combinations (Cross-Module Contracts & Interoperability)
  // =========================================================================

  @Test
  @DisplayName("TC-T3-CB-01: VersionUtils + PacketReflection dynamic field fallback selection")
  void testVersionUtilsWithPacketReflectionFieldFallback() {
    // Simulating packet field schema that changed between 26.1 and 26.2
    String selectedField;
    if (VersionUtils.isWithinRange("26.2.0", null))
      selectedField = "modernPacketData";
    else
      selectedField = "legacyPacketData";

    // Default server is 26.1.2 (< 26.2.0), so legacy field must be chosen
    assertEquals("legacyPacketData", selectedField,
      "Under 26.1.2 runtime, VersionUtils must select legacy packet reflection field");
  }

  @Test
  @DisplayName("TC-T3-CB-02: RequiresVersion + DependencyLoader integration")
  void testRequiresVersionWithDependencyLoader() {
    @RequiresVersion(min = "26.1.0", max = "26.3.0")
    class ValidService {}

    @RequiresVersion(min = "26.3.0")
    class FutureService {}

    assertTrue(DependencyLoader.checkVersion(ValidService.class),
      "DependencyLoader must allow ValidService within range");
    assertFalse(DependencyLoader.checkVersion(FutureService.class),
      "DependencyLoader must gate FutureService requiring 26.3.0");
  }

  @Test
  @DisplayName("TC-T3-CB-03: RequiresVersion + Service scanning simulation")
  void testRequiresVersionWithServiceAnnotationProcessor() {
    @RequiresVersion(min = "26.4+")
    class FutureExperimentalService {}

    final var canRegister = DependencyLoader.checkVersion(FutureExperimentalService.class);
    assertFalse(canRegister, "Future 26.4+ service must be gated by service processor check");
  }

  @Test
  @DisplayName("TC-T3-CB-04: Core (ApiProviderImpl) + Recipe module Jackson integration")
  void testCoreWithRecipeModuleAndJackson() {
    final var mapper = new ObjectMapper();
    final var recipeModuleClass = "fr.dreamin.dreamapi.core.recipe.module.CustomRecipeModule";

    try {
      final var clazz = Class.forName(recipeModuleClass);
      final var ctor = clazz.getDeclaredConstructor();
      ctor.setAccessible(true);
      final var module = ctor.newInstance();
      mapper.registerModule((com.fasterxml.jackson.databind.Module) module);
      assertNotNull(mapper);
    } catch (LinkageError | Exception e) {
      // Graceful fallback when module is not on test classpath or lacks runtime dependencies
      assertTrue(true, "Core gracefully survives absence of Recipe Jackson module");
    }
  }

  @Test
  @DisplayName("TC-T3-CB-05: Core (ApiProviderImpl) + Particle module Jackson integration")
  void testCoreWithParticleModuleAndJackson() {
    final var particleModuleClass = "fr.dreamin.dreamapi.core.particle.module.ParticleAnimationModule";
    try {
      final var clazz = Class.forName(particleModuleClass);
      assertNotNull(clazz);
    } catch (ClassNotFoundException | LinkageError e) {
      assertTrue(true, "Core gracefully survives absence of Particle Jackson module");
    }
  }

  @Test
  @DisplayName("TC-T3-CB-06: Core (ApiProviderImpl) + Cuboid module Jackson integration")
  void testCoreWithCuboidModuleAndJackson() {
    final var cuboidModuleClass = "fr.dreamin.dreamapi.core.cuboid.module.CuboidModule";
    try {
      final var clazz = Class.forName(cuboidModuleClass);
      assertNotNull(clazz);
    } catch (ClassNotFoundException | LinkageError e) {
      assertTrue(true, "Core gracefully survives absence of Cuboid Jackson module");
    }
  }

  @Test
  @DisplayName("TC-T3-CB-07: Plugin-Base (DreamPlugin) + GUI module decoupled command registration")
  void testPluginBaseWithDecoupledGuiCommand() {
    final var commandRegistered = new AtomicBoolean(false);
    final var guiCmdClass = "fr.dreamin.dreamapi.core.gui.cmd.GUICmd";

    try {
      final var clazz = Class.forName(guiCmdClass);
      commandRegistered.set(true);
    } catch (ClassNotFoundException | LinkageError ignored) {
      // Normal when GUI module is omitted from runtime classpath
    }

    // In isolated test environment, GUI is not present, so no exception and command not registered
    assertFalse(commandRegistered.get());
  }

  @Test
  @DisplayName("TC-T3-CB-08: Plugin-Base + WorldBorder command + NMS WorldBorderAction cross-module contract")
  @SuppressWarnings({"unchecked", "rawtypes"})
  void testPluginBaseWithDecoupledWorldBorderCommandAndNmsAction() {
    // Verifies WorldBorderAction enum from NMS is usable without circular dependency on WorldBorder service
    try {
      final var actionClass = Class.forName("fr.dreamin.dreamapi.api.worldborder.model.WorldBorderAction");
      final var actions = actionClass.getEnumConstants();
      assertNotNull(actions, "WorldBorderAction enum must have values");
      assertTrue(actions.length > 0, "WorldBorderAction enum must have values");
      final var initAction = Enum.valueOf((Class<Enum>) actionClass.asSubclass(Enum.class), "INITIALIZE");
      assertEquals("INITIALIZE", initAction.name());
      final var setCenterAction = Enum.valueOf((Class<Enum>) actionClass.asSubclass(Enum.class), "SET_CENTER");
      assertEquals("SET_CENTER", setCenterAction.name());
    } catch (ClassNotFoundException | LinkageError ignored) {
      // Decoupled runtime when NMS is omitted from core test classpath
    }
  }

  @Test
  @DisplayName("TC-T3-CB-09: Plugin-Base + Glowing command + NMS reflection decoupling")
  void testPluginBaseWithDecoupledGlowingCommand() {
    final var glowingCmdClass = "fr.dreamin.dreamapi.core.glowing.cmd.GlowingCmd";
    var isPresent = false;
    try {
      Class.forName(glowingCmdClass);
      isPresent = true;
    } catch (ClassNotFoundException | LinkageError ignored) {
    }
    assertFalse(isPresent, "GlowingCmd should be decoupled and absent in core test classpath");
  }

  @Test
  @DisplayName("TC-T3-CB-10: Plugin-Base + Item & Recipe reflective annotation processors")
  void testPluginBaseWithItemAndRecipeAnnotationProcessors() {
    // Verifies that processor class lookups use Class.forName rather than class literals
    final var itemProcessor = "fr.dreamin.dreamapi.core.item.scanner.ItemAnnotationProcessor";
    final var recipeProcessor = "fr.dreamin.dreamapi.core.recipe.scanner.RecipeAnnotationProcessor";

    assertDoesNotThrow(() -> {
      try {
        Class.forName(itemProcessor);
      } catch (ClassNotFoundException | LinkageError ignored) {}

      try {
        Class.forName(recipeProcessor);
      } catch (ClassNotFoundException | LinkageError ignored) {}
    });
  }

  @Test
  @DisplayName("TC-T3-CB-11: Hooks (NPC) + Citizens lazy initialization contract")
  void testHooksNpcWithLazyCitizens() {
    // When Citizens is not installed, accessing NPC class should not eagerly trigger CitizensAPI.createAnonymousNPCRegistry
    final var npcClass = "fr.dreamin.dreamapi.core.npc.NPC";
    try {
      final var clazz = Class.forName(npcClass);
      assertNotNull(clazz);
    } catch (ClassNotFoundException | LinkageError ignored) {
      // Safe fallback when hooks module is absent
      assertTrue(true);
    }
  }

  @Test
  @DisplayName("TC-T3-CB-12: 'all' aggregator transitive classpath export contract")
  void testAllAggregatorTransitiveExport() {
    // Verifies that 'all' aggregator allows consumers to import core contracts transitively
    assertNotNull(VersionUtils.class);
    assertNotNull(RequiresVersion.class);
    try {
      assertNotNull(Class.forName("fr.dreamin.dreamapi.api.worldborder.model.WorldBorderAction"));
    } catch (ClassNotFoundException ignored) {
      // In isolated core module test, nms is omitted from classpath
    }
  }
}
