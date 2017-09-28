package cz.hartrik.puzzle.service;

import cz.hartrik.puzzle.Application;
import java.util.function.Consumer;

/**
 * Rozhraní pro službu.
 *
 * @version 2016-07-10
 * @author Patrik Harag
 */
public interface ExecutableService extends Consumer<Application> {

}