package com.viswa.crm.repository.jdbc;

import com.viswa.crm.model.Role;
import com.viswa.crm.repository.RoleManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcRoleManagementRepository implements RoleManagementRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long save(Role role) {

        String sql = "INSERT INTO roles (role_name) VALUES (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps =
                    con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, role.getRoleName());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public Optional<Role> findById(Long roleId) {

        String sql = "SELECT id, role_name, created_at FROM roles WHERE id = ?";

        List<Role> roles = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Role r = new Role();
                    r.setId(rs.getLong("id"));
                    r.setRoleName(rs.getString("role_name"));
                    r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return r;
                },
                roleId
        );

        return roles.stream().findFirst();
    }

    @Override
    public List<Role> findAll() {

        String sql = "SELECT id, role_name, created_at FROM roles ORDER BY role_name";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Role r = new Role();
                    r.setId(rs.getLong("id"));
                    r.setRoleName(rs.getString("role_name"));
                    r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return r;
                }
        );
    }

    @Override
    public void deleteById(Long roleId) {

        deletePermissionsByRoleId(roleId);

        jdbcTemplate.update(
                "DELETE FROM roles WHERE id = ?",
                roleId
        );
    }

    @Override
    public void addPermissions(Long roleId, List<Long> permissionIds) {

        String sql =
                "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(
                sql,
                permissionIds,
                permissionIds.size(),
                (ps, permissionId) -> {
                    ps.setLong(1, roleId);
                    ps.setLong(2, permissionId);
                }
        );
    }

    @Override
    public void deletePermissionsByRoleId(Long roleId) {

        jdbcTemplate.update(
                "DELETE FROM role_permissions WHERE role_id = ?",
                roleId
        );
    }

    @Override
    public List<Long> findPermissionIdsByRoleId(Long roleId) {

        String sql =
                "SELECT permission_id FROM role_permissions WHERE role_id = ?";

        return jdbcTemplate.queryForList(
                sql,
                Long.class,
                roleId
        );
    }
}


