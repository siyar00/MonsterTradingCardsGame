package at.technikum.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String username;
    private String password;
    private String name;
    private String bio;
    private String image;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
