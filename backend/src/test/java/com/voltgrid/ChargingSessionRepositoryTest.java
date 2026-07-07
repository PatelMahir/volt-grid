package com.voltgrid;

import com.voltgrid.model.ChargingSession;
import com.voltgrid.model.SessionStatus;
import com.voltgrid.repository.ChargingSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ChargingSessionRepositoryTest {

    @Autowired ChargingSessionRepository repository;

    private ChargingSession session(String station, double energy, BigDecimal cost,
                                    SessionStatus status, Instant startedAt) {
        return ChargingSession.builder()
                .stationExternalId(station).energyKwh(energy).cost(cost)
                .status(status).startedAt(startedAt).build();
    }

    @Test
    void recentSessions_areNewestFirstAndScopedToStation() {
        Instant now = Instant.now();
        repository.save(session("st-1", 5, new BigDecimal("1.50"), SessionStatus.COMPLETED, now.minus(1, ChronoUnit.HOURS)));
        repository.save(session("st-1", 8, null, SessionStatus.ACTIVE, now));
        repository.save(session("other", 3, new BigDecimal("0.90"), SessionStatus.COMPLETED, now));

        List<ChargingSession> result = repository
                .findByStationExternalIdOrderByStartedAtDesc("st-1", PageRequest.of(0, 10))
                .getContent();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEnergyKwh()).isEqualTo(8.0);
    }

    @Test
    void totalRevenue_sumsOnlyCompletedSessionsForStation() {
        Instant now = Instant.now();
        repository.save(session("st-1", 5, new BigDecimal("1.50"), SessionStatus.COMPLETED, now));
        repository.save(session("st-1", 10, new BigDecimal("3.00"), SessionStatus.COMPLETED, now));
        repository.save(session("st-1", 8, null, SessionStatus.ACTIVE, now));       // excluded
        repository.save(session("other", 3, new BigDecimal("9.90"), SessionStatus.COMPLETED, now)); // excluded

        assertThat(repository.totalRevenue("st-1")).isEqualByComparingTo("4.50");
    }
}
