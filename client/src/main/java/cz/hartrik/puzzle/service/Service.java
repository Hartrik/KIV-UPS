package cz.hartrik.puzzle.service;

import java.lang.annotation.*;

/**
 * Anotace sloužící k označení metody, která je službou.
 * Metoda označení touto anotací musí mít jeden parametr typu
 * {@link cz.hartrik.puzzle.Application}.
 *
 * @version 2016-07-09
 * @author Patrik Harag
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
public @interface Service {

    String value();

}