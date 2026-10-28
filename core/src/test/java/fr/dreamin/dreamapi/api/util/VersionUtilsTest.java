package fr.dreamin.dreamapi.api.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VersionUtils Multi-Version & Parsing Tests (Tiers 1 & 2)")
public class VersionUtilsTest {

  // =========================================================================
  // TIER 1: Feature Coverage (Happy-Path Version Parsing & Comparisons)
  // =========================================================================

  @Nested
  @DisplayName("Tier 1: Feature Coverage")
  class Tier1FeatureCoverage {

    @Test
    @DisplayName("TC-T1-VU-01: Parse standard 3-part version string '26.1.2'")
    void testParseStandardThreePartVersion() {
      final var parts = VersionUtils.toMajorMinorPatch("26.1.2");
      assertNotNull(parts, "Parsed version array must not be null");
      assertEquals(3, parts.length, "Parsed version array must have length 3");
      assertEquals(26, parts[0], "Major version must be 26");
      assertEquals(1, parts[1], "Minor version must be 1");
      assertEquals(2, parts[2], "Patch version must be 2");
    }

    @Test
    @DisplayName("TC-T1-VU-02: Parse 2-part version string '26.2'")
    void testParseTwoPartVersion() {
      final var parts = VersionUtils.toMajorMinorPatch("26.2");
      assertEquals(26, parts[0]);
      assertEquals(2, parts[1]);
      assertEquals(0, parts[2], "Missing patch version must default to 0");
    }

    @Test
    @DisplayName("TC-T1-VU-03: Parse 1-part version string '26'")
    void testParseSinglePartVersion() {
      final var parts = VersionUtils.toMajorMinorPatch("26");
      assertEquals(26, parts[0]);
      assertEquals(0, parts[1], "Missing minor version must default to 0");
      assertEquals(0, parts[2], "Missing patch version must default to 0");
    }

    @Test
    @DisplayName("TC-T1-VU-04: Parse pre-release version with dash '26.3-rc1'")
    void testParseVersionWithDashPreRelease() {
      final var parts = VersionUtils.toMajorMinorPatch("26.3-rc1");
      assertEquals(26, parts[0]);
      assertEquals(3, parts[1]);
      assertEquals(0, parts[2]);
    }

    @Test
    @DisplayName("TC-T1-VU-05: Parse version with trailing build metadata '26.1.2.build.123'")
    void testParseVersionWithTrailingBuildMetadata() {
      final var parts = VersionUtils.toMajorMinorPatch("26.1.2.build.123");
      assertEquals(26, parts[0]);
      assertEquals(1, parts[1]);
      assertEquals(2, parts[2]);
    }

    @Test
    @DisplayName("TC-T1-VU-06: Range check with version within bounds")
    void testWithinRangeIncluded() {
      // Default fallback detected server version is 26.1.2
      assertTrue(VersionUtils.isWithinRange("26.1.0", "26.3.0"),
        "26.1.2 should be within range [26.1.0, 26.3.0]");
    }

    @Test
    @DisplayName("TC-T1-VU-07: Range check when server version is below min bound")
    void testWithinRangeBelowMin() {
      assertFalse(VersionUtils.isWithinRange("26.2.0", null),
        "26.1.2 should not satisfy min version 26.2.0");
    }

    @Test
    @DisplayName("TC-T1-VU-08: Range check when server version is above max bound")
    void testWithinRangeAboveMax() {
      assertFalse(VersionUtils.isWithinRange(null, "26.1.0"),
        "26.1.2 should not satisfy max version 26.1.0");
    }

    @Test
    @DisplayName("TC-T1-VU-09: Range check with unbounded min and max")
    void testWithinRangeUnbounded() {
      assertTrue(VersionUtils.isWithinRange(null, null),
        "Null bounds should match any server version");
    }

    @Test
    @DisplayName("TC-T1-VU-10: Server higher or equal comparison logic")
    void testIsServerHigherOrEqual() {
      assertTrue(VersionUtils.isServerHigherOrEqual("26.1.0"), "26.1.2 should be >= 26.1.0");
      assertTrue(VersionUtils.isServerHigherOrEqual("26.1.2"), "26.1.2 should be >= 26.1.2");
      assertFalse(VersionUtils.isServerHigherOrEqual("26.2.0"), "26.1.2 should NOT be >= 26.2.0");
    }

