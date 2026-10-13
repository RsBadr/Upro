package io.upro.daily.application.port;

import io.upro.daily.domain.DailySnapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailySnapshotRepository {

    /** Remplace les bilans existants pour la meme date et le meme module (idempotent). */
    void saveAll(List<DailySnapshot> snapshots);

    Optional<LocalDate> lastSnapshotDate();

    List<DailySnapshot> between(LocalDate fromInclusive, LocalDate toInclusive);
}
