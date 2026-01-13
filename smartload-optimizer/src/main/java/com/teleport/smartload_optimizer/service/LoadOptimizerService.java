package com.teleport.smartload_optimizer.service;

import com.teleport.smartload_optimizer.info.OptimizeRequest;
import com.teleport.smartload_optimizer.info.OptimizeResponse;

public interface LoadOptimizerService {
    OptimizeResponse optimize(OptimizeRequest request);
}
