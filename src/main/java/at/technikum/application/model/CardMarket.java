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
public class CardMarket {
    @JsonProperty("Id")
    private String id;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Damage")
    private Double damage;
    @JsonProperty("cardType")
    private String cardType;
    @JsonProperty("elementType")
    private String elementType;
    @JsonProperty("price")
    private Integer price;
}
