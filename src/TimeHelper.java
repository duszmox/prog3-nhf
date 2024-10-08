import java.time.LocalTime;

public class TimeHelper {

    // Nested class to return both the time and if it's the next day
    public static class TimeResult {
        public final LocalTime time;
        public final boolean isNextDay;

        public TimeResult(LocalTime time, boolean isNextDay) {
            this.time = time;
            this.isNextDay = isNextDay;
        }
    }

    // Method to handle times like "24:28:00"
    public static TimeResult parseGtfsTime(String timeStr) {
        String[] parts = timeStr.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        boolean isNextDay = hours >= 24;

        if (isNextDay) {
            hours = hours - 24;  // Normalize to valid LocalTime (subtract 24)
        }

        return new TimeResult(LocalTime.of(hours, minutes, seconds), isNextDay);
    }
}