import model.Stop;

import java.time.LocalTime;

/**
 * A TripPlanLeg osztály egy utazási szakaszt reprezentál az utazási tervben.
 */
public class TripPlanLeg {
    private final LegType legType;    // Szakasz típusa: TRANSIT, WALK, TRANSFER
    private Stop fromStop;            // Kiinduló megálló vagy helyszín
    private Stop toStop;              // Cél megálló vagy helyszín
    private LocalTime startTime;      // Szakasz indulási ideje
    private LocalTime endTime;        // Szakasz érkezési ideje
    private String tripId;            // Járat azonosítója menetrendi szakaszokhoz
    private String routeId;           // Vonal azonosítója menetrendi szakaszokhoz
    private String routeShortName;    // Vonal rövid neve
    private String routeLongName;     // Vonal hosszú neve
    private double distance;          // Távolság méterben esetén
    private long duration;            // Szakasz időtartama másodpercekben (átszállásokhoz)
    private long waitTime;

    /**
     * Konstruktor menetrendi és gyaloglási szakaszokhoz.
     *
     * @param legType        A szakasz típusa.
     * @param fromStop       Kiinduló megálló.
     * @param toStop         Cél megálló.
     * @param startTime      Indulási idő.
     * @param endTime        Érkezési idő.
     * @param tripId         Járat azonosítója.
     * @param routeId        Vonal azonosítója.
     * @param routeShortName Vonal rövid neve.
     * @param routeLongName  Vonal hosszú neve.
     * @param distance       Távolság méterben.
     * @param duration       Időtartam másodpercekben.
     */
    public TripPlanLeg(LegType legType, Stop fromStop, Stop toStop, LocalTime startTime, LocalTime endTime,
                       String tripId, String routeId, String routeShortName, String routeLongName, double distance, long duration) {
        this.legType = legType;
        this.fromStop = fromStop;
        this.toStop = toStop;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tripId = tripId;
        this.routeId = routeId;
        this.routeShortName = routeShortName;
        this.routeLongName = routeLongName;
        this.distance = distance;
        this.duration = duration;
    }

    /**
     * Konstruktor átszállási szakaszokhoz.
     *
     * @param legType     A szakasz típusa.
     * @param transferStop Átszállási megálló.
     * @param startTime   Kezdési idő.
     * @param endTime     Befejezési idő.
     * @param duration    Időtartam másodpercekben.
     */
    public TripPlanLeg(LegType legType, Stop transferStop, LocalTime startTime, LocalTime endTime, long duration) {
        this.legType = legType;
        this.fromStop = transferStop;
        this.toStop = transferStop;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }

    /**
     * Másoló konstruktor egy másik szakaszból.
     *
     * @param otherLeg Egy másik szakasz.
     */
    public TripPlanLeg(TripPlanLeg otherLeg) {
        this.legType = otherLeg.getLegType();
        this.fromStop = otherLeg.getFromStop();
        this.toStop = otherLeg.getToStop();
        this.startTime = otherLeg.getStartTime();
        this.endTime = otherLeg.getEndTime();
        this.tripId = otherLeg.getTripId();
        this.routeId = otherLeg.getRouteId();
        this.routeShortName = otherLeg.getRouteShortName();
        this.routeLongName = otherLeg.getRouteLongName();
        this.distance = otherLeg.getDistance();
        this.duration = otherLeg.getDuration();
        this.waitTime = otherLeg.getWaitTime();
    }

    /**
     * Visszaadja a kiinduló megállót.
     *
     * @return A kiinduló megálló.
     */
    public Stop getFromStop() {
        return fromStop;
    }

    /**
     * Visszaadja a cél megállót.
     *
     * @return A cél megálló.
     */
    public Stop getToStop() {
        return toStop;
    }

    /**
     * Beállítja a cél megállót.
     *
     * @param toStop A cél megálló.
     */
    public void setToStop(Stop toStop) {
        this.toStop = toStop;
    }

    /**
     * Visszaadja a szakasz időtartamát másodpercekben.
     *
     * @return A szakasz időtartama másodpercekben.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Beállítja a szakasz időtartamát másodpercekben.
     *
     * @param duration A szakasz időtartama másodpercekben.
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * Visszaadja a szakasz indulási idejét.
     *
     * @return A szakasz indulási ideje.
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Visszaadja a szakasz érkezési idejét.
     *
     * @return A szakasz érkezési ideje.
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Beállítja a szakasz érkezési idejét.
     *
     * @param endTime A szakasz érkezési ideje.
     */
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Visszaadja a járat azonosítóját.
     *
     * @return A járat azonosítója.
     */
    public String getTripId() {
        return tripId;
    }

    /**
     * Visszaadja a vonal azonosítóját.
     *
     * @return A vonal azonosítója.
     */
    public String getRouteId() {
        return routeId;
    }

    /**
     * Visszaadja a távolságot méterben.
     *
     * @return A távolság méterben.
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Beállítja a távolságot méterben.
     *
     * @param distance A távolság méterben.
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * Visszaadja a vonal rövid nevét.
     *
     * @return A vonal rövid neve.
     */
    public String getRouteShortName() {
        return routeShortName;
    }

    /**
     * Visszaadja a vonal hosszú nevét.
     *
     * @return A vonal hosszú neve.
     */
    public String getRouteLongName() {
        return routeLongName;
    }

    /**
     * Visszaadja a szakasz típusát.
     *
     * @return A szakasz típusa.
     */
    public LegType getLegType() {
        return legType;
    }

    /**
     * Visszaadja a várakozási időt másodpercekben.
     *
     * @return A várakozási idő másodpercekben.
     */
    public long getWaitTime() {
        return waitTime;
    }

    /**
     * A szakasz lehetséges típusai.
     */
    public enum LegType {
        TRANSIT,
        WALK,
        TRANSFER,
        WAIT
    }
}
