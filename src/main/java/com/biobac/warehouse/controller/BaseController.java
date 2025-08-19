package com.biobac.warehouse.controller;

import jakarta.servlet.http.HttpServletRequest;

public abstract class BaseController {

    protected String getUsername(HttpServletRequest request) {
        return (String) request.getAttribute("username");
    }
}