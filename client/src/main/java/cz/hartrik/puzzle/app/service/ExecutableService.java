package cz.hartrik.puzzle.app.service;

import cz.hartrik.puzzle.app.Application;
import java.util.function.Consumer;

/**
 * Rozhraní pro službu.
 *
 * @version 2016-07-10
 * @author Patrik Harag
 */
public interface ExecutableService extends Consumer<Application> {

}