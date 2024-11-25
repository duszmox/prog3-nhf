package model;

import java.awt.*;
import java.util.Optional;

/**
 * A Route osztály egy vonalat reprezentál.
 */
public class Route {

    /**
     * A vonal egyedi azonosítója. Kötelező.
     */
    private final String routeId;

    /**
     * A vonal rövid neve. Kötelező.
     */
    private final String routeShortName;

    /**
     * A vonal hosszú neve. Opcionális
     */
    private final Optional<String> routeLongName;

    /**
     * A vonal típusa. Kötelező.
     */
    private final int routeType;

    /**
     * A vonal leírása. Opcionális
     */
    private Optional<String> routeDesc;

    /**
     * A vonal színe hex kód formátumban (pl. #FFFFFF). Opcionális
     */
    private Optional<String> routeColor;

    /**
     * A vonal szöveg színe hex kód formátumban (pl. #000000). Opcionális
     */
    private Optional<String> routeTextColor;

    /**
     * A vonal rendezési sorrendje. Opcionális
     */
    private Optional<Integer> routeSortOrder;

    /**
     * Konstruktor, amely minden mezőt inicializál, beleértve az opcionálisakat.
     *
     * @param agencyId        Az agency azonosítója.
     * @param routeId         A vonal egyedi azonosítója.
     * @param routeShortName  A vonal rövid neve.
     * @param routeLongName   A vonal hosszú neve.
     * @param routeType       A vonal típusa.
     * @param routeDesc       A vonal leírása.
     * @param routeColor      A vonal színe.
     * @param routeTextColor  A vonal szöveg színe.
     * @param routeSortOrder  A vonal rendezési sorrendje.
     */
    public Route(Optional<String> agencyId, String routeId, String routeShortName,
                 Optional<String> routeLongName, int routeType, Optional<String> routeDesc,
                 Optional<String> routeColor, Optional<String> routeTextColor,
                 Optional<Integer> routeSortOrder) {
        this.routeId = routeId;
        this.routeShortName = routeShortName;
        this.routeLongName = routeLongName;
        this.routeType = routeType;
        this.routeDesc = routeDesc;
        this.routeColor = routeColor;
        this.routeTextColor = routeTextColor;
        this.routeSortOrder = routeSortOrder;
    }

    /**
     * Visszaadja a vonal egyedi azonosítóját.
     *
     * @return A vonal egyedi azonosítója.
     */
    public String getRouteId() {
        return routeId;
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
     * @return A vonal hosszú neve, ha meg van adva.
     */
    public Optional<String> getRouteLongName() {
        return routeLongName;
    }

    /**
     * Visszaadja a vonal típusát.
     *
     * @return A vonal típusa.
     */
    public int getRouteType() {
        return routeType;
    }

    /**
     * Visszaadja a vonal szöveg színét Color objektumként.
     * Ha a szöveg színe nincs megadva, alapértelmezett fehér színt használ.
     *
     * @return A vonal szöveg színe Color objektumként.
     */
    public Color getColor() {
        String cString = "#" + routeColor.orElse("FFFFFF");
        return new Color(
                Integer.valueOf(cString.substring(1, 3), 16),
                Integer.valueOf(cString.substring(3, 5), 16),
                Integer.valueOf(cString.substring(5, 7), 16));
    }
}
