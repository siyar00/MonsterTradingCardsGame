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
public class Trading {
    @JsonProperty("Id")
    private String id;
    @JsonProperty("CardToTrade")
    private String cardToTrade;
    @JsonProperty("Type")
    private String type;
    @JsonProperty("MinimumDamage")
    private Double minimumDamage;
}