    @Test
    @DisplayName("TC-T1-VU-11: Higher or equal than server comparison logic")
    void testIsHigherOrEqualThanServer() {
      assertTrue(VersionUtils.isHigherOrEqualThanServer("26.1.2"), "26.1.2 is >= server 26.1.2");
      assertTrue(VersionUtils.isHigherOrEqualThanServer("26.2.0"), "26.2.0 is >= server 26.1.2");
      assertFalse(VersionUtils.isHigherOrEqualThanServer("26.1.0"), "26.1.0 is NOT >= server 26.1.2");
    }
  }

  // =========================================================================
  // TIER 2: Boundary & Corner Cases (Limits, Suffixes, Edge Conditions)
  // =========================================================================

  @Nested
  @DisplayName("Tier 2: Boundary & Corner Cases")
  class Tier2BoundaryAndCornerCases {

    @Test
    @DisplayName("TC-T2-VU-01: Parse version with plus suffix '26.4+' (R3 requirement)")
    void testParseVersionWithPlusSuffix() {
      // Must not throw NumberFormatException: For input string: "4+"
      final var parts = VersionUtils.toMajorMinorPatch("26.4+");
      assertNotNull(parts);
      assertEquals(26, parts[0], "Major must be 26");
      assertEquals(4, parts[1], "Minor must be 4");
      assertEquals(0, parts[2], "Patch must be 0");
    }

    @ParameterizedTest
    @ValueSource(strings = {"26.4+build.22", "26.4.1+", "26.4+beta-1"})
    @DisplayName("TC-T2-VU-02: Parse version with mixed plus and build suffixes")
    void testParseVersionWithMixedPlusAndBuild(String raw) {
      final var parts = VersionUtils.toMajorMinorPatch(raw);
      assertEquals(26, parts[0]);
      assertEquals(4, parts[1]);
    }

    @Test
    @DisplayName("TC-T2-VU-03: Parse empty version string returns zero version")
    void testParseEmptyVersionString() {
      final var parts = VersionUtils.toMajorMinorPatch("");
      assertArrayEquals(new int[]{0, 0, 0}, parts);
    }

    @Test
    @DisplayName("TC-T2-VU-04: Parse whitespace-only version string returns zero version")
    void testParseWhitespaceOnlyVersionString() {
      final var parts = VersionUtils.toMajorMinorPatch("    ");
      assertArrayEquals(new int[]{0, 0, 0}, parts);
    }

    @Test
    @DisplayName("TC-T2-VU-05: Parse version with leading and trailing whitespace")
    void testParseVersionLeadingTrailingWhitespace() {
      final var parts = VersionUtils.toMajorMinorPatch("  26.2  ");
      assertEquals(26, parts[0]);
      assertEquals(2, parts[1]);
      assertEquals(0, parts[2]);
    }

    @Test
    @DisplayName("TC-T2-VU-06: Parse zero-padded version numbers '026.002.001'")
    void testParseVersionZeroPadded() {
      final var parts = VersionUtils.toMajorMinorPatch("026.002.001");
      assertEquals(26, parts[0]);
      assertEquals(2, parts[1]);
      assertEquals(1, parts[2]);
    }

    @Test
    @DisplayName("TC-T2-VU-07: Exact boundary equality check on min and max")
    void testWithinRangeExactBoundary() {
      assertTrue(VersionUtils.isWithinRange("26.1.2", "26.1.2"),
        "Exact boundary match must be inclusive");
    }

    @Test
    @DisplayName("TC-T2-VU-08: Inverted range check where min > max returns false")
    void testWithinRangeInverted() {
      assertFalse(VersionUtils.isWithinRange("26.3.0", "26.1.0"),
        "Inverted range [26.3.0, 26.1.0] must evaluate to false");
    }

    @Test
    @DisplayName("TC-T2-VU-09: Version array length validation")
    void testServerHigherOrEqualInvalidLength() {
      assertThrows(IllegalArgumentException.class, () -> VersionUtils.isServerHigherOrEqual(26, 1),
        "Array length != 3 must throw IllegalArgumentException");
      assertThrows(IllegalArgumentException.class, () -> VersionUtils.isHigherOrEqualThanServer(26, 1, 2, 3),
        "Array length != 3 must throw IllegalArgumentException");
    }

    @Test
    @DisplayName("TC-T2-VU-10: Truncate excessive version segments gracefully")
    void testParseVersionExcessiveSegments() {
      final var parts = VersionUtils.toMajorMinorPatch("26.1.2.3.4.5");
      assertEquals(26, parts[0]);
      assertEquals(1, parts[1]);
      assertEquals(2, parts[2]);
    }
  }
}
