package home.thienph.jcef.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SayHelloReq {
    @JsonProperty("name")
    private String name;
}