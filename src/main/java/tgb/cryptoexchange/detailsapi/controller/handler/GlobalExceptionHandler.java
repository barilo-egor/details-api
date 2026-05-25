package tgb.cryptoexchange.detailsapi.controller.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tgb.cryptoexchange.detailsapi.dto.ClientApiErrorResponse;
import tgb.cryptoexchange.detailsapi.exceptions.BaseException;
import tgb.cryptoexchange.detailsapi.exceptions.EnableUniqueAmountException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ClientApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ClientApiErrorResponse errorResponse = new ClientApiErrorResponse(
                "Bad Request",
                HttpStatus.BAD_REQUEST.value(),
                details
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler({ BaseException.class, Exception.class })
    public ResponseEntity<ClientApiErrorResponse> handleUnexpectedErrors(Exception ex) {
        log.error("Произошла непредвиденная системная ошибка:", ex);

        ClientApiErrorResponse errorResponse = new ClientApiErrorResponse(
                "Service Unavailable",
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Something went wrong. Please report this issue to our support team."
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(EnableUniqueAmountException.class)
    public ResponseEntity<ClientApiErrorResponse> handleEnableUniqueAmountError(Exception ex) {
        log.error("Клиент запросил enableUniqueAmount=false, но api-merchant-details вернул amount.");

        ClientApiErrorResponse errorResponse = new ClientApiErrorResponse(
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Something went wrong. Please report this issue to our support team."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
