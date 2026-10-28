package fr.dreamin.dreamapi.api.nms.packet;

import fr.dreamin.dreamapi.api.worldborder.model.WorldBorderAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PacketReflection Resilient Field Resolution Tests (Tiers 1 & 2)")
public class PacketReflectionTest {

  // Test hierarchy fixtures for reflection testing
  static class GrandparentPacket {
    private final String header = "grandparent_header";
    public static final int PROTOCOL_VERSION = 760;
  }

  static class ParentPacket extends GrandparentPacket {
    private int entityId = 101;
    protected String legacyToken = "token_v1";
  }

  static class TargetPacket extends ParentPacket {
    private double velocity = 14.5;
    private String modernToken = "token_v2";
    private final boolean active = true;
  }

  static class AmbiguousPacket {
    private int id1 = 1;
    private int id2 = 2;
  }

  // Reflection helper calling PacketReflection resilient resolution algorithm specified in R3 / Feature 9
  public static Field resolveField(Class<?> clazz, Class<?> fallbackType, String... candidateNames) throws NoSuchFieldException {
    return PacketReflection.resolveField(clazz, fallbackType, candidateNames);
  }

  // =========================================================================
  // TIER 1: Feature Coverage (Field Resolution, Hierarchy Traversal, Fallbacks)
  // =========================================================================

  @Nested
  @DisplayName("Tier 1: Feature Coverage")
  class Tier1FeatureCoverage {

    @Test
    @DisplayName("TC-T1-PR-01: Direct field resolution on target class by exact name")
    void testResolveDirectFieldByName() throws Exception {
      final var field = resolveField(TargetPacket.class, null, "velocity");
      assertNotNull(field, "Field 'velocity' must be found");
      assertEquals(double.class, field.getType());

      final var instance = new TargetPacket();
      assertEquals(14.5, field.getDouble(instance));
    }

    @Test
    @DisplayName("TC-T1-PR-02: Class hierarchy traversal resolves field declared in parent superclass")
    void testResolveFieldInSuperclass() throws Exception {
      final var field = resolveField(TargetPacket.class, null, "entityId");
      assertNotNull(field, "Field 'entityId' from ParentPacket must be resolved");
      assertEquals(int.class, field.getType());

      final var instance = new TargetPacket();
      assertEquals(101, field.getInt(instance));
    }

    @Test
    @DisplayName("TC-T1-PR-03: Candidate names fallback resolves when primary name is missing")
    void testResolveFieldWithCandidateNames() throws Exception {
      // "oldField" does not exist, but "modernToken" exists
      final var field = resolveField(TargetPacket.class, null, "oldField", "modernToken");
      assertNotNull(field);
      assertEquals("modernToken", field.getName());

      final var instance = new TargetPacket();
      assertEquals("token_v2", field.get(instance));
    }

    @Test
    @DisplayName("TC-T1-PR-04: Type-based fallback resolves when all candidate names are missing")
    void testResolveFieldByTypeFallback() throws Exception {
      // Candidate names "unknownA" and "unknownB" do not exist, fall back to type double.class
      final var field = resolveField(TargetPacket.class, double.class, "unknownA", "unknownB");
      assertNotNull(field);
      assertEquals("velocity", field.getName());
      assertEquals(double.class, field.getType());
    }

    @Test
    @DisplayName("TC-T1-PR-05: Resolved private field has accessible flag granted")
    void testFieldAccessibilityMadeTrue() throws Exception {
      final var field = resolveField(TargetPacket.class, null, "active");
      assertTrue(field.canAccess(new TargetPacket()), "Resolved private field must be accessible");
    }

    @Test
    @DisplayName("TC-T1-PR-06: WorldBorderAction enum contract in fr.dreamin.dreamapi.api.worldborder.model")
    void testWorldBorderActionEnumContract() {
      // Verifies all required actions exist and are co-located in nms to prevent circular dependency
      assertEquals(6, WorldBorderAction.values().length);
      assertNotNull(WorldBorderAction.valueOf("INITIALIZE"));
      assertNotNull(WorldBorderAction.valueOf("LERP_SIZE"));
      assertNotNull(WorldBorderAction.valueOf("SET_CENTER"));
      assertNotNull(WorldBorderAction.valueOf("SET_SIZE"));
      assertNotNull(WorldBorderAction.valueOf("SET_WARNING_BLOCKS"));
      assertNotNull(WorldBorderAction.valueOf("SET_WARNING_TIME"));
    }
  }

  // =========================================================================
  // TIER 2: Boundary & Corner Cases (Deep Hierarchy, Missing Fields, Static)
  // =========================================================================

