package at.technikum.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Credentials {

    @JsonProperty("Username")
    private String username;
    @JsonProperty("Password")
    private String password;

}
