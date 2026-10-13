package io.upro.daily.infrastructure.jdbc;

import io.upro.catalog.domain.Level;
import io.upro.daily.application.port.DailySnapshotRepository;
import io.upro.daily.domain.DailySnapshot;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class JdbcDailySnapshotRepository implements DailySnapshotRepository {

    private final JdbcClient jdbc;

    public JdbcDailySnapshotRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void saveAll(List<DailySnapshot> snapshots) {
        for (DailySnapshot s : snapshots) {
            jdbc.sql("delete from daily_snapshot where snapshot_date = ? and module_id = ?")
                    .params(s.date(), s.moduleId())
                    .update();
            jdbc.sql("""
                            insert into daily_snapshot (snapshot_date, module_id, rating, level, answers, correct)
                            values (?, ?, ?, ?, ?, ?)""")
                    .params(s.date(), s.moduleId(), s.rating(), s.level().name(), s.answers(), s.correct())
                    .update();
        }
    }

    @Override
    public Optional<LocalDate> lastSnapshotDate() {
        // max()/min() renvoient une ligne avec NULL quand la table est vide
        return jdbc.sql("select max(snapshot_date) from daily_snapshot")
                .query((rs, n) -> Optional.ofNullable(rs.getObject(1, LocalDate.class)))
                .single();
    }

    @Override
    public List<DailySnapshot> between(LocalDate fromInclusive, LocalDate toInclusive) {
        return jdbc.sql("""
                        select snapshot_date, module_id, rating, level, answers, correct from daily_snapshot
                        where snapshot_date between ? and ? order by snapshot_date, module_id""")
                .params(fromInclusive, toInclusive)
                .query((rs, n) -> new DailySnapshot(rs.getObject("snapshot_date", LocalDate.class),
                        rs.getString("module_id"), rs.getDouble("rating"), Level.valueOf(rs.getString("level")),
                        rs.getInt("answers"), rs.getInt("correct")))
                .list();
    }
}