  @Nested
  @DisplayName("Tier 2: Boundary & Corner Cases")
  class Tier2BoundaryAndCornerCases {

    @Test
    @DisplayName("TC-T2-PR-01: Non-existent field name and non-matching type throws NoSuchFieldException")
    void testResolveFieldNotFoundThrowsNoSuchField() {
      assertThrows(NoSuchFieldException.class, () ->
        resolveField(TargetPacket.class, Float.class, "nonExistentField1", "nonExistentField2")
      );
    }

    @Test
    @DisplayName("TC-T2-PR-02: Hierarchy traversal terminates safely at Object.class")
    void testResolveFieldRootObjectClassBoundary() {
      assertThrows(NoSuchFieldException.class, () ->
        resolveField(Object.class, null, "anyField")
      );
    }

    @Test
    @DisplayName("TC-T2-PR-03: Deep inheritance traversal across 3+ levels")
    void testResolveFieldDeepInheritanceHierarchy() throws Exception {
      final var field = resolveField(TargetPacket.class, null, "header");
      assertNotNull(field, "Field 'header' from GrandparentPacket must be resolved");
      assertEquals(GrandparentPacket.class, field.getDeclaringClass());

      final var instance = new TargetPacket();
      assertEquals("grandparent_header", field.get(instance));
    }

    @Test
    @DisplayName("TC-T2-PR-04: Ambiguous type fallback returns first declared match deterministically")
    void testResolveFieldAmbiguousTypeFallback() throws Exception {
      final var field = resolveField(AmbiguousPacket.class, int.class, "missingName");
      assertNotNull(field);
      assertEquals("id1", field.getName(), "First declared field of matching type should be returned");
    }

    @Test
    @DisplayName("TC-T2-PR-05: Static field resolution across class hierarchy")
    void testResolveStaticVsInstanceField() throws Exception {
      final var field = resolveField(TargetPacket.class, null, "PROTOCOL_VERSION");
      assertNotNull(field);
      assertEquals(760, field.getInt(null));
    }

    @Test
    @DisplayName("TC-T2-PR-06: Primitive vs wrapper type fallback handling")
    void testPrimitiveVsWrapperTypeHandling() throws Exception {
      // Primitive double.class matches double field
      final var primitiveField = resolveField(TargetPacket.class, double.class, "none");
      assertEquals(double.class, primitiveField.getType());

      // Double.class wrapper does not assign to primitive without unboxing check
      assertThrows(NoSuchFieldException.class, () ->
        resolveField(TargetPacket.class, Double.class, "none")
      );
    }
  }

  // =========================================================================
  // TIER 5: Adversarial Stress Tests & Edge Case Mining
  // =========================================================================

  // Deep 5-level hierarchy fixtures
  static class L5AncientBase {
    private final long ancientTimestamp = 123456789L;
    protected String baseName = "L5";
  }

  static class L4Intermediate extends L5AncientBase {
    private final byte flags = (byte) 0x7F;
  }

  static class L3MidLayer extends L4Intermediate {
    private final short protocolId = 42;
  }

  static class L2ParentLayer extends L3MidLayer {
    private final ArrayList<String> itemList = new ArrayList<>();
    String shadowedName = "parentShadow";
  }

  static class L1LeafPacket extends L2ParentLayer {
    private final char marker = 'X';
    String shadowedName = "childShadow";
  }

  interface PacketInterface {
    String PROTOCOL_TAG = "DREAM_PACKET_V1";
  }

  static class PrimitiveAndBoxedPacket {
    private byte pByte = 1;
    private short pShort = 2;
    private int pInt = 3;
    private long pLong = 4L;
    private float pFloat = 5.0f;
    private double pDouble = 6.0;
    private boolean pBool = true;
    private char pChar = 'a';

    private Byte bByte = 10;
    private Short bShort = 20;
    private Integer bInt = 30;
    private Long bLong = 40L;
    private Float bFloat = 50.0f;
    private Double bDouble = 60.0;
    private Boolean bBool = false;
    private Character bChar = 'b';
  }

  @Nested
  @DisplayName("Tier 5: Adversarial Stress Tests")
  class Tier5AdversarialStressTests {

