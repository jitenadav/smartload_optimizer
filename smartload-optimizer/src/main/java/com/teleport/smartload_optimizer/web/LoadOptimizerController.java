package com.teleport.smartload_optimizer.web;

import com.teleport.smartload_optimizer.info.OptimizeRequest;
import com.teleport.smartload_optimizer.info.OptimizeResponse;
import com.teleport.smartload_optimizer.service.LoadOptimizerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/load-optimizer")
public class LoadOptimizerController {
    private static final Logger LOG = LoggerFactory.getLogger(LoadOptimizerController.class);
    private final LoadOptimizerService optimizerService;

    @Autowired
    public LoadOptimizerController(LoadOptimizerService optimizerService) {
        this.optimizerService = optimizerService;
    }

    @PostMapping("/optimize")
    public ResponseEntity optimize(@Valid @RequestBody OptimizeRequest request) {
        OptimizeResponse optimizedPayouts =  optimizerService.optimize(request);
        LOG.info("Optimized Payout {}", optimizedPayouts);
        return ResponseEntity.ok("Optimization completed successfully.");
    }
}
