package com.teleport.smartload_optimizer.service;

import com.teleport.smartload_optimizer.info.OptimizeRequest;
import com.teleport.smartload_optimizer.info.OptimizeResponse;
import com.teleport.smartload_optimizer.info.OrderDto;
import com.teleport.smartload_optimizer.info.TruckDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoadOptimizerServiceImpl implements  LoadOptimizerService{
    private static final Logger LOG = LoggerFactory.getLogger(LoadOptimizerServiceImpl.class);

    @Override
    public OptimizeResponse optimize(OptimizeRequest request) {
        TruckDto truck = request.getTruck();
        List<OrderDto> orders = request.getOrders();
        LOG.info("Truck {}", truck);
        LOG.info("Truck Orders: {}", orders);
        if (orders == null || orders.isEmpty()) {
            return emptyResponse(truck);
        }

        // limit for judge constraints; prevents insane payloads
        if (orders.size() > 22) {
            throw new IllegalArgumentException("Too many orders; max supported is 22");
        }
        return null;
    }

    private OptimizeResponse emptyResponse(TruckDto truck) {
        return new OptimizeResponse(
                truck.getId(),
                List.of(),
                0,
                0,
                0,
                0.0,
                0.0
        );
    }
}
