package com.project.revconnect.config;

import com.project.revconnect.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        HttpStatus status = resolveStatus(ex.getMessage());
        String message = safeMessage(ex.getMessage());

        if (status.is5xxServerError()) {
            log.error("Unhandled runtime exception on {} {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    ex);
        } else {
            log.warn("Request failed on {} {}: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    message);
        }

        return buildResponse(status, message, request);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        log.warn("Bad request on {} {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request payload", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Forbidden request on {} {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "You are not authorized to perform this action", request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("Payload too large on {} {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage());
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "Size is more. Max 1GB only.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {} {}",
                request.getMethod(),
                request.getRequestURI(),
                ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again.", request);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    private HttpStatus resolveStatus(String message) {
        if (message == null || message.isBlank()) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String value = message.trim().toLowerCase();

        if (value.contains("not authenticated") || value.contains("token is missing") || value.contains("token is invalid")) {
            return HttpStatus.UNAUTHORIZED;
        }

        if ("unauthorized".equals(value) || value.startsWith("unauthorized")) {
            return HttpStatus.FORBIDDEN;
        }

        if (value.contains("not found")) {
            return HttpStatus.NOT_FOUND;
        }

        if (value.contains("already")) {
            return HttpStatus.CONFLICT;
        }

        if (value.contains("only ") || value.contains("must be") || value.contains("required")
                || value.contains("invalid") || value.contains("cannot")) {
            return HttpStatus.BAD_REQUEST;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String safeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Something went wrong. Please try again.";
        }
        return message;
    }
}
