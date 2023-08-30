package com.intuit.userbusinessprofile.service.impl;

import com.intuit.userbusinessprofile.exceptions.EntityNotFoundException;
import com.intuit.userbusinessprofile.model.User;
import com.intuit.userbusinessprofile.repository.UserRepository;
import com.intuit.userbusinessprofile.service.UserService;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Cacheable(key = "#userId", value = "Users")
    public User getUser(String userId) {
        return getUserFromDb(userId);
    }

    @Override
    public User createUser(User user) {
        return userRepository.createUser(user);
    }

    @Override
    @CachePut(key = "#userId", value = "Users")
    public User getUserAndUpdateCache(String userId) {
        return getUserFromDb(userId);
    }

    private User getUserFromDb(String userId) {
        User user  = userRepository.getUser(userId);
        if(user == null)
            throw new EntityNotFoundException("User with id - "+userId+" not found");
        return user;
    }

}
