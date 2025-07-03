package com.cdyt.be.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/common")
@Tag(name = "Common", description = "Common APIs for the application")
public class CommonController {

}
