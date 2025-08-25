package com.biobac.warehouse.service;

import com.biobac.warehouse.request.ProductCreateRequest;
import com.biobac.warehouse.response.ProductResponse;

public interface ProductService {
     ProductResponse create (ProductCreateRequest request);
}
