package com.biobac.warehouse.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntityReferenceResponse {
    @JsonProperty("Id")
    private Long id;
    @JsonProperty("Name")
    private String name;
}
