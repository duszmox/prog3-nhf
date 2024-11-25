package model;

import java.util.Optional;

/**
 * A Stop osztály egy megállót reprezentál.
 */
public class Stop {
    /**
     * A megálló egyedi azonosítója. Kötelező.
     */
    private String stopId;

    /**
     * A megálló neve. Kötelező.
     */
    private String stopName;

    /**
     * A megálló szélességi koordinátája. Kötelező.
     */
    private double stopLat;

    /**
     * A megálló hosszúsági koordinátája. Kötelező.
     */
    private double stopLon;

    /**
     * A megálló kódja. Opcionális
     */
    private Optional<String> stopCode;

    /**
     * A helyszín típusa. Opcionális (0 = megálló, 1 = állomás, stb.).
     */
    private Optional<Integer> locationType;

    /**
     * A helyszín al-típusa. Opcionális
     */
    private Optional<String> locationSubType;

    /**
     * A szülő állomás azonosítója. Opcionális
     */
    private Optional<String> parentStation;

    /**
     * Információ a kerekesszékkel való megközelíthetőségről. Opcionális (0 = nincs információ, 1 = hozzáférhető, 2 = nem hozzáférhető).
     */
    private Optional<Integer> wheelchairBoarding;

    /**
     * Konstruktor, amely inicializálja a kötelező és opcionális mezőket.
     *
     * @param stopId               A megálló egyedi azonosítója.
     * @param stopName             A megálló neve.
     * @param stopLat              A megálló szélességi koordinátája.
     * @param stopLon              A megálló hosszúsági koordinátája.
     * @param stopCode             A megálló kódja.
     * @param locationType         A helyszín típusa.
     * @param locationSubType      A helyszín al-típusa.
     * @param parentStation        A szülő állomás azonosítója.
     * @param wheelchairBoarding   Információ a kerekesszékkel való megközelíthetőségről.
     */
    public Stop(String stopId, String stopName, double stopLat, double stopLon,
                Optional<String> stopCode, Optional<Integer> locationType,
                Optional<String> locationSubType, Optional<String> parentStation,
                Optional<Integer> wheelchairBoarding) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.stopLat = stopLat;
        this.stopLon = stopLon;
        this.stopCode = stopCode;
        this.locationType = locationType;
        this.locationSubType = locationSubType;
        this.parentStation = parentStation;
        this.wheelchairBoarding = wheelchairBoarding;
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
     * Visszaadja a megálló nevét.
     *
     * @return A megálló neve.
     */
    public String getStopName() {
        return stopName;
    }


    /**
     * Visszaadja a megálló szélességi koordinátáját.
     *
     * @return A megálló szélességi koordinátája.
     */
    public double getStopLat() {
        return stopLat;
    }


    /**
     * Visszaadja a megálló hosszúsági koordinátáját.
     *
     * @return A megálló hosszúsági koordinátája.
     */
    public double getStopLon() {
        return stopLon;
    }

    /**
     * Visszaadja a szülő állomás azonosítóját.
     *
     * @return A szülő állomás azonosítója, ha meg van adva.
     */
    public Optional<String> getParentStation() {
        return parentStation;
    }

    /**
     * Szöveges reprezentációja a megállónak.
     *
     * @return A megálló neve.
     */
    @Override
    public String toString() {
        return getStopName();
    }
}
