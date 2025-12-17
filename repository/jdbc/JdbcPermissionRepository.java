package com.viswa.crm.repository.jdbc;

import com.viswa.crm.model.Permission;
import com.viswa.crm.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcPermissionRepository implements PermissionRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String HAS_PERMISSION =
            "SELECT COUNT(1) " +
                    "FROM users u " +
                    "JOIN role_permissions rp ON u.role_id = rp.role_id " +
                    "JOIN permissions p ON rp.permission_id = p.id " +
                    "WHERE u.id = ? AND p.permission_code = ?";

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        Integer count = jdbcTemplate.queryForObject(
                HAS_PERMISSION,
                Integer.class,
                userId,
                permissionCode
        );
        return count != null && count > 0;
    }

    @Override
    public List<Permission> findAll() {
        return jdbcTemplate.query(
                "SELECT id, permission_code, description FROM permissions",
                (rs, i) -> new Permission(
                        rs.getLong("id"),
                        rs.getString("permission_code"),
                        rs.getString("description")
                )
        );
    }


    @Override
    public List<Permission> findByIds(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        String placeholders =
                ids.stream()
                        .map(i -> "?")
                        .collect(Collectors.joining(","));

        String sql =
                "SELECT id, permission_code, description " +
                        "FROM permissions WHERE id IN (" + placeholders + ")";

        return jdbcTemplate.query(
                sql,
                ids.toArray(),
                (rs, rowNum) -> {
                    Permission p = new Permission();
                    p.setId(rs.getLong("id"));
                    p.setPermissionCode(rs.getString("permission_code"));
                    p.setDescription(rs.getString("description"));
                    return p;
                }
        );
    }

    @Override
    public boolean existsByCode(String permissionCode) {

        Integer count =
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM permissions WHERE permission_code = ?",
                        Integer.class,
                        permissionCode
                );

        return count != null && count > 0;
    }
}
