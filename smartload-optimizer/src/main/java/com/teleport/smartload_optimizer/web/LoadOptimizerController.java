package com.teleport.smartload_optimizer.web;

import com.teleport.smartload_optimizer.service.LoadOptimizerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/load-optimizer")
public class LoadOptimizerController {
    private final LoadOptimizerService optimizerService;

    @Autowired
    public LoadOptimizerController(LoadOptimizerService optimizerService) {
        this.optimizerService = optimizerService;
    }
}
