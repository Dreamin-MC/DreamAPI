package fr.dreamin.dreamapi.api.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VersionUtils Adversarial Stress Tests (Challenger M2-1)")
public class VersionUtilsAdversarialTest {

  @Nested
  @DisplayName("1. Version Parsing Adversarial Tests")
  class ParsingAdversarialTests {

    @Test
    @DisplayName("Parse version '26.4+'")
    void testParse26_4_Plus() {
      final var parts = VersionUtils.toMajorMinorPatch("26.4+");
      assertArrayEquals(new int[]{26, 4, 0}, parts);
    }

    @Test
    @DisplayName("Parse version '26.4+build.22'")
    void testParse26_4_PlusBuild22() {
      final var parts = VersionUtils.toMajorMinorPatch("26.4+build.22");
      assertArrayEquals(new int[]{26, 4, 0}, parts);
    }

    @Test
    @DisplayName("Parse version '26.4.1+'")
    void testParse26_4_1_Plus() {
      final var parts = VersionUtils.toMajorMinorPatch("26.4.1+");
      assertArrayEquals(new int[]{26, 4, 1}, parts);
    }

    @Test
    @DisplayName("Parse version '26.4+beta-1'")
    void testParse26_4_PlusBetaDash1() {
      final var parts = VersionUtils.toMajorMinorPatch("26.4+beta-1");
      assertArrayEquals(new int[]{26, 4, 0}, parts);
    }

    @Test
    @DisplayName("Parse version '026.004.000'")
    void testParseZeroPadded26_4_0() {
      final var parts = VersionUtils.toMajorMinorPatch("026.004.000");
      assertArrayEquals(new int[]{26, 4, 0}, parts);
    }

    @Test
    @DisplayName("Parse version '26.4-SNAPSHOT'")
    void testParse26_4_Snapshot() {
      final var parts = VersionUtils.toMajorMinorPatch("26.4-SNAPSHOT");
      assertArrayEquals(new int[]{26, 4, 0}, parts);
    }

    @Test
    @DisplayName("Parse empty string ''")
    void testParseEmpty() {
      final var parts = VersionUtils.toMajorMinorPatch("");
      assertArrayEquals(new int[]{0, 0, 0}, parts);
    }

    @Test
    @DisplayName("Parse null version")
    void testParseNull() {
      final var parts = VersionUtils.toMajorMinorPatch(null);
      assertArrayEquals(new int[]{0, 0, 0}, parts);
    }

    @Test
    @DisplayName("Parse purely alphabetic 'abc'")
    void testParseAlphabetical() {
      final var parts = VersionUtils.toMajorMinorPatch("abc");
      assertArrayEquals(new int[]{0, 0, 0}, parts);
    }

    @Test
    @DisplayName("Parse excessive segments '26.1.2.3.4'")
    void testParseFiveSegments() {
      final var parts = VersionUtils.toMajorMinorPatch("26.1.2.3.4");
      assertArrayEquals(new int[]{26, 1, 2}, parts);
    }

    @Test
    @DisplayName("Parse overflow digits gracefully")
    void testParseOverflowDigits() {
      final var parts = VersionUtils.toMajorMinorPatch("9999999999999999999999.1.2");
      assertArrayEquals(new int[]{0, 1, 2}, parts);
    }

    @Test
    @DisplayName("Parse version with leading 'v' prefix 'v26.4.0'")
    void testParseVersionWithVPrefix() {
      final var parts = VersionUtils.toMajorMinorPatch("v26.4.0");
      assertArrayEquals(new int[]{26, 4, 0}, parts);
    }

    @Test
    @DisplayName("Parse version with empty middle segments '26..4'")
    void testParseMissingSegment() {
      final var parts = VersionUtils.toMajorMinorPatch("26..4");
      assertArrayEquals(new int[]{26, 0, 4}, parts);
    }
  }

  @Nested
  @DisplayName("2. compare(v1, v2) Adversarial Tests")
  class CompareAdversarialTests {

    @ParameterizedTest(name = "compare({0}, {1}) should be {2}")
    @CsvSource({
      "26.4, 26.4, 0",
      "26.4+, 26.4, 0",
      "26.4, 26.4+, 0",
      "26.4+build.22, 26.4-SNAPSHOT, 0",
      "026.004.000, 26.4.0, 0",
      "26.4.1+, 26.4.0, 1",
      "26.4.0, 26.4.1+, -1",
      "27.0.0, 26.4.0, 1",
      "26.3.9, 26.4.0, -1",
      "26.1.2.3.4, 26.1.2, 0"
    })
    void testCompareValues(String v1, String v2, int expectedSign) {
      final var result = VersionUtils.compare(v1, v2);
      if (expectedSign == 0)
        assertEquals(0, result); else if (expectedSign > 0)
        assertTrue(result > 0, () -> "Expected positive but got " + result); else
        assertTrue(result < 0, () -> "Expected negative but got " + result);
    }

    @Test
    @DisplayName("compare with null and empty inputs")
    void testCompareWithNullsAndEmpty() {
      assertEquals(0, VersionUtils.compare(null, null));
      assertEquals(0, VersionUtils.compare(null, ""));
      assertEquals(0, VersionUtils.compare("", "abc"));
      assertTrue(VersionUtils.compare("26.4", null) > 0);
      assertTrue(VersionUtils.compare(null, "26.4") < 0);
      assertTrue(VersionUtils.compare("26.4", "") > 0);
      assertTrue(VersionUtils.compare("", "26.4") < 0);
    }
  }

  @Nested
  @DisplayName("3. isHigherOrEqual(v1, v2) Adversarial Tests")
  class HigherOrEqualAdversarialTests {

