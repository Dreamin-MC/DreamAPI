package fr.dreamin.dreamapi.test.runner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Standalone programmatic E2E Test Runner for DreamAPI.
 * Executes all test classes across Tiers 1-4, tracks metrics, and generates execution summaries.
 */
public class DreamApiE2ETestRunner {

  private static final List<String> TEST_CLASS_NAMES = List.of(
    "fr.dreamin.dreamapi.api.util.VersionUtilsTest",
    "fr.dreamin.dreamapi.api.dependency.RequiresVersionTest",
    "fr.dreamin.dreamapi.api.nms.packet.PacketReflectionTest",
    "fr.dreamin.dreamapi.core.decoupling.RuntimeDecouplingTest",
    "fr.dreamin.dreamapi.test.e2e.tier1.ModularArchitectureTier1Test",
    "fr.dreamin.dreamapi.test.e2e.tier3.CrossFeatureTier3Test",
    "fr.dreamin.dreamapi.test.e2e.tier4.RealWorldScenariosTier4Test"
  );

  public static void main(String[] args) {
    System.out.println("===============================================================================");
    System.out.println("                 DreamAPI 4-Tier Opaque-Box E2E Test Runner                    ");
    System.out.println("===============================================================================\n");

    var totalTests = 0;
    var passedTests = 0;
    var failedTests = 0;
    final var failureDetails = new ArrayList<String>();

    for (final var className : TEST_CLASS_NAMES) {
      Class<?> clazz;
      try {
        clazz = Class.forName(className);
      } catch (ClassNotFoundException e) {
        System.out.println("Skipping suite (not on classpath): " + className);
        continue;
      }

      final var classDisplayName = clazz.getAnnotation(DisplayName.class);
      final var displayName = classDisplayName != null ? classDisplayName.value() : clazz.getSimpleName();
      System.out.println("Running Suite: " + displayName);

      // Collect test classes including nested @Nested classes
      final var classesToRun = new ArrayList<Class<?>>();
      classesToRun.add(clazz);
      for (final var member : clazz.getDeclaredClasses()) {
        if (member.isAnnotationPresent(Nested.class)) {
          classesToRun.add(member);
        }
      }

      for (final var targetClass : classesToRun) {
        final var nestedName = targetClass.getAnnotation(DisplayName.class);
        if (targetClass != clazz && nestedName != null)
          System.out.println("  -> " + nestedName.value());

        for (final var m : targetClass.getDeclaredMethods()) {
          if (m.isAnnotationPresent(Test.class)) {
            totalTests++;
            final var methodDisplayName = m.getAnnotation(DisplayName.class);
            final var testName = methodDisplayName != null ? methodDisplayName.value() : m.getName();

            try {
              final var instance = instantiateClass(targetClass, clazz);
              m.setAccessible(true);
              m.invoke(instance);
              passedTests++;
              System.out.println("    [PASS] " + testName);
            } catch (InvocationTargetException ite) {
              failedTests++;
              final var cause = ite.getCause() != null ? ite.getCause() : ite;
              final var failureMsg = "[" + clazz.getSimpleName() + "] " + testName + " FAILED: " + cause.toString();
              failureDetails.add(failureMsg);
              System.out.println("    [FAIL] " + testName + " -> " + cause.getMessage());
            } catch (Exception e) {
              failedTests++;
              failureDetails.add("[" + clazz.getSimpleName() + "] " + testName + " ERROR: " + e.getMessage());
              System.out.println("    [ERROR] " + testName + " -> " + e.getMessage());
            }
          }
        }
      }
      System.out.println();
    }

    System.out.println("===============================================================================");
    System.out.println("                            E2E Test Execution Summary                         ");
    System.out.println("===============================================================================");
    System.out.println(String.format("Total Executed: %d | Passed: %d | Failed: %d", totalTests, passedTests, failedTests));

    if (!failureDetails.isEmpty()) {
      System.out.println("\nFailures / Expected Gaps (Pre-M1 to M4):");
      for (final var fail : failureDetails)
        System.out.println("  * " + fail);
    }

    System.out.println("===============================================================================");
    if (failedTests > 0) {
      System.out.println("Status: FAILED (" + failedTests + " failures detected).");
      System.exit(1);
    } else {
      System.out.println("Status: SUCCESS (100% PASS).");
      System.exit(0);
    }
  }

  private static Object instantiateClass(Class<?> target, Class<?> root) throws Exception {
    if (target == root || Modifier.isStatic(target.getModifiers())) {
      var ctor = target.getDeclaredConstructor();
      ctor.setAccessible(true);
      return ctor.newInstance();
    } else {
      final var rootInstance = instantiateClass(root, root);
      final var ctor = target.getDeclaredConstructor(root);
      ctor.setAccessible(true);
      return ctor.newInstance(rootInstance);
    }
  }
}
