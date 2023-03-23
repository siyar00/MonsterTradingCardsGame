package at.technikum.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserData {
    @JsonProperty("Name")
    String name;
    @JsonProperty("Bio")
    String bio;
    @JsonProperty("Image")
    String image;
}
