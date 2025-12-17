package com.viswa.crm.repository.jdbc;

import com.viswa.crm.model.*;
import com.viswa.crm.repository.DashboardRepository;
import com.viswa.crm.repository.RecentProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class JdbcDashboardRepository implements DashboardRepository {

    private final JdbcTemplate jdbcTemplate;

    /* =========================================================
       DEAL COUNTS
       ========================================================= */

    private static final String COUNT_DEALS_ADMIN =
            "SELECT COUNT(*) FROM deals";

    private static final String COUNT_DEALS_SALES =
            "SELECT COUNT(*) FROM deals WHERE user_id = ?";

    private static final String COUNT_DEALS_MANAGER =
            "SELECT COUNT(*) FROM deals d " +
                    "JOIN users u ON d.user_id = u.id " +
                    "WHERE u.manager_id = ?";

    /* =========================================================
       COMPANY / CONTACT / USER COUNTS (ADMIN)
       ========================================================= */

    private static final String COUNT_COMPANIES =
            "SELECT COUNT(*) FROM companies";

    private static final String COUNT_CONTACTS =
            "SELECT COUNT(*) FROM contacts";

    private static final String COUNT_USERS =
            "SELECT COUNT(*) FROM users";

    /* =========================================================
       PENDING ACTIVITIES
       ========================================================= */

    private static final String COUNT_PENDING_ACTIVITIES_ADMIN =
            "SELECT COUNT(*) FROM activities WHERE status_code = 'PENDING'";

    private static final String COUNT_PENDING_ACTIVITIES_SALES =
            "SELECT COUNT(*) FROM activities " +
                    "WHERE owner_user_id = ? AND status_code = 'PENDING'";

    private static final String COUNT_PENDING_ACTIVITIES_MANAGER =
            "SELECT COUNT(*) FROM activities a " +
                    "JOIN deals d ON a.deal_id = d.id " +
                    "JOIN users u ON d.user_id = u.id " +
                    "WHERE u.manager_id = ? AND a.status_code = 'PENDING'";

    /* =========================================================
       DEAL STATUS PIPELINE
       ========================================================= */

    private static final String DEALS_BY_STATUS_ADMIN =
            "SELECT status, COUNT(*) cnt FROM deals GROUP BY status";

    private static final String DEALS_BY_STATUS_SALES =
            "SELECT status, COUNT(*) cnt FROM deals " +
                    "WHERE user_id = ? GROUP BY status";

    private static final String DEALS_BY_STATUS_MANAGER =
            "SELECT d.status, COUNT(*) cnt FROM deals d " +
                    "JOIN users u ON d.user_id = u.id " +
                    "WHERE u.manager_id = ? GROUP BY d.status";

    /* =========================================================
       PIPELINE AMOUNT
       ========================================================= */

    private static final String PIPELINE_SUM_ADMIN =
            "SELECT COALESCE(SUM(amount),0) FROM deals " +
                    "WHERE status <> 'CLOSED'";

    private static final String PIPELINE_SUM_SALES =
            "SELECT COALESCE(SUM(amount),0) FROM deals " +
                    "WHERE user_id = ? AND status <> 'CLOSED'";

    private static final String PIPELINE_SUM_MANAGER =
            "SELECT COALESCE(SUM(d.amount),0) FROM deals d " +
                    "JOIN users u ON d.user_id = u.id " +
                    "WHERE u.manager_id = ? AND d.status <> 'CLOSED'";

    /* =========================================================
       OVERDUE / COMPLETED ACTIVITIES
       ========================================================= */

    private static final String COUNT_OVERDUE_ADMIN =
            "SELECT COUNT(*) FROM activities " +
                    "WHERE status_code = 'PENDING' AND due_date < CURRENT_DATE";

    private static final String COUNT_OVERDUE_SALES =
            "SELECT COUNT(*) FROM activities " +
                    "WHERE owner_user_id = ? AND status_code = 'PENDING' AND due_date < CURRENT_DATE";

    private static final String COUNT_OVERDUE_MANAGER =
            "SELECT COUNT(*) FROM activities a " +
                    "JOIN deals d ON a.deal_id = d.id " +
                    "JOIN users u ON d.user_id = u.id " +
                    "WHERE u.manager_id = ? AND a.status_code = 'PENDING' AND a.due_date < CURRENT_DATE";

    private static final String COUNT_COMPLETED_ADMIN =
            "SELECT COUNT(*) FROM activities WHERE status_code = 'DONE'";

    private static final String COUNT_COMPLETED_SALES =
            "SELECT COUNT(*) FROM activities WHERE owner_user_id = ? AND status_code = 'DONE'";

    private static final String COUNT_COMPLETED_MANAGER =
            "SELECT COUNT(*) FROM activities a " +
                    "JOIN deals d ON a.deal_id = d.id " +
                    "JOIN users u ON d.user_id = u.id " +
                    "WHERE u.manager_id = ? AND a.status_code = 'DONE'";

    /* =========================================================
       RECENT DEALS
       ========================================================= */

    private static final String RECENT_DEALS_ADMIN =
            "SELECT id, title, created_at FROM deals " +
                    "ORDER BY created_at DESC LIMIT ?";

    private static final String RECENT_DEALS_SALES =
            "SELECT id, title, created_at FROM deals " +
                    "WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";

    private static final String RECENT_DEALS_MANAGER =
            "SELECT d.id, d.title, d.created_at FROM deals d " +
                    "JOIN users u ON d.user_id = u.id " +
                    "WHERE u.manager_id = ? ORDER BY d.created_at DESC LIMIT ?";

    /* =========================================================
       RECENT CONTACTS
       ========================================================= */

    private static final String RECENT_CONTACTS =
            "SELECT id, name AS title, created_at FROM contacts " +
                    "ORDER BY created_at DESC LIMIT ?";

    private static final String RECENT_ACTIVITIES_ADMIN =
            "SELECT id, deal_id, owner_user_id, type, status_code, description, due_date, created_at " +
                    "FROM activities ORDER BY created_at DESC LIMIT ?";

    private static final String RECENT_ACTIVITIES_SALES =
            "SELECT id, deal_id, owner_user_id, type, status_code, description, due_date, created_at " +
                    "FROM activities WHERE owner_user_id = ? " +
                    "ORDER BY created_at DESC LIMIT ?";

    private static final String RECENT_ACTIVITIES_MANAGER =
            "SELECT a.id, a.deal_id, a.owner_user_id, a.type, a.status_code, " +
                    "a.description, a.due_date, a.created_at " +
                    "FROM activities a " +
                    "JOIN deals d ON a.deal_id = d.id " +
                    "JOIN users u ON d.user_id = u.id " +
                    "WHERE u.manager_id = ? " +
                    "ORDER BY a.created_at DESC LIMIT ?";

    /* =========================================================
       IMPLEMENTATION
       ========================================================= */

    @Override
    public long countDealsForAdmin() {
        return jdbcTemplate.queryForObject(COUNT_DEALS_ADMIN, Long.class);
    }

    @Override
    public long countDealsForSales(Long userId) {
        return jdbcTemplate.queryForObject(COUNT_DEALS_SALES, Long.class, userId);
    }

    @Override
    public long countDealsForManager(Long managerId) {
        return jdbcTemplate.queryForObject(COUNT_DEALS_MANAGER, Long.class, managerId);
    }

    @Override
    public long countCompanies() {
        return jdbcTemplate.queryForObject(COUNT_COMPANIES, Long.class);
    }

    @Override
    public long countContacts() {
        return jdbcTemplate.queryForObject(COUNT_CONTACTS, Long.class);
    }

    @Override
    public long countUsers() {
        return jdbcTemplate.queryForObject(COUNT_USERS, Long.class);
    }

    @Override
    public long countPendingActivitiesForAdmin() {
        return jdbcTemplate.queryForObject(COUNT_PENDING_ACTIVITIES_ADMIN, Long.class);
    }

    @Override
    public long countPendingActivitiesForSales(Long userId) {
        return jdbcTemplate.queryForObject(
                COUNT_PENDING_ACTIVITIES_SALES,
                Long.class,
                userId
        );
    }

    @Override
    public long countPendingActivitiesForManager(Long managerId) {
        return jdbcTemplate.queryForObject(
                COUNT_PENDING_ACTIVITIES_MANAGER,
                Long.class,
                managerId
        );
    }

    @Override
    public Map<DealStatus, Long> countDealsByStatusForAdmin() {
        return fetchDealStatusCounts(DEALS_BY_STATUS_ADMIN);
    }

    @Override
    public Map<DealStatus, Long> countDealsByStatusForSales(Long userId) {
        return fetchDealStatusCounts(DEALS_BY_STATUS_SALES, userId);
    }

    @Override
    public Map<DealStatus, Long> countDealsByStatusForManager(Long managerId) {
        return fetchDealStatusCounts(DEALS_BY_STATUS_MANAGER, managerId);
    }

    @Override
    public BigDecimal sumPipelineAmountForAdmin() {
        return jdbcTemplate.queryForObject(PIPELINE_SUM_ADMIN, BigDecimal.class);
    }

    @Override
    public BigDecimal sumPipelineAmountForSales(Long userId) {
        return jdbcTemplate.queryForObject(
                PIPELINE_SUM_SALES,
                BigDecimal.class,
                userId
        );
    }

    @Override
    public BigDecimal sumPipelineAmountForManager(Long managerId) {
        return jdbcTemplate.queryForObject(
                PIPELINE_SUM_MANAGER,
                BigDecimal.class,
                managerId
        );
    }

    @Override
    public long countOverdueActivitiesForAdmin() {
        return jdbcTemplate.queryForObject(COUNT_OVERDUE_ADMIN, Long.class);
    }

    @Override
    public long countOverdueActivitiesForSales(Long userId) {
        return jdbcTemplate.queryForObject(
                COUNT_OVERDUE_SALES,
                Long.class,
                userId
        );
    }

    @Override
    public long countOverdueActivitiesForManager(Long managerId) {
        return jdbcTemplate.queryForObject(
                COUNT_OVERDUE_MANAGER,
                Long.class,
                managerId
        );
    }

    @Override
    public long countCompletedActivitiesForAdmin() {
        return jdbcTemplate.queryForObject(COUNT_COMPLETED_ADMIN, Long.class);
    }

    @Override
    public long countCompletedActivitiesForSales(Long userId) {
        return jdbcTemplate.queryForObject(
                COUNT_COMPLETED_SALES,
                Long.class,
                userId
        );
    }

    @Override
    public long countCompletedActivitiesForManager(Long managerId) {
        return jdbcTemplate.queryForObject(
                COUNT_COMPLETED_MANAGER,
                Long.class,
                managerId
        );
    }

    @Override
    public List<RecentProjection> findRecentDealsForAdmin(int limit) {
        return jdbcTemplate.query(
                RECENT_DEALS_ADMIN,
                recentRowMapper(),
                limit
        );
    }

    @Override
    public List<RecentProjection> findRecentDealsForSales(Long userId, int limit) {
        return jdbcTemplate.query(
                RECENT_DEALS_SALES,
                recentRowMapper(),
                userId,
                limit
        );
    }

    @Override
    public List<RecentProjection> findRecentDealsForManager(Long managerId, int limit) {
        return jdbcTemplate.query(
                RECENT_DEALS_MANAGER,
                recentRowMapper(),
                managerId,
                limit
        );
    }

    @Override
    public List<RecentProjection> findRecentContacts(int limit) {
        return jdbcTemplate.query(
                RECENT_CONTACTS,
                recentRowMapper(),
                limit
        );
    }

    /* =========================================================
       HELPERS
       ========================================================= */

    private Map<DealStatus, Long> fetchDealStatusCounts(
            String sql,
            Object... args
    ) {
        Map<DealStatus, Long> map = new EnumMap<>(DealStatus.class);
        jdbcTemplate.query(sql, args, rs -> {
            map.put(
                    DealStatus.valueOf(rs.getString("status")),
                    rs.getLong("cnt")
            );
        });
        return map;
    }

    private RowMapper<RecentProjection> recentRowMapper() {
        return (rs, rowNum) ->
                new RecentProjectionImpl(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
    }
    @Override
    public List<Activity> findRecentActivitiesForAdmin(int limit) {
        return jdbcTemplate.query(
                RECENT_ACTIVITIES_ADMIN,
                activityRowMapper(),
                limit
        );
    }

    @Override
    public List<Activity> findRecentActivitiesForSales(Long userId, int limit) {
        return jdbcTemplate.query(
                RECENT_ACTIVITIES_SALES,
                activityRowMapper(),
                userId,
                limit
        );
    }

    @Override
    public List<Activity> findRecentActivitiesForManager(Long managerId, int limit) {
        return jdbcTemplate.query(
                RECENT_ACTIVITIES_MANAGER,
                activityRowMapper(),
                managerId,
                limit
        );
    }
    private RowMapper<Activity> activityRowMapper() {
        return (rs, rowNum) -> {

            Deal deal = new Deal();
            deal.setId(rs.getLong("deal_id"));

            Activity activity = new Activity();
            activity.setId(rs.getLong("id"));
            activity.setDeal(deal);

            activity.setOwnerUserId(
                    rs.getLong("owner_user_id")
            );

            activity.setActivityType(
                    ActivityType.valueOf(rs.getString("type"))
            );

            activity.setStatusCode(
                    rs.getString("status_code")
            );

            activity.setDescription(
                    rs.getString("description")
            );

            if (rs.getDate("due_date") != null) {
                activity.setDueDate(
                        rs.getDate("due_date").toLocalDate()
                );
            }

            activity.setCreatedAt(
                    rs.getTimestamp("created_at").toLocalDateTime()
            );

            return activity;
        };
    }

}
