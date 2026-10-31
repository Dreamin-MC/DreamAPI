package fr.dreamin.dreamapi.core.bukkit.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("BukkitItemStackModule Serialization Tests (Feature F3)")
class BukkitItemStackModuleTest {

  private final ObjectMapper mapper = new ObjectMapper()
    .registerModule(new BukkitItemStackModule());

  @Test
  @DisplayName("Serializing null ItemStack outputs null JSON")
  void testSerializeNull() throws Exception {
    final var json = mapper.writeValueAsString((ItemStack) null);
    assertEquals("null", json);
  }

  @Test
  @DisplayName("Deserializing null JSON returns null ItemStack")
  void testDeserializeNull() throws Exception {
    final var result = mapper.readValue("null", ItemStack.class);
    assertNull(result);
  }

  @Test
  @DisplayName("Deserializing empty string or empty bytes returns null")
  void testDeserializeEmpty() throws Exception {
    final var result = mapper.readValue("\"\"", ItemStack.class);
    assertNull(result);
  }
}
