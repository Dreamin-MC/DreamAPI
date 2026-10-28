package fr.dreamin.dreamapi.api.dependency;

import fr.dreamin.dreamapi.core.dependency.DependencyLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("@RequiresVersion Annotation Gating Tests (Tiers 1 & 2)")
public class RequiresVersionTest {

  // Test fixture classes
  static class UnannotatedService {}

  @RequiresVersion(min = "26.1.0")
  static class ServiceMatchingMin {}

  @RequiresVersion(min = "26.2.0")
  static class ServiceHigherMinGated {}

  @RequiresVersion(min = "26.1.0", max = "26.3.0")
  static class ServiceValidRange {}

  @RequiresVersion(min = "26.0.0", max = "26.1.0")
  static class ServiceExpiredMaxGated {}

  @RequiresVersion(min = "26.4+")
  static class ServiceWithPlusSuffixGated {}

  @RequiresVersion(min = "26.1.2", max = "26.1.2")
  static class ServiceExactBoundaryMatch {}

  @RequiresVersion(min = "26.1.0", max = "")
  static class ServiceEmptyMaxTreatedUnbounded {}

  @RequiresVersion(min = "   ", max = "   ")
  static class ServiceBlankBoundsTreatedUnbounded {}

  // =========================================================================
  // TIER 1: Feature Coverage (Standard Annotation Gating)
  // =========================================================================

  @Nested
  @DisplayName("Tier 1: Feature Coverage")
  class Tier1FeatureCoverage {

    @Test
    @DisplayName("TC-T1-RV-01: Class without @RequiresVersion is accepted by default")
    void testUnannotatedClassPasses() {
      assertTrue(DependencyLoader.checkVersion(UnannotatedService.class),
        "Unannotated class should pass version check unconditionally");
    }

    @Test
    @DisplayName("TC-T1-RV-02: Class with matching min version (26.1.0 <= 26.1.2) is accepted")
    void testMatchingMinVersionPasses() {
      assertTrue(DependencyLoader.checkVersion(ServiceMatchingMin.class),
        "Service requiring min 26.1.0 should pass on server 26.1.2");
    }

    @Test
    @DisplayName("TC-T1-RV-03: Class with future min version (26.2.0 > 26.1.2) is gated")
    void testHigherMinVersionGated() {
      assertFalse(DependencyLoader.checkVersion(ServiceHigherMinGated.class),
        "Service requiring min 26.2.0 should be gated on server 26.1.2");
    }

    @Test
    @DisplayName("TC-T1-RV-04: Class with valid range [26.1.0, 26.3.0] containing current version is accepted")
    void testValidRangePasses() {
      assertTrue(DependencyLoader.checkVersion(ServiceValidRange.class),
        "Service requiring [26.1.0, 26.3.0] should pass on server 26.1.2");
    }

    @Test
    @DisplayName("TC-T1-RV-05: Class with expired max range [26.0.0, 26.1.0] is gated")
    void testExpiredMaxGated() {
      assertFalse(DependencyLoader.checkVersion(ServiceExpiredMaxGated.class),
        "Service capped at max 26.1.0 should be gated on server 26.1.2");
    }

    @Test
    @DisplayName("TC-T1-RV-06: Annotation default values verification")
    void testAnnotationDefaults() throws NoSuchMethodException {
      final var maxMethod = RequiresVersion.class.getMethod("max");
      assertEquals("", maxMethod.getDefaultValue(),
        "Default max version value in @RequiresVersion must be empty string");
    }
  }

  // =========================================================================
  // TIER 2: Boundary & Corner Cases (Limits, Suffixes, Edge Formats)
  // =========================================================================

  @Nested
  @DisplayName("Tier 2: Boundary & Corner Cases")
  class Tier2BoundaryAndCornerCases {

    @Test
    @DisplayName("TC-T2-RV-01: Service with plus suffix in min version '26.4+' does not throw exception")
    void testVersionWithPlusRequirement() {
      // Server 26.1.2 < 26.4+, so it should evaluate to false without throwing NumberFormatException
      final var allowed = DependencyLoader.checkVersion(ServiceWithPlusSuffixGated.class);
      assertFalse(allowed, "Server 26.1.2 should not satisfy min 26.4+");
    }

    @Test
    @DisplayName("TC-T2-RV-02: Exact match at both min and max boundary [26.1.2, 26.1.2] is accepted")
    void testExactBoundaryMinAndMax() {
      assertTrue(DependencyLoader.checkVersion(ServiceExactBoundaryMatch.class),
        "Exact boundary [26.1.2, 26.1.2] must be inclusive and allowed");
    }

    @Test
    @DisplayName("TC-T2-RV-03: Empty max string is treated as unbounded upper bound")
    void testEmptyMaxTreatedAsUnbounded() {
      assertTrue(DependencyLoader.checkVersion(ServiceEmptyMaxTreatedUnbounded.class),
        "Empty max string should be treated as no upper limit");
    }

    @Test
    @DisplayName("TC-T2-RV-04: Blank min and max strings are treated as unbounded")
    void testBlankBoundsTreatedAsUnbounded() {
      assertTrue(DependencyLoader.checkVersion(ServiceBlankBoundsTreatedUnbounded.class),
        "Blank bounds should be treated as unbounded");
    }

    @Test
    @DisplayName("TC-T2-RV-05: Null class throws NullPointerException")
    void testNullClassThrowsNullPointerException() {
      assertThrows(NullPointerException.class, () -> DependencyLoader.checkVersion(null),
        "Passing null class to DependencyLoader.checkVersion must throw NullPointerException");
    }

    @Test
    @DisplayName("TC-T2-RV-06: Package-level annotation fallback check")
    void testPackageLevelAnnotationFallback() {
      // Testing that when class has no annotation, package is checked
      final var pkg = RequiresVersionTest.class.getPackage();
      assertNotNull(pkg, "Package must not be null");
      // Even without package annotation on test package, fallback returns true
      assertTrue(DependencyLoader.checkVersion(UnannotatedService.class));
    }
  }
}
