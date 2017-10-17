package cz.hartrik.puzzle;

import cz.hartrik.puzzle.net.ConnectionHolder;
import cz.hartrik.puzzle.page.Page;
import cz.hartrik.puzzle.service.ServiceManager;
import javafx.application.Platform;

/**
 *
 * @version 2017-10-10
 * @author Patrik Harag
 */
public class Application {

    private final FrameStage frameStage;
    private final FrameController controller;
    private final ServiceManager serviceManager;

    private ConnectionHolder connection;

    public Application(FrameStage frameStage, FrameController controller) {
        this.frameStage = frameStage;
        this.controller = controller;
        this.serviceManager = new ServiceManager(this);
    }

    public FrameStage getStage() {
        return frameStage;
    }

    public FrameController getController() {
        return controller;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public void setActivePage(Page activePage) {
        if (Platform.isFxApplicationThread())
            getController().setActivePage(activePage);
        else
            Platform.runLater(() -> getController().setActivePage(activePage));
    }

    public void setConnection(ConnectionHolder connection) {
        this.connection = connection;
    }

    public ConnectionHolder getConnection() {
        return connection;
    }
}