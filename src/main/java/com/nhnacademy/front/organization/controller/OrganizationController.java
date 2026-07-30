package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.organization.client.OrganizationApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationApiClient orgApiClient;

}
