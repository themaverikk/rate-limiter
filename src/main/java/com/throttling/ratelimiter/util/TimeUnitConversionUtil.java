package com.throttling.ratelimiter.util;

import com.throttling.ratelimiter.enums.TimeUnit;

public class TimeUnitConversionUtil {

  public static double convert(long milliseconds, TimeUnit timeUnit) {

    if (timeUnit == null) {
      throw new IllegalArgumentException("Invalid timeUnit supplied for conversion");
    }

    switch (timeUnit) {
      case SEC:
        return milliseconds / 1000.0;

      case MIN:
        return milliseconds / (60 * 1000.0);

      case HOUR:
        return milliseconds / (3600 * 1000.0);

      case WEEK:
        return milliseconds / (7 * 24 * 3600 * 1000.0);

      case MONTH:
        return milliseconds / (30 * 7 * 24 * 3600 * 1000.0);

      default:
        return -1;
    }

  }
}
