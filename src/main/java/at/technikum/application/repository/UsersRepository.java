package at.technikum.application.repository;

import at.technikum.application.model.Credentials;
import at.technikum.application.model.UserDataRec;

public interface UsersRepository {
    String registerUser(Credentials credentials);
    String getUserData(String username);
    String updateUser(String username, UserDataRec userDataRec);
    String loginUser(Credentials credentials);
}
