package com.dormitory.dormitory;

import com.dormitory.entity.Bed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OccupancyStatusTest {
    @Test void reportsAvailableWhenAtLeastOneBedIsEmpty() {
        Bed occupied = new Bed(); occupied.setStudentId(1L);
        Bed empty = new Bed();
        var summary = OccupancyStatus.summarize(List.of(occupied, empty));
        assertThat(summary.total()).isEqualTo(2);
        assertThat(summary.occupied()).isEqualTo(1);
        assertThat(summary.empty()).isEqualTo(1);
        assertThat(summary.status()).isEqualTo("AVAILABLE");
    }

    @Test void reportsFullWhenEveryBedIsOccupied() {
        Bed first = new Bed(); first.setStudentId(1L);
        Bed second = new Bed(); second.setStudentId(2L);
        assertThat(OccupancyStatus.summarize(List.of(first, second)).status()).isEqualTo("FULL");
    }
}
