package com.viswa.crm.repository.jdbc;

import com.viswa.crm.model.ActivityStatusDefinition;
import com.viswa.crm.model.ActivityType;
import com.viswa.crm.repository.ActivityStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcActivityStatusRepository implements ActivityStatusRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String FIND_CURRENT_ORDER =
            "SELECT sort_order FROM activity_statuses " +
                    "WHERE activity_type = ? AND code = ?";

    private static final String FIND_NEXT_STATUSES =
            "SELECT activity_type, code, display_name, is_terminal, sort_order " +
                    "FROM activity_statuses " +
                    "WHERE activity_type = ? AND sort_order > ? " +
                    "ORDER BY sort_order ASC";

    private static final String FIND_DEFAULT =
            "SELECT code FROM activity_statuses " +
                    "WHERE activity_type = ? " +
                    "ORDER BY sort_order ASC LIMIT 1";

    private static final String IS_TERMINAL =
            "SELECT is_terminal FROM activity_statuses " +
                    "WHERE activity_type = ? AND code = ?";

    private static final String FIND_LABEL =
            "SELECT display_name FROM activity_statuses " +
                    "WHERE activity_type = ? AND code = ?";

    @Override
    public List<ActivityStatusDefinition> findNextStatuses(
            ActivityType type,
            String current
    ) {

        Integer currentOrder = jdbcTemplate.query(
                FIND_CURRENT_ORDER,
                rs -> rs.next() ? rs.getInt("sort_order") : null,
                type.name(),
                current
        );

        if (currentOrder == null) {
            return List.of();
        }

        return jdbcTemplate.query(
                FIND_NEXT_STATUSES,
                statusRowMapper(),
                type.name(),
                currentOrder
        );
    }


    @Override
    public String findDefaultStatus(ActivityType type) {
        return jdbcTemplate.queryForObject(
                FIND_DEFAULT,
                String.class,
                type.name()
        );
    }

    @Override
    public boolean isTerminal(ActivityType type, String statusCode) {

        List<Boolean> result = jdbcTemplate.query(
                IS_TERMINAL,
                (rs, rowNum) -> rs.getBoolean("is_terminal"),
                type.name(),
                statusCode
        );

        if (result.isEmpty()) {
            throw new RuntimeException(
                    "No status definition found for type="
                            + type + ", code=" + statusCode
            );
        }

        return result.get(0);
    }


    @Override
    public boolean isValidTransition(
            ActivityType type,
            String current,
            String next
    ) {

        Integer currentOrder = jdbcTemplate.query(
                FIND_CURRENT_ORDER,
                rs -> rs.next() ? rs.getInt("sort_order") : null,
                type.name(),
                current
        );

        Integer nextOrder = jdbcTemplate.query(
                FIND_CURRENT_ORDER,
                rs -> rs.next() ? rs.getInt("sort_order") : null,
                type.name(),
                next
        );

        if (currentOrder == null || nextOrder == null) {
            return false;
        }

        return nextOrder > currentOrder;
    }

    private RowMapper<ActivityStatusDefinition> statusRowMapper() {
        return (rs, rowNum) -> {
            ActivityStatusDefinition def = new ActivityStatusDefinition();
            def.setActivityType(rs.getString("activity_type"));
            def.setCode(rs.getString("code"));
            def.setDisplayName(rs.getString("display_name"));
            def.setTerminal(rs.getBoolean("is_terminal"));
            def.setSortOrder(rs.getInt("sort_order"));
            return def;
        };
    }
    @Override
    public String findLabel(ActivityType type, String code) {

        List<String> results = jdbcTemplate.query(
                FIND_LABEL,
                (rs, rowNum) -> rs.getString("display_name"),
                type.name(),
                code
        );

        return results.isEmpty() ? null : results.get(0);
    }

}
