package com.org.Sistema_Bancario.SpringBoot.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InstanceController {
    @Value("${INSTANCE_ID:local}")
    private String instanceId;

    @GetMapping("/instance")
    public String getInstanceId(){
        return instanceId;
    }
}
