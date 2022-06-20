package com.project.fitclub.shared;

import com.project.fitclub.error.ApiError;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.IOException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        ApiError apiError = new ApiError(400, "Validation error", request.getServletPath());

        BindingResult result = exception.getBindingResult();

        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError fieldError : result.getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        apiError.setValidationErrors(validationErrors);
        return apiError;
    }

    @ExceptionHandler({IOException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleUploadFileExtensionException(IOException exception, HttpServletRequest request) {
        ApiError apiError = new ApiError(400, exception.getMessage(), request.getServletPath());
        return apiError;
    }

    @ExceptionHandler({FileSizeLimitExceededException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleUploadFileSizeException(FileSizeLimitExceededException exception, HttpServletRequest request) {
        ApiError apiError = new ApiError(400, exception.getMessage(), request.getServletPath());
        return apiError;
    }

    @ExceptionHandler({SQLIntegrityConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleEmailAlreadyInUseException(SQLIntegrityConstraintViolationException exception, HttpServletRequest request) {
        ApiError apiError = new ApiError(400, "This email is already connected to an account!", request.getServletPath());
        return apiError;
    }
}
