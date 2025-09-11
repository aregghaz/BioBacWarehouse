package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttributeOptionValueResponse extends AttributeDefResponse{
    private List<OptionValueResponse> value;
}
