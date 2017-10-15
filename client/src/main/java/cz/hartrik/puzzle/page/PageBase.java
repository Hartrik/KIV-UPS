package cz.hartrik.puzzle.page;

import cz.hartrik.puzzle.Application;
import cz.hartrik.puzzle.net.ConnectionHolder;

/**
 * An abstract page.
 *
 * @author Patrik Harag
 * @version 2017-10-15
 */
public abstract class PageBase implements Page {

    protected final Application application;
    protected final ConnectionHolder connection;

    public PageBase(Application application, ConnectionHolder connection) {
        this.application = application;
        this.connection = connection;
    }

}
