package fr.socolin.applicationinsights.utils;

import org.jetbrains.annotations.NotNull;

public class TimeSpan implements Comparable<TimeSpan> {
    final int hours;
    final int minutes;
    final int seconds;
    final int milliseconds;
    final long total;

    public TimeSpan(int hours, int minutes, int seconds, int milliseconds) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = milliseconds;
        total = hours * 3_600_000L + minutes * 60_000L + seconds * 1_000L + milliseconds;
    }

    public TimeSpan(String value) {
        String[] split = value.split("\\.", 2);
        String[] hourMinuteSeconds = split[0].split(":", 3);
        hours = Integer.parseInt(hourMinuteSeconds[0]);
        minutes = Integer.parseInt(hourMinuteSeconds[1]);
        seconds = Integer.parseInt(hourMinuteSeconds[2]);
        milliseconds = Integer.parseInt(split[1].substring(0, 3));
        total = hours * 3_600_000L + minutes * 60_000L + seconds * 1_000L + milliseconds;
    }

    @Override
    public String toString() {
        if (hours == 0) {
            if (minutes == 0) {
                if (seconds == 0) {
                    if (milliseconds == 0)
                        return "";
                    return milliseconds + "ms";
                }
                return seconds + "." + milliseconds + "s";
            }
            return minutes + "m " + seconds + "." + milliseconds + "s";
        }
        return hours + "h " + minutes + "m " + seconds + "." + milliseconds + "s";
    }


    @Override
    public int compareTo(@NotNull TimeSpan timespan) {
        return (int) (timespan.total - total);
    }

    public static TimeSpan Zero = new TimeSpan(0, 0, 0, 0);
}
