package com.ueb.hotel.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validim në nivel klase për {@code ReservationForm}:
 * <ol>
 *   <li>largimi është pas ardhjes (së paku 1 natë);</li>
 *   <li>dhoma nuk është e zënë në ato data;</li>
 *   <li>numri i mysafirëve nuk e kalon kapacitetin e dhomës.</li>
 * </ol>
 * Gabimet lidhen me fushat përkatëse ({@code checkOut}, {@code roomId}, {@code guests}).
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidStayValidator.class)
public @interface ValidStay {

    String message() default "Rezervimi nuk është i vlefshëm.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
