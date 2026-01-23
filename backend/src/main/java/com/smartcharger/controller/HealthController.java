package com.smartcharger.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: sonicge
 * @CreateTime: 2026-01-23
 */

@RestController
@RequestMapping
public class HealthController {
    @GetMapping("/health")
    public String health(){
        return "健康";
    }
}
