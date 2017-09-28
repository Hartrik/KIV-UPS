
package cz.hartrik.puzzle.module;

import cz.hartrik.puzzle.Application;

/**
 * Rozhraní pro modul, který do aplikace přidá nějakou funkcionalitu.
 *
 * @version 2016-07-10
 * @author Patrik Harag
 */
public interface ApplicationModule {

    /**
     * Přidá modul do aplikace.
     *
     * @param app rozhraní aplikace
     */
    void init(Application app);

}