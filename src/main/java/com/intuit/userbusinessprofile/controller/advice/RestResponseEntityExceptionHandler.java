package com.intuit.userbusinessprofile.controller.advice;

import com.intuit.userbusinessprofile.dto.GlobalExceptionDto;
import com.intuit.userbusinessprofile.exceptions.EntityNotFoundException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@RestControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    public RestResponseEntityExceptionHandler() {
        super();
    }


    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<GlobalExceptionDto> handleIllegalArgumentException(final IllegalArgumentException e) {
        GlobalExceptionDto globalExceptionDto = new GlobalExceptionDto();
        globalExceptionDto.setResultCode(400);
        globalExceptionDto.setReason(e.getMessage());
        return ResponseEntity.status(400).body(globalExceptionDto);
    }

    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<GlobalExceptionDto> handleEntityNotFoundException(final EntityNotFoundException e) {
        GlobalExceptionDto globalExceptionDto = new GlobalExceptionDto();
        globalExceptionDto.setResultCode(404);
        globalExceptionDto.setReason(e.getMessage());
        return ResponseEntity.status(404).body(globalExceptionDto);
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<GlobalExceptionDto> handleRuntimeException(final RuntimeException e) {
        GlobalExceptionDto globalExceptionDto = new GlobalExceptionDto();
        globalExceptionDto.setResultCode(500);
        globalExceptionDto.setReason(e.getMessage());
        return ResponseEntity.status(500).body(globalExceptionDto);
    }
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return convertExceptionToGlobalExceptionDto(status,ex);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return convertExceptionToGlobalExceptionDto(status,ex);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return convertExceptionToGlobalExceptionDto(status,ex);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return convertExceptionToGlobalExceptionDto(status,ex);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return convertExceptionToGlobalExceptionDto(status,ex);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return convertExceptionToGlobalExceptionDto(status,ex);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return convertExceptionToGlobalExceptionDto(status,ex);
    }

    private ResponseEntity<Object> convertExceptionToGlobalExceptionDto(HttpStatusCode status, Exception ex){
        GlobalExceptionDto registrationResponseDto = new GlobalExceptionDto();
        registrationResponseDto.setResultCode(status.value());
        registrationResponseDto.setReason(ex.getMessage());
        return ResponseEntity.status(status).body(registrationResponseDto);
    }

}
