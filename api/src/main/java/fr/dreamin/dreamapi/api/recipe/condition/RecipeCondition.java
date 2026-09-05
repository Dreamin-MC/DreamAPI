package fr.dreamin.dreamapi.api.recipe.condition;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@FunctionalInterface
public interface RecipeCondition {

  @NotNull RecipeConditionResult test(
    final @NotNull RecipeConditionContext context
  );

  static RecipeCondition always() {
    return ctx -> RecipeConditionResult.allow();
  }


  final class Default {
    public static RecipeCondition permission(final @NotNull String perm) {
      return ctx -> ctx.player().hasPermission(perm) ? RecipeConditionResult.allow() : RecipeConditionResult.deny();
    }

    public static RecipeCondition minLevel(final int lvl) {
      return ctx -> (ctx.player().getLevel() >= lvl) ? RecipeConditionResult.allow() : RecipeConditionResult.deny();
    }

    public static RecipeCondition maxLevel(final int lvl) {
      return ctx -> (ctx.player().getLevel() <= lvl) ? RecipeConditionResult.allow() : RecipeConditionResult.deny();
    }

    public static RecipeCondition inWorld(final @NotNull String worldName) {
      return ctx -> (ctx.player().getWorld().getName().equalsIgnoreCase(worldName)) ? RecipeConditionResult.allow() : RecipeConditionResult.deny();
    }

    public static RecipeCondition biome(final @NotNull Biome biome) {
      return ctx -> (ctx.player().getLocation().getBlock().getBiome() == biome) ? RecipeConditionResult.allow() : RecipeConditionResult.deny();
    }

    public static RecipeCondition holding(final @NotNull Material mat) {
      return ctx -> (ctx.player().getInventory().getItemInMainHand().getType() == mat) ? RecipeConditionResult.allow() : RecipeConditionResult.deny();
    }

    public static RecipeCondition holding(final @NotNull ItemStack item) {
      return ctx -> (ctx.player().getInventory().getItemInMainHand().isSimilar(item)) ? RecipeConditionResult.allow() : RecipeConditionResult.deny();
    }

    public static RecipeCondition wearing(final @NotNull Material mat) {
      return ctx -> (ctx.player().getInventory().getHelmet().getType() == mat) ? RecipeConditionResult.allow() : RecipeConditionResult.deny();
    }

    public static RecipeCondition wearing(final @NotNull ItemStack item) {
      return ctx -> (ctx.player().getInventory().getHelmet().isSimilar(item)) ? RecipeConditionResult.allow() : RecipeConditionResult.deny();
    }

    public static RecipeCondition prediacte(final @NotNull Predicate<Player> predicate) {
      return ctx -> (predicate.test(ctx.player())) ? RecipeConditionResult.allow() : RecipeConditionResult.deny();
    }

  }

}
