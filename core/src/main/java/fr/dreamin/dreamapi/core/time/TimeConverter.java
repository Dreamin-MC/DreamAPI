package fr.dreamin.dreamapi.core.time;

import java.util.Date;

public final class TimeConverter {

  public static int getTimeSeconds(final long start, final int cooldown) {
    final var seconds = getTimeSeconds(start);
    return cooldown - seconds;
  }

  public static long getTime(final long time1, final long time2) {
    return java.lang.Math.abs(time1 - time2);
  }
  public static String getTimeSecMin(final long start, final int cooldown) {
    final var timeBetween = getTime(start, new Date().getTime());
    var minutes = (int) (timeBetween / 1000 / 60);
    var seconds = (int) (timeBetween / 1000) % 60;

    if (cooldown - seconds > 60) {
      minutes = ((cooldown - seconds) / 60) - minutes;
      seconds = (cooldown - seconds) % 60;

      if (seconds < 10) return minutes + " minute"+ ((minutes > 1) ? "s" : "") + " et 0" + seconds + " seconde" + ((seconds > 1) ? "s" : "");
      else return minutes + " minute"+ (minutes > 1 ? "s" : "") + " et " + seconds + " seconde" + "s";
    } else {
      seconds = (cooldown - seconds);
      return seconds + " seconde" + ((seconds > 1) ? "s" : "");
    }
  }

  public static String getTimeSecMin(int seconds) {
    var secondsVar = seconds;

    if (secondsVar > 60) {
      var minutes = secondsVar / 60;
      secondsVar = secondsVar % 60;
      if (secondsVar < 10) return minutes + ":0" + secondsVar + "min";
      else return minutes + ":" + secondsVar + "min";
    }
    else return secondsVar + "sec";
  }

  public static String getTimeSecMinHour(final int ticks) {
    var seconds = getTicksToSeconds(ticks);
    if (seconds > 60) {
      var minutes = seconds / 60;
      seconds = seconds % 60;
      if (minutes > 60) {
        var hours = minutes / 60;
        minutes = minutes % 60;

        return hours + "h" + (minutes > 9 ? minutes + "m" :  "0" + minutes + "m") + (seconds > 9 ? seconds + "s" :  "0" + seconds + "s");
      }
      else return minutes + "m" + (seconds > 9 ? seconds + "s" :  "0" + seconds + "s");
    }
    else return seconds + "s";
  }

  public static String getTimeFormatted(final int ticks) {
    return getTimeFormatted((long) ticks);
  }

  public static String getTimeFormatted(final long ticks) {
    final var totalSeconds = getTicksToSeconds(ticks);
    final var hours = totalSeconds / 3600;
    final var minutes = (totalSeconds % 3600) / 60;
    final var seconds = totalSeconds % 60;

    if (hours > 0) return String.format("%d:%02d:%02d", hours, minutes, seconds);
    else if (minutes > 0) return String.format("%d:%02d", minutes, seconds);
    else return String.format("%d", seconds);
  }

  public static int getTimeSeconds(final long start) {
    final var timeBetween = getTime(start, new Date().getTime());
    return (int) (timeBetween / 1000);
  }

  public static int getTicksToSeconds(final long ticks) {
    return (int) Math.round((double) ticks / 20);
  }

  public static int getSecondsToTicks(final long seconds) {
    return (int) Math.round((double) seconds * 20);
  }
}
