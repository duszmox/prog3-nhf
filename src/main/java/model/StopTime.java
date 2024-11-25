package model;

import java.time.LocalTime;
import java.util.Optional;

/**
 * A StopTime osztály egy megálló idejét és sorrendjét reprezentálja egy utazás során.
 */
public class StopTime {
    /**
     * Az utazás egyedi azonosítója. Kötelező.
     */
    private String tripId;

    /**
     * A megálló egyedi azonosítója. Kötelező.
     */
    private String stopId;

    /**
     * Az érkezési idő. Opcionális (HH:MM:SS formátum).
     */
    private Optional<LocalTime> arrivalTime;

    /**
     * A távozási idő. Opcionális (HH:MM:SS formátum).
     */
    private Optional<LocalTime> departureTime;

    /**
     * A megálló sorrendje az utazásban. Kötelező.
     */
    private int stopSequence;

    /**
     * A megálló fejléce. Opcionális
     */
    private Optional<String> stopHeadsign;

    /**
     * A felvételi típus. Opcionális (0 = normál, 1 = nincs felvétel, stb.).
     */
    private Optional<Integer> pickupType;

    /**
     * A leszállítási típus. Opcionális (0 = normál, 1 = nincs leszállítás, stb.).
     */
    private Optional<Integer> dropOffType;

    /**
     * A megtett távolság a vonal mentén. Opcionális
     */
    private Optional<Double> shapeDistTraveled;

    /**
     * Konstruktor, amely minden mezőt inicializál, beleértve az opcionálisakat.
     *
     * @param tripId             Az utazás egyedi azonosítója.
     * @param stopId             A megálló egyedi azonosítója.
     * @param arrivalTime        Az érkezési idő.
     * @param departureTime      A távozási idő.
     * @param stopSequence       A megálló sorrendje az utazásban.
     * @param stopHeadsign       A megálló fejléce.
     * @param pickupType         A felvételi típus.
     * @param dropOffType        A leszállítási típus.
     * @param shapeDistTraveled  A megtett távolság a vonal mentén.
     */
    public StopTime(String tripId, String stopId, Optional<LocalTime> arrivalTime, Optional<LocalTime> departureTime,
                    int stopSequence, Optional<String> stopHeadsign, Optional<Integer> pickupType,
                    Optional<Integer> dropOffType, Optional<Double> shapeDistTraveled) {
        this.tripId = tripId;
        this.stopId = stopId;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.stopSequence = stopSequence;
        this.stopHeadsign = stopHeadsign;
        this.pickupType = pickupType;
        this.dropOffType = dropOffType;
        this.shapeDistTraveled = shapeDistTraveled;
    }

    /**
     * Visszaadja az utazás azonosítóját.
     *
     * @return Az utazás egyedi azonosítója.
     */
    public String getTripId() {
        return tripId;
    }


    /**
     * Visszaadja a megálló azonosítóját.
     *
     * @return A megálló egyedi azonosítója.
     */
    public String getStopId() {
        return stopId;
    }

    /**
     * Visszaadja az érkezési időt.
     *
     * @return Az érkezési idő, ha meg van adva.
     */
    public Optional<LocalTime> getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Visszaadja a távozási időt.
     *
     * @return A távozási idő, ha meg van adva.
     */
    public Optional<LocalTime> getDepartureTime() {
        return departureTime;
    }

    /**
     * Visszaadja a megálló sorrendjét az utazásban.
     *
     * @return A megálló sorrendje.
     */
    public int getStopSequence() {
        return stopSequence;
    }
}
