package model;

import java.util.Optional;

/**
 * A Pathway osztály egy aluljárót reprezentál, amely összeköt két megállót.
 */
public class Pathway {
    /**
     * Az aluljáró egyedi azonosítója. Kötelező.
     */
    private String pathwayId;

    /**
     * Az aluljáró módja. Kötelező (1 = folyosó, 2 = lépcső, stb.).
     */
    private int pathwayMode;

    /**
     * Az aluljáró kétirányúságát jelzi. Kötelező (0 = egyirányú, 1 = kétirányú).
     */
    private int isBidirectional;

    /**
     * Az indulási megálló azonosítója. Kötelező.
     */
    private String fromStopId;

    /**
     * Az érkezési megálló azonosítója. Kötelező.
     */
    private String toStopId;

    /**
     * Az átkelési idő másodpercekben. Opcionális.
     */
    private Optional<Integer> traversalTime;

    /**
     * Konstruktor, amely minden mezőt inicializál, beleértve az opcionális átkelési időt.
     *
     * @param pathwayId      Az aluljáró egyedi azonosítója.
     * @param pathwayMode    Az aluljáró módja.
     * @param isBidirectional Az aluljáró kétirányúságát jelzi.
     * @param fromStopId     Az indulási megálló azonosítója.
     * @param toStopId       Az érkezési megálló azonosítója.
     * @param traversalTime  Az átkelési idő másodpercekben.
     */
    public Pathway(String pathwayId, int pathwayMode, int isBidirectional,
                   String fromStopId, String toStopId, Optional<Integer> traversalTime) {
        this.pathwayId = pathwayId;
        this.pathwayMode = pathwayMode;
        this.isBidirectional = isBidirectional;
        this.fromStopId = fromStopId;
        this.toStopId = toStopId;
        this.traversalTime = traversalTime;
    }

    /**
     * Visszaadja az aluljáró azonosítóját.
     *
     * @return Az aluljáró azonosítója.
     */
    public String getPathwayId() {
        return pathwayId;
    }


    /**
     * Visszaadja az aluljáró módját.
     *
     * @return Az aluljáró módja.
     */
    public int getPathwayMode() {
        return pathwayMode;
    }

    /**
     * Visszaadja, hogy az aluljáró kétirányú-e.
     *
     * @return 0, ha egyirányú, 1 ha kétirányú.
     */
    public int getIsBidirectional() {
        return isBidirectional;
    }

    /**
     * Visszaadja az indulási megálló azonosítóját.
     *
     * @return Az indulási megálló azonosítója.
     */
    public String getFromStopId() {
        return fromStopId;
    }

    /**
     * Visszaadja az érkezési megálló azonosítóját.
     *
     * @return Az érkezési megálló azonosítója.
     */
    public String getToStopId() {
        return toStopId;
    }

    /**
     * Visszaadja az átkelési időt.
     *
     * @return Az átkelési idő másodpercekben, ha meg van adva.
     */
    public Optional<Integer> getTraversalTime() {
        return traversalTime;
    }
}
