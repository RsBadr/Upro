package io.upro.config;

import io.upro.catalog.application.ModuleNotFoundException;
import io.upro.catalog.application.QuestionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduit les exceptions applicatives en reponses RFC 9457 (Problem Details).
 */
@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler({ModuleNotFoundException.class, QuestionNotFoundException.class})
    ProblemDetail notFound(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
