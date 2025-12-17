package com.viswa.crm.repository.jdbc;

import com.viswa.crm.model.Activity;
import com.viswa.crm.model.ActivityType;
import com.viswa.crm.model.Deal;
import com.viswa.crm.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcActivityRepository implements ActivityRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    /* ============================
       SQL QUERIES
       ============================ */

    private static final String FIND_BY_ID =
            "SELECT id, deal_id, owner_user_id, type, status_code, description, due_date, created_at " +
                    "FROM activities WHERE id = ?";

    private static final String FIND_BY_DEAL =
            "SELECT id, deal_id, owner_user_id, type, status_code, description, due_date, created_at " +
                    "FROM activities WHERE deal_id = ? ORDER BY due_date DESC";

    private static final String INSERT =
            "INSERT INTO activities (deal_id, owner_user_id, type, status_code, description, due_date, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE activities SET type = ?, status_code = ?, description = ?, due_date = ? WHERE id = ?";

    private static final String DELETE =
            "DELETE FROM activities WHERE id = ?";

    private static final String EXISTS =
            "SELECT COUNT(1) FROM activities WHERE id = ?";

    private static final String FIND_RECENT =
            "SELECT id, deal_id, owner_user_id, type, status_code, description, due_date, created_at " +
                    "FROM activities ORDER BY due_date DESC LIMIT ?";

    private static final String EXISTS_BY_DEAL =
            "SELECT COUNT(1) FROM activities WHERE deal_id = ?";

    private static final String FIND_BY_OWNER =
            "SELECT id, deal_id, owner_user_id, type, status_code, description, due_date, created_at " +
                    "FROM activities WHERE owner_user_id = ? ORDER BY due_date ASC";

    private static final String FIND_RECENT_BY_OWNER =
            "SELECT id, deal_id, owner_user_id, type, status_code, description, due_date, created_at " +
                    "FROM activities WHERE owner_user_id = ? ORDER BY due_date DESC LIMIT ?";

    private static final String FIND_RECENT_BY_DEAL_OWNER =
            "SELECT a.id, a.deal_id, a.owner_user_id, a.type, a.status_code, " +
                    "a.description, a.due_date, a.created_at " +
                    "FROM activities a JOIN deals d ON a.deal_id = d.id " +
                    "WHERE d.user_id = ? ORDER BY a.due_date DESC LIMIT ?";

    private static final String FIND_BY_MANAGER =
            "SELECT a.id, a.deal_id, a.owner_user_id, a.type, a.status_code, " +
                    "a.description, a.due_date, a.created_at " +
                    "FROM activities a " +
                    "JOIN deals d ON a.deal_id = d.id " +
                    "JOIN users u ON d.user_id = u.id " +
                    "WHERE u.manager_id = ? ORDER BY a.created_at DESC";

    private static final String FIND_BY_CONTACT =
            "SELECT a.id, a.deal_id, a.owner_user_id, a.type, a.status_code, " +
                    "a.description, a.due_date, a.created_at " +
                    "FROM activities a JOIN deals d ON a.deal_id = d.id " +
                    "WHERE d.contact_id = ? ORDER BY a.created_at DESC";

    private static final String FIND_BY_COMPANY =
            "SELECT a.id, a.deal_id, a.owner_user_id, a.type, a.status_code, " +
                    "a.description, a.due_date, a.created_at " +
                    "FROM activities a JOIN deals d ON a.deal_id = d.id " +
                    "WHERE d.company_id = ? ORDER BY a.created_at DESC";

    /* ============================
       CRUD METHODS
       ============================ */

    @Override
    public Optional<Activity> findById(Long id) {
        return jdbcTemplate.query(FIND_BY_ID, activityRowMapper(), id)
                .stream().findFirst();
    }

    @Override
    public List<Activity> findByDealId(Long dealId) {
        return jdbcTemplate.query(FIND_BY_DEAL, activityRowMapper(), dealId);
    }

    @Override
    public Long save(Activity activity) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    INSERT, Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, activity.getDeal().getId());
            ps.setLong(2, activity.getOwnerUserId());
            ps.setString(3, activity.getActivityType().name());
            ps.setString(4, activity.getStatusCode());
            ps.setString(5, activity.getDescription());

            if (activity.getDueDate() != null) {
                ps.setDate(6, java.sql.Date.valueOf(activity.getDueDate()));
            } else {
                ps.setNull(6, java.sql.Types.DATE);
            }

            ps.setTimestamp(7,
                    java.sql.Timestamp.valueOf(activity.getCreatedAt()));
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void update(Activity activity) {
        jdbcTemplate.update(
                UPDATE,
                activity.getActivityType().name(),
                activity.getStatusCode(),
                activity.getDescription(),
                activity.getDueDate() != null
                        ? java.sql.Date.valueOf(activity.getDueDate())
                        : null,
                activity.getId()
        );
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE, id);
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(EXISTS, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByDealId(Long dealId) {
        Integer count = jdbcTemplate.queryForObject(EXISTS_BY_DEAL, Integer.class, dealId);
        return count != null && count > 0;
    }

    /* ============================
       READ HELPERS
       ============================ */

    @Override
    public List<Activity> findRecentActivities(int limit) {
        return jdbcTemplate.query(FIND_RECENT, activityRowMapper(), limit);
    }

    @Override
    public List<Activity> findByOwner(Long ownerUserId) {
        return jdbcTemplate.query(FIND_BY_OWNER, activityRowMapper(), ownerUserId);
    }

    @Override
    public List<Activity> findRecentActivitiesByOwner(Long ownerUserId, int limit) {
        return jdbcTemplate.query(
                FIND_RECENT_BY_OWNER, activityRowMapper(), ownerUserId, limit
        );
    }

    @Override
    public List<Activity> findRecentByDealOwner(Long userId, int limit) {
        return jdbcTemplate.query(
                FIND_RECENT_BY_DEAL_OWNER, activityRowMapper(), userId, limit
        );
    }

    @Override
    public List<Activity> findByManagerId(Long managerId) {
        return jdbcTemplate.query(FIND_BY_MANAGER, activityRowMapper(), managerId);
    }

    @Override
    public List<Activity> findByContactId(Long contactId) {
        return jdbcTemplate.query(FIND_BY_CONTACT, activityRowMapper(), contactId);
    }

    @Override
    public List<Activity> findByCompanyId(Long companyId) {
        return jdbcTemplate.query(FIND_BY_COMPANY, activityRowMapper(), companyId);
    }

    /* ============================
       ROW MAPPER
       ============================ */

    private RowMapper<Activity> activityRowMapper() {
        return this::mapActivity;
    }

    private Activity mapActivity(ResultSet rs, int rowNum) throws SQLException {

        Deal deal = new Deal();
        deal.setId(rs.getLong("deal_id"));

        Activity activity = new Activity();
        activity.setId(rs.getLong("id"));
        activity.setDeal(deal);
        activity.setOwnerUserId(rs.getLong("owner_user_id"));
        activity.setActivityType(ActivityType.valueOf(rs.getString("type")));
        activity.setStatusCode(rs.getString("status_code"));
        activity.setDescription(rs.getString("description"));

        if (rs.getDate("due_date") != null) {
            activity.setDueDate(rs.getDate("due_date").toLocalDate());
        }

        activity.setCreatedAt(
                rs.getTimestamp("created_at").toLocalDateTime()
        );

        return activity;
    }

    @Override
    public List<Activity> findRecentByDealIds(List<Long> dealIds, int limit) {

        if (dealIds == null || dealIds.isEmpty()) {
            return List.of();
        }

        String sql = "SELECT a.* FROM activities a WHERE a.deal_id IN (:dealIds) ORDER BY a.created_at DESC LIMIT :limit";


        Map<String, Object> params = new HashMap<>();
        params.put("dealIds", dealIds);
        params.put("limit", limit);

        return namedJdbcTemplate.query(
                sql,
                params,
                activityRowMapper()
        );
    }


}
