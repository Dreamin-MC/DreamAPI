package fr.dreamin.dreamapi.api.navigate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter @Setter
public final class Node {
  private final @NotNull Location location;
  private @Nullable Node parent;
  private double gCost, hCost, fCost;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  public Node(
    final @NotNull Location location,
    final @Nullable Node node,
    final double gCost,
    final double hCost
  ) {
    this.location = location;
    this.parent = node;
    this.gCost = gCost;
    this.hCost = hCost;
    this.fCost = gCost + hCost;
  }

}
