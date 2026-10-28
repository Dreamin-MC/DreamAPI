package fr.dreamin.dreamapi.core.recipe.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;

import fr.dreamin.dreamapi.api.recipe.IngredientDefinition;
import fr.dreamin.dreamapi.api.recipe.IngredientPredicate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.io.IOException;
import java.util.HashMap;

public final class IngredientDefinitionModule extends SimpleModule {

  public IngredientDefinitionModule() {
    super("IngredientDefinitionModule");


    addSerializer(IngredientDefinition.class, new JsonSerializer<>() {

      @Override
      public void serialize(IngredientDefinition def, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        final var out = new HashMap<>();

        out.put("display", def.display());

        if (def.choice() instanceof RecipeChoice.MaterialChoice mat)
          out.put("material", mat.getChoices().iterator().next().name());
        else if (def.choice() instanceof RecipeChoice.ExactChoice ex)
          out.put("item", ex.getChoices().get(0));

        gen.writeObject(out);
      }
    });

    addDeserializer(IngredientDefinition.class, new JsonDeserializer<>() {
      @Override
      public IngredientDefinition deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        final var n = (JsonNode) p.getCodec().readTree(p);

        final var display = p.getCodec().treeToValue(n.get("display"), ItemStack.class);

        if (n.has("material")) {
          final var mat = Material.valueOf(n.get("material").asText());
          return new IngredientDefinition(
            IngredientPredicate.ofMaterial(mat),
            new RecipeChoice.MaterialChoice(mat),
            display
          );
        }

        if (n.has("item")) {
          final var item = p.getCodec().treeToValue(n.get("item"), ItemStack.class);
          return new IngredientDefinition(
            IngredientPredicate.ofExactItem(item),
            new RecipeChoice.ExactChoice(item),
            display
          );
        }

        throw new IOException("Invalid IngredientDefinition JSON");
      }
    });

  }

}
