package cz.hartrik.puzzle.app;

import cz.hartrik.puzzle.net.ConnectionHolder;
import cz.hartrik.puzzle.app.page.ErrorPage;
import cz.hartrik.puzzle.app.page.LogInPage;
import cz.hartrik.puzzle.app.page.Page;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 *
 * @version 2017-11-12
 * @author Patrik Harag
 */
public class Application {

    public static final int DEFAULT_TIMEOUT = 4000;  // ms

    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

    private final FrameStage frameStage;
    private final FrameController controller;
    private final Page defaultPage;

    private ConnectionHolder connection;

    public Application(FrameStage frameStage, FrameController controller) {
        this.frameStage = frameStage;
        this.controller = controller;
        this.defaultPage = new LogInPage(this);

        setActivePage(defaultPage);
    }

    public FrameStage getStage() {
        return frameStage;
    }

    public FrameController getController() {
        return controller;
    }

    public void setActivePage(Page activePage) {
        if (Platform.isFxApplicationThread())
            getController().setActivePage(activePage);
        else
            Platform.runLater(() -> getController().setActivePage(activePage));
    }

    public void setConnection(ConnectionHolder connection) {
        this.connection = connection;

        connection.setOnConnectionLost(() -> {
            Page errorPage = new ErrorPage(this, defaultPage, "Connection lost");
            setActivePage(errorPage);
            this.connection = null;
        });
    }

    public ConnectionHolder getConnection() {
        return connection;
    }

    public void logException(String msg, Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString(); // stack trace as a string

        LOGGER.warning(msg + "\n" + stackTrace);
    }

    public void log(String msg) {
        LOGGER.warning(msg);
    }

    void onClose() {
        controller.getActivePage().onClose();

        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                logException("While closing:", e);
            }
        }
    }

}