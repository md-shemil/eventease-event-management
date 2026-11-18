package com.eventease.repository;

import com.eventease.model.Role;
import com.eventease.model.User;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        String roleStr = rs.getString("role");
        if (roleStr != null) {
            try {
                user.setRole(Role.valueOf(roleStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                user.setRole(Role.USER);
            }
        }
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            user.setCreatedAt(ts.toLocalDateTime());
        }
        return user;
    };

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, name, email, password, role, created_at FROM users WHERE id = ?";
        List<User> list = jdbcTemplate.query(sql, userRowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String sql = "SELECT id, name, email, password, role, created_at FROM users WHERE LOWER(email) = LOWER(?)";
        List<User> list = jdbcTemplate.query(sql, userRowMapper, email.trim());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) return false;
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email.trim());
        return count != null && count > 0;
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT id, name, email, password, role, created_at FROM users ORDER BY id ASC";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public User save(User user) {
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        String roleName = user.getRole() != null ? user.getRole().name() : Role.USER.name();

        if (user.getId() == null) {
            String sql = "INSERT INTO users (name, email, password, role, created_at) VALUES (?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, user.getName());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPassword());
                ps.setString(4, roleName);
                ps.setTimestamp(5, Timestamp.valueOf(user.getCreatedAt()));
                return ps;
            }, keyHolder);

            Number key = extractGeneratedId(keyHolder);
            if (key != null) {
                user.setId(key.longValue());
            }
        } else {
            String checkSql = "SELECT COUNT(*) FROM users WHERE id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, user.getId());
            if (count != null && count > 0) {
                String updateSql = "UPDATE users SET name = ?, email = ?, password = ?, role = ? WHERE id = ?";
                jdbcTemplate.update(updateSql, user.getName(), user.getEmail(), user.getPassword(), roleName, user.getId());
            } else {
                String insertSql = "INSERT INTO users (id, name, email, password, role, created_at) VALUES (?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(insertSql, user.getId(), user.getName(), user.getEmail(), user.getPassword(), roleName,
                        Timestamp.valueOf(user.getCreatedAt()));
            }
        }
        return user;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) return;
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM users";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    private Number extractGeneratedId(KeyHolder keyHolder) {
        if (keyHolder.getKeys() != null) {
            Object idVal = keyHolder.getKeys().get("id");
            if (idVal == null) idVal = keyHolder.getKeys().get("ID");
            if (idVal instanceof Number) return (Number) idVal;
        }
        try {
            return keyHolder.getKey();
        } catch (Exception ignored) {
            return null;
        }
    }
}
