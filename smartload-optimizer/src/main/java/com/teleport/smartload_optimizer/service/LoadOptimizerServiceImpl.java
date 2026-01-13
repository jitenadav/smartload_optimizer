package com.teleport.smartload_optimizer.service;

import com.teleport.smartload_optimizer.info.OptimizeRequest;
import com.teleport.smartload_optimizer.info.OptimizeResponse;
import com.teleport.smartload_optimizer.info.OrderDto;
import com.teleport.smartload_optimizer.info.TruckDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoadOptimizerServiceImpl implements  LoadOptimizerService{
    private static final Logger LOG = LoggerFactory.getLogger(LoadOptimizerServiceImpl.class);
    private static final boolean ALLOW_HAZMAT = true;
    private static final boolean HAZMAT_MUST_BE_ALONE = true;

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
        Map<String, List<OrderDto>> laneGroups = groupByLane(orders);
        LOG.info("LaneGroups {}", laneGroups);
        OptimizeResponse bestAcrossLanes = emptyResponse(truck);
        long bestPayout = 0;

        for (List<OrderDto> laneOrders : laneGroups.values()) {
            OptimizeResponse laneBest = solveLane(truck, laneOrders);
            if (laneBest.total_payout_cents() > bestPayout) {
                bestPayout = laneBest.total_payout_cents();
                bestAcrossLanes = laneBest;
            }
        }
        return bestAcrossLanes;
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

    private Map<String, List<OrderDto>> groupByLane(List<OrderDto> orders) {
        Map<String, List<OrderDto>> groups = new HashMap<>();
        for (OrderDto o : orders) {
            if (o == null) continue;
            if (o.getOrigin() == null || o.getDestination() == null) continue;
            String key = o.getOrigin().trim() + " -> " + o.getDestination().trim();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(o);
        }
        return groups;
    }

    private OptimizeResponse solveLane(TruckDto truck, List<OrderDto> laneOrders) {
        List<OrderDto> valid = new ArrayList<>();
        for (OrderDto o : laneOrders) {
            if (o.getPickup_date() == null || o.getDelivery_date() == null) continue;
            if (o.getPickup_date().isAfter(o.getDelivery_date())) continue;
            if (!ALLOW_HAZMAT && Boolean.TRUE.equals(o.getIs_hazmat())) continue;
            valid.add(o);
        }
        LOG.info("Valid: {}", valid);
        if (valid.isEmpty()) return emptyResponse(truck);

        int n = valid.size();
        int maxMask = 1 << n;

        long[] payout = new long[maxMask];
        int[] weight = new int[maxMask];
        int[] volume = new int[maxMask];
        int[] maxPickupDay = new int[maxMask];
        int[] minDeliveryDay = new int[maxMask];
        boolean[] hasHazmat = new boolean[maxMask];
        boolean[] feasible = new boolean[maxMask];

        int[] orderPickupDay = new int[n];
        int[] orderDeliveryDay = new int[n];
        long[] orderPayout = new long[n];
        int[] orderWeight = new int[n];
        int[] orderVolume = new int[n];
        boolean[] orderHazmat = new boolean[n];

        for (int i = 0; i < n; i++) {
            OrderDto o = valid.get(i);
            orderPickupDay[i] = encodeDay(o.getPickup_date());
            orderDeliveryDay[i] = encodeDay(o.getDelivery_date());
            orderPayout[i] = o.getPayout_cents();
            orderWeight[i] = o.getWeight_lbs();
            orderVolume[i] = o.getVolume_cuft();
            orderHazmat[i] = Boolean.TRUE.equals(o.getIs_hazmat());
        }

        // init empty set
        feasible[0] = true;
        maxPickupDay[0] = Integer.MIN_VALUE;
        minDeliveryDay[0] = Integer.MAX_VALUE;

        int bestMask = 0;
        long bestPay = 0;

        for (int mask = 1; mask < maxMask; mask++) {
            int lsb = mask & -mask;
            int idx = Integer.numberOfTrailingZeros(lsb);
            int prev = mask ^ lsb;

            if (!feasible[prev]) continue;

            long p = payout[prev] + orderPayout[idx];
            int w = weight[prev] + orderWeight[idx];
            int v = volume[prev] + orderVolume[idx];

            if (w > truck.getMax_weight_lbs() || v > truck.getMax_volume_cuft()) continue;

            boolean hz = hasHazmat[prev] || orderHazmat[idx];
            if (HAZMAT_MUST_BE_ALONE && hz && Integer.bitCount(mask) > 1) continue;

            int mxPick = Math.max(maxPickupDay[prev], orderPickupDay[idx]);
            int mnDel = Math.min(minDeliveryDay[prev], orderDeliveryDay[idx]);

            // Group time-window feasibility: there exists schedule if maxPickup <= minDelivery
            if (mxPick > mnDel) continue;

            feasible[mask] = true;
            payout[mask] = p;
            weight[mask] = w;
            volume[mask] = v;
            hasHazmat[mask] = hz;
            maxPickupDay[mask] = mxPick;
            minDeliveryDay[mask] = mnDel;

            // choose best by payout; tie-breaker by higher utilization (weight+volume)
            if (p > bestPay) {
                bestPay = p;
                bestMask = mask;
            } else if (p == bestPay && bestPay > 0) {
                long utilScoreThis = ((long) w * 1_000_000L / truck.getMax_weight_lbs())
                        + ((long) v * 1_000_000L / truck.getMax_volume_cuft());
                long utilScoreBest = ((long) weight[bestMask] * 1_000_000L / truck.getMax_weight_lbs())
                        + ((long) volume[bestMask] * 1_000_000L / truck.getMax_volume_cuft());
                if (utilScoreThis > utilScoreBest) {
                    bestMask = mask;
                }
            }
        }

        if (bestMask == 0) return emptyResponse(truck);

        List<String> selected = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if ((bestMask & (1 << i)) != 0) {
                selected.add(valid.get(i).getId());
            }
        }

        int totalW = weight[bestMask];
        int totalV = volume[bestMask];
        long totalP = payout[bestMask];

        double utilW = round2((totalW * 100.0) / truck.getMax_weight_lbs());
        double utilV = round2((totalV * 100.0) / truck.getMax_volume_cuft());

        return new OptimizeResponse(
                truck.getId(),
                selected,
                totalP,
                totalW,
                totalV,
                utilW,
                utilV
        );
    }

    private int encodeDay(LocalDate d) {
        return d.getYear() * 400 + d.getDayOfYear();
    }

    private double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}
