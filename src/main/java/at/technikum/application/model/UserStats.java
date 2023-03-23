package at.technikum.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStats {
    @JsonProperty("Name")
    String name;
    @JsonProperty("Elo")
    Integer elo;
    @JsonProperty("Wins")
    Integer wins;
    @JsonProperty("Losses")
    Integer losses;
}
