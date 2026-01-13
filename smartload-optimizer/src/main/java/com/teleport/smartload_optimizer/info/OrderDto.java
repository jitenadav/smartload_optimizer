package com.teleport.smartload_optimizer.info;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Setter
@Getter
@ToString
public class OrderDto {
    @NotBlank
    private String id;

    @NotNull
    @Min(0)
    private Long payout_cents;

    @NotNull @Min(0)
    private Integer weight_lbs;

    @NotNull @Min(0)
    private Integer volume_cuft;

    @NotBlank
    private String origin;

    @NotBlank
    private String destination;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate pickup_date;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate delivery_date;

    @NotNull
    private Boolean is_hazmat;
}
