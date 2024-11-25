package model;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

/**
 * A Trip osztály egy vonalon belüli jármű mozgását reprezentálja.
 */
public class Trip {
    /**
     * A járat vonalának egyedi azonosítója. Kötelező.
     */
    private String routeId;

    /**
     * A járat egyedi azonosítója. Kötelező.
     */
    private String tripId;

    /**
     * A szolgáltatás egyedi azonosítója. Kötelező.
     */
    private String serviceId;

    /**
     * A szolgáltatási napok listája. Kötelező a szolgáltatási napokhoz.
     */
    private List<LocalDate> serviceDates;

    /**
     * A járat feljéce. Opcionális
     */
    private Optional<String> tripHeadsign;

    /**
     * Az irány azonosítója. Opcionális (0 = outbound, 1 = inbound).
     */
    private Optional<Integer> directionId;

    /**
     * A blokk azonosítója. Opcionális
     */
    private Optional<String> blockId;

    /**
     * A shape egyedi azonosítója. Opcionális
     */
    private Optional<String> shapeId;

    /**
     * Információ a kerekesszékkel való hozzáférhetőségről. Opcionális (0 = nincs információ, 1 = hozzáférhető, 2 = nem hozzáférhető).
     */
    private Optional<Integer> wheelchairAccessible;

    /**
     * Információ a kerékpárok szállíthatóságáról. Opcionális (0 = nincs információ, 1 = engedélyezett, 2 = nem engedélyezett).
     */
    private Optional<Integer> bikesAllowed;

    /**
     * Konstruktor, amely inicializálja minden mezőt, beleértve az opcionálisakat.
     *
     * @param routeId                A járat vonalának egyedi azonosítója.
     * @param tripId                 A járat egyedi azonosítója.
     * @param serviceId              A szolgáltatás egyedi azonosítója.
     * @param tripHeadsign           A járat fejléce.
     * @param directionId            Az irány azonosítója.
     * @param blockId                A blokk azonosítója.
     * @param shapeId                A shape egyedi azonosítója.
     * @param wheelchairAccessible   Információ a kerekesszékkel való hozzáférhetőségről.
     * @param bikesAllowed           Információ a kerékpárok engedélyezéséről.
     */
    public Trip(String routeId, String tripId, String serviceId,
                Optional<String> tripHeadsign, Optional<Integer> directionId,
                Optional<String> blockId, Optional<String> shapeId,
                Optional<Integer> wheelchairAccessible, Optional<Integer> bikesAllowed) {
        this.routeId = routeId;
        this.tripId = tripId;
        this.serviceId = serviceId;
        this.tripHeadsign = tripHeadsign;
        this.directionId = directionId;
        this.blockId = blockId;
        this.shapeId = shapeId;
        this.wheelchairAccessible = wheelchairAccessible;
        this.bikesAllowed = bikesAllowed;
    }

    /**
     * Visszaadja a szolgáltatási napok listáját.
     *
     * @return A szolgáltatási napok listája.
     */
    public List<LocalDate> getServiceDates() {
        return serviceDates;
    }

    /**
     * Beállítja a szolgáltatási napok listáját.
     *
     * @param serviceDates Az új szolgáltatási napok listája.
     */
    public void setServiceDates(List<LocalDate> serviceDates) {
        this.serviceDates = serviceDates;
    }

    /**
     * Visszaadja a járat vonalának egyedi azonosítóját.
     *
     * @return A vonal egyedi azonosítója.
     */
    public String getRouteId() {
        return routeId;
    }

    /**
     * Visszaadja a járat egyedi azonosítóját.
     *
     * @return A járat egyedi azonosítója.
     */
    public String getTripId() {
        return tripId;
    }


    /**
     * Visszaadja a szolgáltatás egyedi azonosítóját.
     *
     * @return A szolgáltatás egyedi azonosítója.
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Visszaadja a járat fejlécét.
     *
     * @return A járat fejléce, ha meg van adva.
     */
    public Optional<String> getTripHeadsign() {
        return tripHeadsign;
    }
}