    @Test
    @DisplayName("TC-T5-PR-01: 5-level deep inheritance traversal across all hierarchy levels by name")
    void testFiveLevelDeepInheritanceByName() throws Exception {
      final var packet = new L1LeafPacket();

      final var fMarker = resolveField(L1LeafPacket.class, null, "marker");
      assertEquals('X', fMarker.getChar(packet));

      final var fItemList = resolveField(L1LeafPacket.class, null, "itemList");
      assertNotNull(fItemList.get(packet));

      final var fProtocolId = resolveField(L1LeafPacket.class, null, "protocolId");
      assertEquals((short) 42, fProtocolId.getShort(packet));

      final var fFlags = resolveField(L1LeafPacket.class, null, "flags");
      assertEquals((byte) 0x7F, fFlags.getByte(packet));

      final var fAncient = resolveField(L1LeafPacket.class, null, "ancientTimestamp");
      assertEquals(123456789L, fAncient.getLong(packet));
    }

    @Test
    @DisplayName("TC-T5-PR-02: 5-level deep inheritance type fallback when field exists only at root level")
    void testFiveLevelDeepInheritanceByTypeFallback() throws Exception {
      // Long field exists solely in L5AncientBase
      final var field = resolveField(L1LeafPacket.class, long.class, "bogusA", "bogusB");
      assertNotNull(field);
      assertEquals("ancientTimestamp", field.getName());
      assertEquals(L5AncientBase.class, field.getDeclaringClass());

      final var packet = new L1LeafPacket();
      assertEquals(123456789L, field.getLong(packet));
    }

    @Test
    @DisplayName("TC-T5-PR-03: Candidate names fallback with 4 missing candidates before existing candidate")
    void testMultipleCandidateNamesOrdering() throws Exception {
      final var field = resolveField(L1LeafPacket.class, null, "c1", "c2", "c3", "c4", "protocolId");
      assertNotNull(field);
      assertEquals("protocolId", field.getName());
      assertEquals(L3MidLayer.class, field.getDeclaringClass());
    }

    @Test
    @DisplayName("TC-T5-PR-04: Candidate names fallback where candidate 1 is missing, candidate 2 is in superclass")
    void testCandidateInSuperclassFallback() throws Exception {
      final var field = resolveField(L1LeafPacket.class, null, "missingCandidate1", "baseName");
      assertNotNull(field);
      assertEquals("baseName", field.getName());
      assertEquals(L5AncientBase.class, field.getDeclaringClass());
    }

    @Test
    @DisplayName("TC-T5-PR-05: Empty candidate array with valid type resolves field by type")
    void testEmptyCandidateNamesArrayWithType() throws Exception {
      final var field = resolveField(L1LeafPacket.class, char.class);
      assertNotNull(field);
      assertEquals("marker", field.getName());
      assertEquals(char.class, field.getType());
    }

    @Test
    @DisplayName("TC-T5-PR-06: Empty candidate array with null type throws NoSuchFieldException")
    void testEmptyCandidateNamesArrayWithNullTypeThrows() {
      assertThrows(NoSuchFieldException.class, () ->
        resolveField(L1LeafPacket.class, null)
      );
    }

    @Test
    @DisplayName("TC-T5-PR-07: Polymorphic subtyping and interface assignability in type fallback")
    void testPolymorphicSubtypingFallback() throws Exception {
      // ArrayList is assignable to List, Collection, and Iterable
      final var fList = resolveField(L1LeafPacket.class, List.class, "missing");
      assertEquals("itemList", fList.getName());

      final var fColl = resolveField(L1LeafPacket.class, Collection.class, "missing");
      assertEquals("itemList", fColl.getName());

      final var fIter = resolveField(L1LeafPacket.class, Iterable.class, "missing");
      assertEquals("itemList", fIter.getName());

      // Incompatible concrete subclass LinkedList must throw NoSuchFieldException
      assertThrows(NoSuchFieldException.class, () ->
        resolveField(L1LeafPacket.class, LinkedList.class, "missing")
      );
    }

    @Test
    @DisplayName("TC-T5-PR-08: Shadowed field priority: child class field returned before parent class")
    void testShadowedFieldHierarchyPriority() throws Exception {
      // By name: child's shadowedName is encountered first
      final var fName = resolveField(L1LeafPacket.class, null, "shadowedName");
      assertEquals(L1LeafPacket.class, fName.getDeclaringClass());

      final var packet = new L1LeafPacket();
      assertEquals("childShadow", fName.get(packet));

      // By type: child's shadowedName is encountered first
      final var fType = resolveField(L1LeafPacket.class, String.class, "nonExistentField");
      assertEquals(L1LeafPacket.class, fType.getDeclaringClass());
      assertEquals("childShadow", fType.get(packet));
    }

