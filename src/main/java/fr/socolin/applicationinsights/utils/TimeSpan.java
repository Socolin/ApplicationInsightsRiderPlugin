package fr.socolin.applicationinsights.utils;

public class TimeSpan {
    final int hours;
    final int minutes;
    final int seconds;
    final int nanoseconds;

    public TimeSpan(String value) {
        String[] split = value.split("\\.", 2);
        String[] hourMinuteSeconds = split[0].split(":", 3);
        hours = Integer.parseInt(hourMinuteSeconds[0]);
        minutes = Integer.parseInt(hourMinuteSeconds[1]);
        seconds = Integer.parseInt(hourMinuteSeconds[2]);
        nanoseconds = Integer.parseInt(split[1].substring(0, 3));
    }

    @Override
    public String toString() {
        if (hours == 0) {
            if (minutes == 0) {
                if (seconds == 0) {
                    return nanoseconds + "ms";
                }
                return seconds + "." + nanoseconds + "s";
            }
            return minutes + "m " + seconds + "." + nanoseconds + "s";
        }
        return hours + "h " + minutes + "m " + seconds + "." + nanoseconds + "s";
    }
}
