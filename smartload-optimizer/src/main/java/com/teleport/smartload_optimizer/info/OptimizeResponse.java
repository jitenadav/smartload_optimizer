package com.teleport.smartload_optimizer.info;

import lombok.Getter;

import java.util.List;

public record OptimizeResponse(String truck_id, List<String> selected_order_ids, long total_payout_cents,
                               int total_weight_lbs, int total_volume_cuft, double utilization_weight_percent,
                               double utilization_volume_percent) {

}