    @Test
    @DisplayName("TC-T5-PR-09: Complete primitive vs boxed separation across all 8 Java primitive types")
    void testCompletePrimitiveVsBoxedSeparation() throws Exception {
      // Primitive types must resolve primitive fields
      assertEquals("pByte", resolveField(PrimitiveAndBoxedPacket.class, byte.class, "x").getName());
      assertEquals("pShort", resolveField(PrimitiveAndBoxedPacket.class, short.class, "x").getName());
      assertEquals("pInt", resolveField(PrimitiveAndBoxedPacket.class, int.class, "x").getName());
      assertEquals("pLong", resolveField(PrimitiveAndBoxedPacket.class, long.class, "x").getName());
      assertEquals("pFloat", resolveField(PrimitiveAndBoxedPacket.class, float.class, "x").getName());
      assertEquals("pDouble", resolveField(PrimitiveAndBoxedPacket.class, double.class, "x").getName());
      assertEquals("pBool", resolveField(PrimitiveAndBoxedPacket.class, boolean.class, "x").getName());
      assertEquals("pChar", resolveField(PrimitiveAndBoxedPacket.class, char.class, "x").getName());

      // Boxed wrapper types must resolve boxed fields
      assertEquals("bByte", resolveField(PrimitiveAndBoxedPacket.class, Byte.class, "x").getName());
      assertEquals("bShort", resolveField(PrimitiveAndBoxedPacket.class, Short.class, "x").getName());
      assertEquals("bInt", resolveField(PrimitiveAndBoxedPacket.class, Integer.class, "x").getName());
      assertEquals("bLong", resolveField(PrimitiveAndBoxedPacket.class, Long.class, "x").getName());
      assertEquals("bFloat", resolveField(PrimitiveAndBoxedPacket.class, Float.class, "x").getName());
      assertEquals("bDouble", resolveField(PrimitiveAndBoxedPacket.class, Double.class, "x").getName());
      assertEquals("bBool", resolveField(PrimitiveAndBoxedPacket.class, Boolean.class, "x").getName());
      assertEquals("bChar", resolveField(PrimitiveAndBoxedPacket.class, Character.class, "x").getName());

      // Incompatible wrapper query when only primitive exists must fail
      assertThrows(NoSuchFieldException.class, () ->
        resolveField(AmbiguousPacket.class, Integer.class, "x")
      );
    }

    @Test
    @DisplayName("TC-T5-PR-10: NoSuchFieldException error message detail verification")
    void testNoSuchFieldExceptionMessageDetails() {
      NoSuchFieldException ex = assertThrows(NoSuchFieldException.class, () ->
        resolveField(TargetPacket.class, Runnable.class, "fakeA", "fakeB")
      );

      assertTrue(ex.getMessage().contains(TargetPacket.class.getName()), "Message should mention target class");
      assertTrue(ex.getMessage().contains("fakeA"), "Message should mention candidate fakeA");
      assertTrue(ex.getMessage().contains("fakeB"), "Message should mention candidate fakeB");
      assertTrue(ex.getMessage().contains(Runnable.class.getName()), "Message should mention requested type");
    }

    @Test
    @DisplayName("TC-T5-PR-11: Static interface field resolution")
    void testInterfaceFieldResolution() throws Exception {
      final var field = resolveField(PacketInterface.class, null, "PROTOCOL_TAG");
      assertNotNull(field);
      assertEquals("DREAM_PACKET_V1", field.get(null));
    }

    @Test
    @DisplayName("TC-T5-PR-12: Multi-threaded concurrent resolution stress harness")
    void testConcurrentResolutionStressHarness() throws Exception {
      final var threadCount = 16;
      final var iterations = 500;
      final var executor = Executors.newFixedThreadPool(threadCount);
      final var latch = new CountDownLatch(threadCount);
      final var failureCount = new AtomicInteger(0);

      for (int t = 0; t < threadCount; t++) {
        executor.submit(() -> {
          try {
            for (int i = 0; i < iterations; i++) {
              final var f1 = resolveField(L1LeafPacket.class, long.class, "missing", "ancientTimestamp");
              if (!f1.getName().equals("ancientTimestamp")) failureCount.incrementAndGet();

              final var f2 = resolveField(PrimitiveAndBoxedPacket.class, double.class, "bogus");
              if (!f2.getName().equals("pDouble")) failureCount.incrementAndGet();

              final var f3 = resolveField(PrimitiveAndBoxedPacket.class, Double.class, "bogus");
              if (!f3.getName().equals("bDouble")) failureCount.incrementAndGet();
            }
          } catch (Exception e) {
            failureCount.incrementAndGet();
          } finally {
            latch.countDown();
          }
        });
      }

      final var completed = latch.await(15, TimeUnit.SECONDS);
      executor.shutdown();
      assertTrue(completed, "Concurrent stress harness should complete within timeout");
      assertEquals(0, failureCount.get(), "Zero resolution failures during concurrent execution");
    }
  }
}

