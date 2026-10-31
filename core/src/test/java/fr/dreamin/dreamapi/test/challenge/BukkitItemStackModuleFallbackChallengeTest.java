package fr.dreamin.dreamapi.test.challenge;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dreamin.dreamapi.core.bukkit.module.BukkitItemStackModule;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BukkitItemStackModule Legacy Map Fallback & Binary Challenge Tests (Feature F3)")
class BukkitItemStackModuleFallbackChallengeTest {

  private final ObjectMapper mapper = new ObjectMapper()
    .registerModule(new BukkitItemStackModule());

  @Test
  @DisplayName("Empirically verify START_OBJECT JSON routes to legacy ItemStack.deserialize(Map)")
  void testLegacyMapFallbackRouting() {
    final var legacyJson = """
      {
        "==": "org.bukkit.inventory.ItemStack",
        "v": 3953,
        "type": "STONE",
        "amount": 4
      }
      """;

    // In a headless environment without a Paper server RegistryAccess,
    // reaching ItemStack.deserialize(Map) is proven by either NoClassDefFoundError /
    // ExceptionInInitializerError / NullPointerException originating from Bukkit ItemFactory/RegistryAccess,
    // rather than a Jackson JSON parsing error.
    final var ex = assertThrows(Throwable.class, () -> mapper.readValue(legacyJson, ItemStack.class));
    assertFalse(ex instanceof JsonParseException, "Legacy map must be parsed into Map before ItemStack deserialization");
    final var causeChain = ex.toString();
    final var isBukkitOrRegistryError = causeChain.contains("Registry")
      || causeChain.contains("ItemFactory")
      || causeChain.contains("NullPointerException")
      || causeChain.contains("ExceptionInInitializerError")
      || causeChain.contains("NoClassDefFoundError");
    assertTrue(isBukkitOrRegistryError, "Exception must originate from Bukkit ItemStack.deserialize(Map): " + causeChain);
  }

  @Test
  @DisplayName("Empirically verify binary Base64 JSON routes to ItemStack.deserializeBytes(byte[])")
  void testBinaryRouting() {
    final var base64Json = "\"AQIDBA==\""; // Valid Base64

    final var ex = assertThrows(Throwable.class, () -> mapper.readValue(base64Json, ItemStack.class));
    assertFalse(ex instanceof JsonParseException, "Base64 string must decode successfully");
    final var causeChain = ex.toString();
    final var isBukkitOrRegistryError = causeChain.contains("Registry")
      || causeChain.contains("ItemFactory")
      || causeChain.contains("NullPointerException")
      || causeChain.contains("ExceptionInInitializerError")
      || causeChain.contains("NoClassDefFoundError");
    assertTrue(isBukkitOrRegistryError, "Exception must originate from Bukkit ItemStack.deserializeBytes: " + causeChain);
  }

  @Test
  @DisplayName("Empirically verify invalid and corrupted input handling")
  void testInvalidInputHandling() {
    assertThrows(JsonParseException.class, () -> mapper.readValue("\"!invalid-base64!\"", ItemStack.class));
    assertThrows(Exception.class, () -> mapper.readValue("12345", ItemStack.class));
    assertThrows(Exception.class, () -> mapper.readValue("true", ItemStack.class));
    assertThrows(Exception.class, () -> mapper.readValue("[1, 2, 3]", ItemStack.class));
  }

  @Test
  @DisplayName("Empirically verify null and empty values return null")
  void testNullAndEmptyHandling() throws Exception {
    assertNull(mapper.readValue("null", ItemStack.class));
    assertNull(mapper.readValue("\"\"", ItemStack.class));
    assertEquals("null", mapper.writeValueAsString(null));
  }
}
