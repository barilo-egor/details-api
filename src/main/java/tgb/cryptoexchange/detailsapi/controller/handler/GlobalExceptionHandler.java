package tgb.cryptoexchange.detailsapi.controller.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.rpc.BadRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tgb.cryptoexchange.detailsapi.exceptions.BaseException;
import tgb.cryptoexchange.detailsapi.exceptions.EnableUniqueAmountException;

import java.net.URI;
import java.time.Instant;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String PROPERTY_TIMESTAMP = "timestamp";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(details);
        problemDetail.setType(URI.create("/errors/now-valid"));
        problemDetail.setProperty(PROPERTY_TIMESTAMP, Instant.now());

        return problemDetail;
    }

    @ExceptionHandler({ BaseException.class, Exception.class })
    public ProblemDetail handleUnexpectedErrors(Exception ex) {
        log.error("Произошла непредвиденная системная ошибка:", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problemDetail.setTitle("Service Unavailable");
        problemDetail.setType(URI.create("/errors/internal-server-error"));
        problemDetail.setProperty(PROPERTY_TIMESTAMP, Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(EnableUniqueAmountException.class)
    public ProblemDetail handleEnableUniqueAmountError() {
        log.error("Клиент запросил enableUniqueAmount=false, но api-merchant-details вернул amount.");

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Something went wrong. Please report this issue to our support team.");
        problemDetail.setType(URI.create("/errors/internal-server-error"));
        problemDetail.setProperty(PROPERTY_TIMESTAMP, Instant.now());

        return problemDetail;
    }

    @ExceptionHandler({ CompletionException.class, StatusRuntimeException.class })
    public ProblemDetail handleGrpcException(Throwable ex) {
        Throwable cause = (ex instanceof CompletionException) ? ex.getCause() : ex;

        if (cause instanceof StatusRuntimeException grpcEx) {
            Status.Code grpcCode = grpcEx.getStatus().getCode();

            if (grpcCode == Status.Code.UNAVAILABLE || grpcCode == Status.Code.INTERNAL) {
                return buildServiceUnavailableDetail(grpcEx);
            }

            com.google.rpc.Status status = StatusProto.fromThrowable(grpcEx);
            HttpStatus httpStatus = mapGrpcCodeToHttpStatus(grpcCode);

            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, status.getMessage());
            problemDetail.setProperty(PROPERTY_TIMESTAMP, Instant.now());

            if (status.getDetailsCount() > 0) {
                try {
                    BadRequest badRequest = status.getDetails(0).unpack(BadRequest.class);
                    problemDetail.setTitle("Validation failed");
                    problemDetail.setType(URI.create("/errors/validation-error"));

                    var errorsMap = badRequest.getFieldViolationsList().stream()
                            .collect(java.util.stream.Collectors.toMap(
                                    BadRequest.FieldViolation::getField,
                                    BadRequest.FieldViolation::getDescription,
                                    (existing, replacement) -> existing
                            ));
                    problemDetail.setProperty("invalid_params", errorsMap);
                } catch (InvalidProtocolBufferException e) {
                    problemDetail.setTitle("RPC Error Details Unpack Failed");
                }
            } else {
                problemDetail.setTitle(grpcEx.getStatus().getCode().name());
                problemDetail.setType(URI.create("/errors/" + grpcCode.name().toLowerCase()));
            }

            return problemDetail;
        }

        return buildServiceUnavailableDetail(ex);
    }

    /**
     * Хелпер для сборки ответа 503
     */
    private ProblemDetail buildServiceUnavailableDetail(Throwable ex) {
        log.error("Handling unexpected system error: ", ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Something went wrong. Please report this issue to our support team."
        );

        problemDetail.setTitle("Service Unavailable");
        problemDetail.setType(URI.create("/errors/service-unavailable"));
        problemDetail.setProperty(PROPERTY_TIMESTAMP, Instant.now());
        return problemDetail;
    }

    private HttpStatus mapGrpcCodeToHttpStatus(Status.Code grpcCode) {
        return switch (grpcCode) {
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
