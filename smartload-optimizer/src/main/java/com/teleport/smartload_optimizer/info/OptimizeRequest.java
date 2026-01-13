package com.teleport.smartload_optimizer.info;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OptimizeRequest {

    @NotNull
    @Valid
    private TruckDto truck;

    @NotNull
    @Valid
    private List<OrderDto> orders;
}
