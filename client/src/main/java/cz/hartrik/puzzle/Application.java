package cz.hartrik.puzzle;

import cz.hartrik.puzzle.service.ServiceManager;

/**
 *
 * @version 2017-09-28
 * @author Patrik Harag
 */
public class Application {

    private final FrameStage frameStage;
    private final FrameController controller;
    private final ServiceManager serviceManager;

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
}