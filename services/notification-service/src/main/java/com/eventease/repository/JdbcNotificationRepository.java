package com.eventease.repository;

import com.eventease.model.Notification;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class JdbcNotificationRepository implements NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Notification> notificationRowMapper = (rs, rowNum) -> {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        n.setUserId(rs.getLong("user_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getBoolean("is_read"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            n.setCreatedAt(ts.toLocalDateTime());
        }
        return n;
    };

    @Override
    public Optional<Notification> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, user_id, title, message, is_read, created_at FROM notifications WHERE id = ?";
        List<Notification> list = jdbcTemplate.query(sql, notificationRowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Notification> findByUserId(Long userId) {
        if (userId == null) return new ArrayList<>();
        String sql = "SELECT id, user_id, title, message, is_read, created_at " +
                "FROM notifications WHERE user_id = ? ORDER BY created_at DESC, id DESC";
        return jdbcTemplate.query(sql, notificationRowMapper, userId);
    }

    @Override
    public Notification save(Notification notification) {
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }

        if (notification.getId() == null) {
            String sql = "INSERT INTO notifications (user_id, title, message, is_read, created_at) " +
                    "VALUES (?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, notification.getUserId());
                ps.setString(2, notification.getTitle());
                ps.setString(3, notification.getMessage());
                ps.setBoolean(4, notification.isRead());
                ps.setTimestamp(5, Timestamp.valueOf(notification.getCreatedAt()));
                return ps;
            }, keyHolder);

            Number key = extractGeneratedId(keyHolder);
            if (key != null) {
                notification.setId(key.longValue());
            }
        } else {
            String checkSql = "SELECT COUNT(*) FROM notifications WHERE id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, notification.getId());
            if (count != null && count > 0) {
                String updateSql = "UPDATE notifications SET user_id = ?, title = ?, message = ?, is_read = ? WHERE id = ?";
                jdbcTemplate.update(updateSql,
                        notification.getUserId(),
                        notification.getTitle(),
                        notification.getMessage(),
                        notification.isRead(),
                        notification.getId()
                );
            } else {
                String insertSql = "INSERT INTO notifications (id, user_id, title, message, is_read, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(insertSql,
                        notification.getId(),
                        notification.getUserId(),
                        notification.getTitle(),
                        notification.getMessage(),
                        notification.isRead(),
                        Timestamp.valueOf(notification.getCreatedAt())
                );
            }
        }
        return notification;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) return;
        String sql = "DELETE FROM notifications WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public long countUnreadByUserId(Long userId) {
        if (userId == null) return 0L;
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userId);
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
