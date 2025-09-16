package com.biobac.warehouse.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "attribute-service", url = "${services.attribute-url}")
public interface AttributeClient {
}
