package com.intuit.userbusinessprofile.controller;

import com.intuit.userbusinessprofile.model.User;
import com.intuit.userbusinessprofile.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateUser() {
        User user = new User();
        when(userService.createUser(user)).thenReturn(user);

        ResponseEntity<User> responseEntity = userController.createUser(user);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(user, responseEntity.getBody());
        verify(userService, times(1)).createUser(user);
    }

    @Test
    public void testGetUser() {
        String userId = "123";
        User user = new User();
        when(userService.getUser(userId)).thenReturn(user);

        ResponseEntity<User> responseEntity = userController.getUser(userId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(user, responseEntity.getBody());
        verify(userService, times(1)).getUser(userId);
    }
}
