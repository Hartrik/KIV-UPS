package cz.hartrik.puzzle.page;

import cz.hartrik.puzzle.Application;

/**
 * An abstract page.
 *
 * @author Patrik Harag
 * @version 2017-10-17
 */
public abstract class PageBase implements Page {

    protected final Application application;

    public PageBase(Application application) {
        this.application = application;
    }

}
