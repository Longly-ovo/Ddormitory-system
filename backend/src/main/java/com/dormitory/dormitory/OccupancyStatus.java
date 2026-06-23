package com.dormitory.dormitory;

import com.dormitory.entity.Bed;

import java.util.List;

public final class OccupancyStatus {
    private OccupancyStatus() {}

    public static Summary summarize(List<Bed> beds) {
        long occupied = beds.stream().filter(bed -> bed.getStudentId() != null).count();
        long empty = beds.size() - occupied;
        return new Summary(beds.size(), occupied, empty, !beds.isEmpty() && empty == 0 ? "FULL" : "AVAILABLE");
    }

    public record Summary(long total, long occupied, long empty, String status) {}
}
