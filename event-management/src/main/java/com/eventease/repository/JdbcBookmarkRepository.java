package com.eventease.repository;

import com.eventease.model.Bookmark;
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
public class JdbcBookmarkRepository implements BookmarkRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcBookmarkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Bookmark> bookmarkRowMapper = (rs, rowNum) -> {
        Bookmark bookmark = new Bookmark();
        bookmark.setId(rs.getLong("id"));
        bookmark.setUserId(rs.getLong("user_id"));
        bookmark.setEventId(rs.getLong("event_id"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            bookmark.setCreatedAt(ts.toLocalDateTime());
        }
        return bookmark;
    };

    @Override
    public Optional<Bookmark> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, user_id, event_id, created_at FROM bookmarks WHERE id = ?";
        List<Bookmark> list = jdbcTemplate.query(sql, bookmarkRowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Bookmark> findByUserId(Long userId) {
        if (userId == null) return new ArrayList<>();
        String sql = "SELECT id, user_id, event_id, created_at FROM bookmarks WHERE user_id = ? ORDER BY created_at DESC, id DESC";
        return jdbcTemplate.query(sql, bookmarkRowMapper, userId);
    }

    @Override
    public boolean existsByUserIdAndEventId(Long userId, Long eventId) {
        if (userId == null || eventId == null) return false;
        String sql = "SELECT COUNT(*) FROM bookmarks WHERE user_id = ? AND event_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, eventId);
        return count != null && count > 0;
    }

    @Override
    public Optional<Bookmark> findByUserIdAndEventId(Long userId, Long eventId) {
        if (userId == null || eventId == null) return Optional.empty();
        String sql = "SELECT id, user_id, event_id, created_at FROM bookmarks WHERE user_id = ? AND event_id = ?";
        List<Bookmark> list = jdbcTemplate.query(sql, bookmarkRowMapper, userId, eventId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Bookmark save(Bookmark bookmark) {
        if (bookmark.getCreatedAt() == null) {
            bookmark.setCreatedAt(LocalDateTime.now());
        }

        if (bookmark.getId() == null) {
            String sql = "INSERT INTO bookmarks (user_id, event_id, created_at) VALUES (?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, bookmark.getUserId());
                ps.setLong(2, bookmark.getEventId());
                ps.setTimestamp(3, Timestamp.valueOf(bookmark.getCreatedAt()));
                return ps;
            }, keyHolder);

            Number key = extractGeneratedId(keyHolder);
            if (key != null) {
                bookmark.setId(key.longValue());
            }
        } else {
            String checkSql = "SELECT COUNT(*) FROM bookmarks WHERE id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, bookmark.getId());
            if (count != null && count > 0) {
                String updateSql = "UPDATE bookmarks SET user_id = ?, event_id = ? WHERE id = ?";
                jdbcTemplate.update(updateSql, bookmark.getUserId(), bookmark.getEventId(), bookmark.getId());
            } else {
                String insertSql = "INSERT INTO bookmarks (id, user_id, event_id, created_at) VALUES (?, ?, ?, ?)";
                jdbcTemplate.update(insertSql, bookmark.getId(), bookmark.getUserId(), bookmark.getEventId(),
                        Timestamp.valueOf(bookmark.getCreatedAt()));
            }
        }
        return bookmark;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) return;
        String sql = "DELETE FROM bookmarks WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteByUserIdAndEventId(Long userId, Long eventId) {
        if (userId == null || eventId == null) return;
        String sql = "DELETE FROM bookmarks WHERE user_id = ? AND event_id = ?";
        jdbcTemplate.update(sql, userId, eventId);
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
