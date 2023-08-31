package com.intuit.userbusinessprofile.controller.advice;


import com.intuit.userbusinessprofile.dto.GlobalExceptionDto;
import com.intuit.userbusinessprofile.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class RestResponseEntityExceptionHandlerTest {

    @InjectMocks
    private RestResponseEntityExceptionHandler exceptionHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void handleIllegalArgumentExceptionTest() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");
        ResponseEntity<GlobalExceptionDto> responseEntity = exceptionHandler.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(400, responseEntity.getBody().getResultCode());
        assertEquals("Invalid argument", responseEntity.getBody().getReason());
    }

    @Test
    public void handleEntityNotFoundExceptionTest() {
        EntityNotFoundException exception = new EntityNotFoundException("Entity not found");
        ResponseEntity<GlobalExceptionDto> responseEntity = exceptionHandler.handleEntityNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(404, responseEntity.getBody().getResultCode());
        assertEquals("Entity not found", responseEntity.getBody().getReason());
    }

    @Test
    public void handleRuntimeExceptionTest() {
        RuntimeException exception = new RuntimeException("Internal server error");
        ResponseEntity<GlobalExceptionDto> responseEntity = exceptionHandler.handleRuntimeException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(500, responseEntity.getBody().getResultCode());
        assertEquals("Internal server error", responseEntity.getBody().getReason());
    }

    @Test
    public void handleMissingServletRequestParameterTest() {
        MissingServletRequestParameterException exception = new MissingServletRequestParameterException("paramName", "String");
        ResponseEntity<Object> responseEntity = exceptionHandler.handleMissingServletRequestParameter(exception, null, HttpStatus.BAD_REQUEST, mock(WebRequest.class));

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(400, ((GlobalExceptionDto) responseEntity.getBody()).getResultCode());
        assertEquals("Required request parameter 'paramName' for method parameter type String is not present", ((GlobalExceptionDto) responseEntity.getBody()).getReason());
    }

    @Test
    public void convertExceptionToGlobalExceptionDtoTest() {
        HttpStatus status = HttpStatus.NOT_FOUND;
        Exception exception = new Exception("Test exception");
        ResponseEntity<Object> responseEntity = exceptionHandler.convertExceptionToGlobalExceptionDto(status, exception);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(404, ((GlobalExceptionDto) responseEntity.getBody()).getResultCode());
        assertEquals("Test exception", ((GlobalExceptionDto) responseEntity.getBody()).getReason());
    }
}
