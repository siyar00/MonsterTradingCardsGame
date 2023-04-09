package at.technikum.application.service;

import at.technikum.application.model.Credentials;
import at.technikum.application.model.UserData;
import at.technikum.application.repository.users.UsersRepository;

public record UserService(UsersRepository usersRepository) {
    public String registerUser(Credentials credentials) {
        return usersRepository.registerUser(credentials);
    }

    public String readUserData(String username) {
        return usersRepository.getUserData(username);
    }

    public String updateUser(String username, UserData userData) {
        return usersRepository.updateUser(username, userData);
    }

    public String loginUser(Credentials credentials) {
        return usersRepository.loginUser(credentials);
    }
}