    @Test
    @DisplayName("isHigherOrEqual normal and boundary cases")
    void testHigherOrEqual() {
      assertTrue(VersionUtils.isHigherOrEqual("26.4", "26.4"));
      assertTrue(VersionUtils.isHigherOrEqual("26.4+", "26.4"));
      assertTrue(VersionUtils.isHigherOrEqual("26.4", "26.4+"));
      assertTrue(VersionUtils.isHigherOrEqual("26.4.1+", "26.4"));
      assertFalse(VersionUtils.isHigherOrEqual("26.4", "26.4.1+"));
      assertTrue(VersionUtils.isHigherOrEqual("26.4-SNAPSHOT", "26.3.0"));
      assertTrue(VersionUtils.isHigherOrEqual("026.004.000", "26.4.0"));
      assertTrue(VersionUtils.isHigherOrEqual("26.1.2.3.4", "26.1.2"));
      assertFalse(VersionUtils.isHigherOrEqual("26.1.2", "26.1.3"));
    }

    @Test
    @DisplayName("isHigherOrEqual null safety")
    void testHigherOrEqualNullSafety() {
      assertTrue(VersionUtils.isHigherOrEqual(null, null));
      assertTrue(VersionUtils.isHigherOrEqual("26.4", null));
      assertFalse(VersionUtils.isHigherOrEqual(null, "26.4"));
    }
  }

  @Nested
  @DisplayName("4. isWithinRange(version, min, max) Adversarial Tests")
  class RangeAdversarialTests {

    @Test
    @DisplayName("Normal inclusive ranges")
    void testNormalRanges() {
      assertTrue(VersionUtils.isWithinRange("26.2.0", "26.1.0", "26.3.0"));
      assertTrue(VersionUtils.isWithinRange("26.1.0", "26.1.0", "26.3.0"));
      assertTrue(VersionUtils.isWithinRange("26.3.0", "26.1.0", "26.3.0"));
      assertFalse(VersionUtils.isWithinRange("26.0.9", "26.1.0", "26.3.0"));
      assertFalse(VersionUtils.isWithinRange("26.3.1", "26.1.0", "26.3.0"));
    }

    @Test
    @DisplayName("Boundary tests with '+' and build suffixes")
    void testBoundaryWithSuffixes() {
      assertTrue(VersionUtils.isWithinRange("26.4.0", "26.1.2", "26.4+"));
      assertTrue(VersionUtils.isWithinRange("26.4+", "26.1.2", "26.4+"));
      assertTrue(VersionUtils.isWithinRange("26.4+build.22", "26.1.2", "26.4+"));
      assertFalse(VersionUtils.isWithinRange("26.4.1+", "26.1.2", "26.4+"));
      assertTrue(VersionUtils.isWithinRange("26.4.1+", "26.1.2", "26.4.1+"));
      assertTrue(VersionUtils.isWithinRange("026.004.000", "26.1.2", "26.4+"));
    }

    @Test
    @DisplayName("Exact point boundary (min == max == version)")
    void testExactSinglePointRange() {
      assertTrue(VersionUtils.isWithinRange("26.1.2", "26.1.2", "26.1.2"));
      assertTrue(VersionUtils.isWithinRange("26.4+", "26.4+", "26.4+"));
      assertFalse(VersionUtils.isWithinRange("26.1.3", "26.1.2", "26.1.2"));
      assertFalse(VersionUtils.isWithinRange("26.1.1", "26.1.2", "26.1.2"));
    }

    @Test
    @DisplayName("Inverted ranges (min > max) must return false")
    void testInvertedRanges() {
      assertFalse(VersionUtils.isWithinRange("26.2.0", "26.3.0", "26.1.0"),
        "Version between min and max when min > max must return false");
      assertFalse(VersionUtils.isWithinRange("26.3.0", "26.3.0", "26.1.0"),
        "Version equal to inverted min must return false");
      assertFalse(VersionUtils.isWithinRange("26.1.0", "26.3.0", "26.1.0"),
        "Version equal to inverted max must return false");
      assertFalse(VersionUtils.isWithinRange("26.4.0", "26.4.1+", "26.4+"),
        "Inverted range [26.4.1+, 26.4+] must return false");
    }

    @Test
    @DisplayName("Unbounded, null, and blank bounds")
    void testUnboundedAndBlankBounds() {
      assertTrue(VersionUtils.isWithinRange("26.2.0", null, null));
      assertTrue(VersionUtils.isWithinRange("26.2.0", "", ""));
      assertTrue(VersionUtils.isWithinRange("26.2.0", "   ", "   "));
      assertTrue(VersionUtils.isWithinRange("26.2.0", "26.1.0", null));
      assertTrue(VersionUtils.isWithinRange("26.2.0", "26.1.0", ""));
      assertFalse(VersionUtils.isWithinRange("26.0.9", "26.1.0", null));
      assertTrue(VersionUtils.isWithinRange("26.2.0", null, "26.3.0"));
      assertTrue(VersionUtils.isWithinRange("26.2.0", "", "26.3.0"));
      assertFalse(VersionUtils.isWithinRange("26.3.1", null, "26.3.0"));
    }

    @Test
    @DisplayName("Null and malformed version parameter")
    void testNullAndMalformedVersionParam() {
      assertFalse(VersionUtils.isWithinRange(null, "26.1.0", "26.3.0"),
        "Null version is treated as 0.0.0 and is below min 26.1.0");
      assertTrue(VersionUtils.isWithinRange(null, null, null),
        "Null version with unbounded range returns true");
      assertFalse(VersionUtils.isWithinRange("abc", "26.1.0", "26.3.0"),
        "Malformed version 'abc' treated as 0.0.0 and is below min");
      assertTrue(VersionUtils.isWithinRange("abc", null, null),
        "Malformed version 'abc' with unbounded range returns true");
    }
  }
}
