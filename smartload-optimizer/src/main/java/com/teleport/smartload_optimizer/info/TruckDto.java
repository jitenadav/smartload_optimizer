package com.teleport.smartload_optimizer.info;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TruckDto {
    @NotBlank
    private String id;

    @NotNull
    @Min(1)
    private Integer max_weight_lbs;

    @NotNull @Min(1)
    private Integer max_volume_cuft;
}
