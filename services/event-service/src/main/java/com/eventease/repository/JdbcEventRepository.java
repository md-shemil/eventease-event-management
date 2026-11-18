package com.eventease.repository;

import com.eventease.model.Event;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class JdbcEventRepository implements EventRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Event> eventRowMapper = (rs, rowNum) -> {
        Event event = new Event();
        event.setId(rs.getLong("id"));
        event.setName(rs.getString("name"));
        event.setDescription(rs.getString("description"));
        event.setVenue(rs.getString("venue"));
        Date d = rs.getDate("event_date");
        if (d != null) {
            event.setDate(d.toLocalDate());
        }
        event.setTime(rs.getString("event_time"));
        event.setCapacity(rs.getInt("capacity"));
        event.setRegisteredCount(rs.getInt("registered_count"));

        long categoryId = rs.getLong("category_id");
        if (!rs.wasNull()) {
            event.setCategoryId(categoryId);
        }

        long organizerId = rs.getLong("organizer_id");
        if (!rs.wasNull()) {
            event.setOrganizerId(organizerId);
        }

        event.setBannerTheme(rs.getString("banner_theme"));
        event.setBannerIcon(rs.getString("banner_icon"));
        event.setStatus(rs.getString("status"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            event.setCreatedAt(ts.toLocalDateTime());
        }
        return event;
    };

    @Override
    public Optional<Event> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, name, description, venue, event_date, event_time, capacity, registered_count, " +
                "category_id, organizer_id, banner_theme, banner_icon, status, created_at " +
                "FROM events WHERE id = ?";
        List<Event> list = jdbcTemplate.query(sql, eventRowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Event> findAll() {
        String sql = "SELECT id, name, description, venue, event_date, event_time, capacity, registered_count, " +
                "category_id, organizer_id, banner_theme, banner_icon, status, created_at " +
                "FROM events ORDER BY event_date ASC, id ASC";
        return jdbcTemplate.query(sql, eventRowMapper);
    }

    @Override
    public List<Event> findByCategoryId(Long categoryId) {
        if (categoryId == null) return findAll();
        String sql = "SELECT id, name, description, venue, event_date, event_time, capacity, registered_count, " +
                "category_id, organizer_id, banner_theme, banner_icon, status, created_at " +
                "FROM events WHERE category_id = ? ORDER BY event_date ASC, id ASC";
        return jdbcTemplate.query(sql, eventRowMapper, categoryId);
    }

    @Override
    public List<Event> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        String pattern = "%" + query.trim().toLowerCase() + "%";
        String sql = "SELECT id, name, description, venue, event_date, event_time, capacity, registered_count, " +
                "category_id, organizer_id, banner_theme, banner_icon, status, created_at " +
                "FROM events WHERE LOWER(name) LIKE ? OR LOWER(venue) LIKE ? OR LOWER(description) LIKE ? " +
                "ORDER BY event_date ASC, id ASC";
        return jdbcTemplate.query(sql, eventRowMapper, pattern, pattern, pattern);
    }

    @Override
    public Event save(Event event) {
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(LocalDateTime.now());
        }
        if (event.getTime() == null || event.getTime().isBlank()) {
            event.setTime("10:00 AM");
        }
        if (event.getStatus() == null || event.getStatus().isBlank()) {
            event.setStatus("UPCOMING");
        }
        if (event.getBannerTheme() == null) {
            event.setBannerTheme("purple");
        }
        if (event.getBannerIcon() == null) {
            event.setBannerIcon("✦");
        }

        if (event.getId() == null) {
            String sql = "INSERT INTO events (name, description, venue, event_date, event_time, capacity, " +
                    "registered_count, category_id, organizer_id, banner_theme, banner_icon, status, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, event.getName());
                ps.setString(2, event.getDescription());
                ps.setString(3, event.getVenue());
                ps.setDate(4, event.getDate() != null ? Date.valueOf(event.getDate()) : null);
                ps.setString(5, event.getTime());
                ps.setInt(6, event.getCapacity());
                ps.setInt(7, event.getRegisteredCount());
                if (event.getCategoryId() != null) {
                    ps.setLong(8, event.getCategoryId());
                } else {
                    ps.setNull(8, Types.BIGINT);
                }
                if (event.getOrganizerId() != null) {
                    ps.setLong(9, event.getOrganizerId());
                } else {
                    ps.setNull(9, Types.BIGINT);
                }
                ps.setString(10, event.getBannerTheme());
                ps.setString(11, event.getBannerIcon());
                ps.setString(12, event.getStatus());
                ps.setTimestamp(13, Timestamp.valueOf(event.getCreatedAt()));
                return ps;
            }, keyHolder);

            Number key = extractGeneratedId(keyHolder);
            if (key != null) {
                event.setId(key.longValue());
            }
        } else {
            String checkSql = "SELECT COUNT(*) FROM events WHERE id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, event.getId());
            if (count != null && count > 0) {
                String updateSql = "UPDATE events SET name = ?, description = ?, venue = ?, event_date = ?, " +
                        "event_time = ?, capacity = ?, registered_count = ?, category_id = ?, organizer_id = ?, " +
                        "banner_theme = ?, banner_icon = ?, status = ? WHERE id = ?";
                jdbcTemplate.update(updateSql,
                        event.getName(),
                        event.getDescription(),
                        event.getVenue(),
                        event.getDate() != null ? Date.valueOf(event.getDate()) : null,
                        event.getTime(),
                        event.getCapacity(),
                        event.getRegisteredCount(),
                        event.getCategoryId(),
                        event.getOrganizerId(),
                        event.getBannerTheme(),
                        event.getBannerIcon(),
                        event.getStatus(),
                        event.getId()
                );
            } else {
                String insertSql = "INSERT INTO events (id, name, description, venue, event_date, event_time, " +
                        "capacity, registered_count, category_id, organizer_id, banner_theme, banner_icon, status, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(insertSql,
                        event.getId(),
                        event.getName(),
                        event.getDescription(),
                        event.getVenue(),
                        event.getDate() != null ? Date.valueOf(event.getDate()) : null,
                        event.getTime(),
                        event.getCapacity(),
                        event.getRegisteredCount(),
                        event.getCategoryId(),
                        event.getOrganizerId(),
                        event.getBannerTheme(),
                        event.getBannerIcon(),
                        event.getStatus(),
                        Timestamp.valueOf(event.getCreatedAt())
                );
            }
        }
        return event;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) return;
        String sql = "DELETE FROM events WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) return false;
        String sql = "SELECT COUNT(*) FROM events WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM events";
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
