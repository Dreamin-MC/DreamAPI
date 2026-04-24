package fr.dreamin.dreamapi.api.worldborder.model;

import lombok.Getter;
import lombok.Setter;

import java.util.function.BiConsumer;

/**
 * This class represents the data of a world border, including its size, center position, damage buffer, warning time, and warning distance.
 */
@Setter
@Getter
public class WorldBorderData {

  private double size;
  private double x;
  private double z;
  private double damageBufferInBlocks;

  private int warningTimeSeconds;

  private int warningDistance;

  public void setCenter(double x, double z) {
    this.x = x;
    this.z = z;
  }

  public void applyCenter(BiConsumer<Double, Double> doubleBiConsumer) {
    doubleBiConsumer.accept(x, z);
  }

  public double getDamageBuffer() {
    return damageBufferInBlocks;
  }

  public void setDamageBuffer(double blocks) {
    this.damageBufferInBlocks = blocks;
  }

  public void applyAll(IWorldBorder worldBorder) {
    worldBorder.setSize(size);
    worldBorder.setCenter(new Position(x, z));
    worldBorder.setDamageBufferInBlocks(damageBufferInBlocks);
    worldBorder.setWarningTimeInSeconds(warningTimeSeconds);
    worldBorder.setWarningDistanceInBlocks(warningDistance);
  }
}