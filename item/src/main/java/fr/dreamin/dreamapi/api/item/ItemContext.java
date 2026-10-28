package fr.dreamin.dreamapi.api.item;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ItemContext(
  @NotNull Player player,
  @NotNull ItemStack item,
  @NotNull Event event
) { }
