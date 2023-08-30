package com.intuit.userbusinessprofile.service;

import com.intuit.userbusinessprofile.model.User;

public interface UserService {
    User getUser(String userId);

    User createUser(User user);

    User getUserAndUpdateCache(String userId);

}
