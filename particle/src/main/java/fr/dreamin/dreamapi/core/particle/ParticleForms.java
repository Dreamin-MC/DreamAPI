package fr.dreamin.dreamapi.core.particle;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ParticleForms {

  // ###############################################################
  // ----------------------- 3D SHAPES ------------------------------
  // ###############################################################

  /** Sphère pleine (points répartis dans un volume sphérique). */
  public static ParticleShape sphere(final double radius, final int density) {
    return new ParticleShape() {

      // ###############################################################
      // -------------------------- METHODS ----------------------------
      // ###############################################################

      @Override
      public void render(final @NotNull Particle particle, final @NotNull Location base) {
        spawnShape(particle, base, ParticleShape.ParticleOptions.defaultOptions());
      }

      @Override
      public void render(final @NotNull Location base, final @NotNull ParticleShape.ParticleOptions opts) {
        spawnShape(Particle.DUST, base, opts);
      }

      @Override
      public @NotNull List<Vector> sample() {
        final var points = new ArrayList<Vector>(density);
        for (int i = 0; i < density; i++) {
          final var phi = Math.random() * Math.PI * 2;
          final var costheta = Math.random() * 2 - 1;
          final var u = Math.random();
          final var r = radius * Math.cbrt(u);
          final var theta = Math.acos(costheta);
          final var x = r * Math.sin(theta) * Math.cos(phi);
          final var y = r * Math.sin(theta) * Math.sin(phi);
          final var z = r * Math.cos(theta);
          points.add(new Vector(x, y, z));
        }
        return points;
      }

      // ###############################################################
      // ----------------------- PRIVATE METHODS -----------------------
      // ###############################################################

      private void spawnShape(final @NotNull Particle particle, final @NotNull Location base, final @NotNull ParticleShape.ParticleOptions opts) {
        for (int i = 0; i < density; i++) {
          final var phi = Math.random() * Math.PI * 2;
          final var costheta = Math.random() * 2 - 1;
          final var u = Math.random();
          final var r = radius * Math.cbrt(u);
          final var theta = Math.acos(costheta);
          final var x = r * Math.sin(theta) * Math.cos(phi);
          final var y = r * Math.sin(theta) * Math.sin(phi);
          final var z = r * Math.cos(theta);
          spawn(particle, base.clone().add(x, y, z), opts);
        }
      }

    };
  }

  /** Sphère vide (points à la surface). */
  public static ParticleShape hollowSphere(final double radius, final int points) {
    return new ParticleShape() {

      // ###############################################################
      // -------------------------- METHODS ----------------------------
      // ###############################################################

      @Override
      public void render(final @NotNull Particle particle, final @NotNull Location base) {
        spawnShape(particle, base, ParticleShape.ParticleOptions.defaultOptions());
      }

      @Override
      public void render(final @NotNull Location base, final @NotNull ParticleShape.ParticleOptions opts) {
        spawnShape(Particle.DUST, base, opts);
      }

      @Override
      public @NotNull List<Vector> sample() {
        final var list = new ArrayList<Vector>(points);
        for (int i = 0; i < points; i++) {
          final var theta = Math.acos(2 * Math.random() - 1);
          final var phi = Math.random() * 2 * Math.PI;
          final var x = radius * Math.sin(theta) * Math.cos(phi);
          final var y = radius * Math.sin(theta) * Math.sin(phi);
          final var z = radius * Math.cos(theta);
          list.add(new Vector(x, y, z));
        }
        return list;
      }

      // ###############################################################
      // ----------------------- PRIVATE METHODS -----------------------
      // ###############################################################

      private void spawnShape(final @NotNull Particle particle, final @NotNull Location base, final @NotNull ParticleShape.ParticleOptions opts) {
        for (var i = 0; i < points; i++) {
          final var theta = Math.acos(2 * Math.random() - 1);
          final var phi = Math.random() * 2 * Math.PI;
          final var x = radius * Math.sin(theta) * Math.cos(phi);
          final var y = radius * Math.sin(theta) * Math.sin(phi);
          final var z = radius * Math.cos(theta);
          spawn(particle, base.clone().add(x, y, z), opts);
        }
      }

    };
  }

  /** Cube (points sur les arêtes et faces). */
  public static ParticleShape cube(final double size, final int step) {
    return new ParticleShape() {
      @Override
      public void render(final @NotNull Particle particle, final @NotNull Location base) {
        spawnShape(particle, base, ParticleShape.ParticleOptions.defaultOptions());
      }

      @Override
      public void render(final @NotNull Location base, final @NotNull ParticleShape.ParticleOptions opts) {
        spawnShape(Particle.DUST, base, opts);
      }

      @Override
      public @NotNull List<Vector> sample() {
        final var list = new ArrayList<Vector>();
        final var half = size / 2;
        for (double x = -half; x <= half; x += step) {
          for (double y = -half; y <= half; y += step) {
            for (double z = -half; z <= half; z += step) {
              var faces = 0;
              if (Math.abs(x - half) < 0.01 || Math.abs(x + half) < 0.01) faces++;
              if (Math.abs(y - half) < 0.01 || Math.abs(y + half) < 0.01) faces++;
              if (Math.abs(z - half) < 0.01 || Math.abs(z + half) < 0.01) faces++;
              if (faces >= 2) list.add(new Vector(x, y, z));
            }
          }
        }
        return list;
      }

      // ###############################################################
      // ----------------------- PRIVATE METHODS -----------------------
      // ###############################################################

      private void spawnShape(final @NotNull Particle particle, final @NotNull Location base, final @NotNull ParticleShape.ParticleOptions opts) {
        final var half = size / 2;
        for (double x = -half; x <= half; x += step) {
          for (double y = -half; y <= half; y += step) {
            for (double z = -half; z <= half; z += step) {
              var faces = 0;
              if (Math.abs(x - half) < 0.01 || Math.abs(x + half) < 0.01) faces++;
              if (Math.abs(y - half) < 0.01 || Math.abs(y + half) < 0.01) faces++;
              if (Math.abs(z - half) < 0.01 || Math.abs(z + half) < 0.01) faces++;
              if (faces >= 2) spawn(particle, base.clone().add(x, y, z), opts);
            }
          }
        }
      }

    };
  }

  /** Donut (anneau 3D façon tore). */
  public static ParticleShape ring3D(final double radius, final double thickness, final int points) {
    return new ParticleShape() {

      // ###############################################################
      // -------------------------- METHODS ----------------------------
      // ###############################################################

      @Override
      public void render(final @NotNull Particle particle, final @NotNull Location base) {
       spawnShape(particle, base, ParticleShape.ParticleOptions.defaultOptions());
      }

      @Override
      public void render(final @NotNull Location base, final @NotNull ParticleShape.ParticleOptions opts) {
        spawnShape(Particle.DUST, base, opts);
      }

      @Override
      public @NotNull List<Vector> sample() {
        final var list = new ArrayList<Vector>();
        for (int i = 0; i < points; i++) {
          final var theta = 2 * Math.PI * i / points;
          for (int j = 0; j < points / 4; j++) {
            final var phi = 2 * Math.PI * j / ((double) points / 4);
            final var x = (radius + thickness * Math.cos(phi)) * Math.cos(theta);
            final var y = thickness * Math.sin(phi);
            final var z = (radius + thickness * Math.cos(phi)) * Math.sin(theta);
            list.add(new Vector(x, y, z));
          }
        }
        return list;
      }

      // ###############################################################
      // ----------------------- PRIVATE METHODS -----------------------
      // ###############################################################

      private void spawnShape(final @NotNull Particle particle, final @NotNull Location base, final @NotNull ParticleShape.ParticleOptions opts) {
        for (var i = 0; i < points; i++) {
          final var theta = 2 * Math.PI * i / points;
          for (var j = 0; j < points / 4; j++) {
            final var phi = 2 * Math.PI * j / ((double) points / 4);
            final var x = (radius + thickness * Math.cos(phi)) * Math.cos(theta);
            final var y = thickness * Math.sin(phi);
            final var z = (radius + thickness * Math.cos(phi)) * Math.sin(theta);
            spawn(particle, base.clone().add(x, y, z), opts);
          }
        }
      }

    };
  }

  /** Cœur stylisé (symbole ❤️). */
  public static ParticleShape heart(final double scale, final int points) {
    return new ParticleShape() {

      // ###############################################################
      // -------------------------- METHODS ----------------------------
      // ###############################################################

      @Override
      public void render(final @NotNull Particle particle, final @NotNull Location base) {
        spawnShape(particle, base, ParticleShape.ParticleOptions.defaultOptions());
      }

      @Override
      public void render(final @NotNull Location base, final @NotNull ParticleShape.ParticleOptions opts) {
        spawnShape(Particle.DUST, base, opts);
      }

      @Override
      public @NotNull List<Vector> sample() {
        final var list = new ArrayList<Vector>(points);
        for (int i = 0; i < points; i++) {
          final var t = Math.PI - (2 * Math.PI * i / points);
          final var x = 16 * Math.pow(Math.sin(t), 3);
          double y = 13 * Math.cos(t) - 5 * Math.cos(2 * t)
            - 2 * Math.cos(3 * t) - Math.cos(4 * t);
          list.add(new Vector(x * scale * 0.05, y * scale * 0.05, 0));
        }
        return list;
      }

      // ###############################################################
      // ----------------------- PRIVATE METHODS -----------------------
      // ###############################################################

      private void spawnShape(final @NotNull Particle particle, final @NotNull Location base, final @NotNull ParticleShape.ParticleOptions opts) {
        for (int i = 0; i < points; i++) {
          final var t = Math.PI - (2 * Math.PI * i / points);
          final var x = 16 * Math.pow(Math.sin(t), 3);
          double y = 13 * Math.cos(t) - 5 * Math.cos(2 * t)
            - 2 * Math.cos(3 * t) - Math.cos(4 * t);
          spawn(particle, base.clone().add(x * scale * 0.05, y * scale * 0.05, 0), opts);
        }
      }

    };
  }

  /** Tornade (spirale verticale). */
  public static ParticleShape tornado(final double height, final double radius, final int turns, final int pointsPerTurn) {
    return new ParticleShape() {

      // ###############################################################
      // -------------------------- METHODS ----------------------------
      // ###############################################################

      @Override
      public void render(final @NotNull Particle particle, final @NotNull Location base) {
        spawnShape(particle, base, ParticleShape.ParticleOptions.defaultOptions());
      }

      @Override
      public void render(final @NotNull Location base, final @NotNull ParticleShape.ParticleOptions opts) {
        spawnShape(Particle.DUST, base, opts);
      }

      @Override
      public @NotNull List<Vector> sample() {
        final var list = new ArrayList<Vector>(turns * pointsPerTurn);
        final var total = turns * pointsPerTurn;
        for (int i = 0; i < total; i++) {
          final var t = (double) i / total;
          final var angle = t * turns * 2 * Math.PI;
          final var x = Math.cos(angle) * radius * t;
          final var y = height * t;
          final var z = Math.sin(angle) * radius * t;
          list.add(new Vector(x, y, z));
        }
        return list;
      }

      // ###############################################################
      // ----------------------- PRIVATE METHODS -----------------------
      // ###############################################################

      private void spawnShape(final @NotNull Particle particle, final @NotNull Location base, final @NotNull ParticleShape.ParticleOptions opts) {
        final var total = turns * pointsPerTurn;
        for (var i = 0; i < total; i++) {
          final var t = (double) i / total;
          final var angle = t * turns * 2 * Math.PI;
          final var x = Math.cos(angle) * radius * t;
          final var y = height * t;
          final var z = Math.sin(angle) * radius * t;
          spawn(particle, base.clone().add(x, y, z), opts);
        }
      }

    };
  }

  // ###############################################################
  // ---------------------- UTILITY METHOD --------------------------
  // ###############################################################

  private static void spawn(final @NotNull Particle particle, final @NotNull Location loc, final @NotNull ParticleShape.ParticleOptions opts) {
    if (particle == Particle.DUST) {
      if (opts.color() != null) {
        final var dust = new Particle.DustOptions(opts.color(), opts.size());
        loc.getWorld().spawnParticle(particle, loc, opts.count(), opts.offset().getX(), opts.offset().getY(), opts.offset().getZ(), 0, dust);
      }
      else {
        final var dust = new Particle.DustOptions(Color.WHITE, 1.5f);
        loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0, dust);
      }
    } else
      loc.getWorld().spawnParticle(particle, loc, opts.count(), opts.offset().getX(), opts.offset().getY(), opts.offset().getZ(), 0);
  }
}