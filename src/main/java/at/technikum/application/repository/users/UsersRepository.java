package at.technikum.application.repository.users;

import at.technikum.application.model.Credentials;
import at.technikum.application.model.UserData;

public interface UsersRepository {
    String registerUser(Credentials credentials);
    String getUserData(String username);
    String updateUser(String username, UserData userData);
    String loginUser(Credentials credentials);
}
