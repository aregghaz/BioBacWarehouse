package com.biobac.warehouse.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CompanyResponse {
    private Long id;
    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private String website;
}
