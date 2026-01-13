package com.teleport.smartload_optimizer.utils;

import com.teleport.smartload_optimizer.info.OptimizeRequest;
import com.teleport.smartload_optimizer.info.OrderDto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Comparator;

public class RequestFingerprint {
    private RequestFingerprint() {}

    public static String fingerprint(OptimizeRequest req) {
        StringBuilder sb = new StringBuilder(1024);

        // Truck
        sb.append("truck_id=").append(req.getTruck().getId()).append('|');
        sb.append("maxW=").append(req.getTruck().getMax_weight_lbs()).append('|');
        sb.append("maxV=").append(req.getTruck().getMax_volume_cuft()).append('|');

        // Orders: sort to make key order-independent
        req.getOrders().stream()
                .sorted(Comparator.comparing(OrderDto::getId))
                .forEach(o -> {
                    sb.append("ord:")
                            .append(o.getId()).append(',')
                            .append(o.getPayout_cents()).append(',')
                            .append(o.getWeight_lbs()).append(',')
                            .append(o.getVolume_cuft()).append(',')
                            .append(o.getOrigin()).append(',')
                            .append(o.getDestination()).append(',')
                            .append(o.getPickup_date()).append(',')
                            .append(o.getDelivery_date()).append(',')
                            .append(o.getIs_hazmat())
                            .append('|');
                });

        // Hash the whole string to keep key short
        return sha256Hex(sb.toString());
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder out = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                out.append(String.format("%02x", b));
            }
            return out.toString();
        } catch (Exception e) {
            // fallback: extremely unlikely
            return Integer.toHexString(input.hashCode());
        }
    }
}
