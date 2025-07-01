package com.cdyt.be.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/common")
public class CommonController {
  @Value("${spring.data.redis.host}")
  private String profile;
  @GetMapping("/test")
  public String Test() {
    return "Hello World, current profile: " + profile;
  }
}
