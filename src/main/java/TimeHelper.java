import java.time.LocalTime;

/**
 * A TimeHelper osztály segédfüggvényeket biztosít az idő kezelése során.
 */
public class TimeHelper {

    /**
     * Belső osztály az idő és a napváltás jelzésére.
     */
    public record TimeResult(LocalTime time, boolean isNextDay) {
    }

    /**
     * Metódus a GTFS időformátum (pl. "13:28:00") kezelésére.
     *
     * @param timeStr Az idő sztring formátumban.
     * @return A TimeResult objektum, amely tartalmazza a LocalTime objektumot és egy jelzést, ha a következő napra esik.
     */
    public static TimeResult parseGtfsTime(String timeStr) {
        String[] parts = timeStr.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        boolean isNextDay = hours >= 24;

        if (isNextDay) {
            hours = hours - 24;  // Normalizálás érvényes LocalTime értékre
        }

        return new TimeResult(LocalTime.of(hours, minutes, seconds), isNextDay);
    }
}
