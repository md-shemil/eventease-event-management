package com.eventease.repository;

import com.eventease.model.Registration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class JdbcRegistrationRepository implements RegistrationRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcRegistrationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Registration> registrationRowMapper = (rs, rowNum) -> {
        Registration reg = new Registration();
        reg.setId(rs.getLong("id"));
        reg.setEventId(rs.getLong("event_id"));

        long userId = rs.getLong("user_id");
        if (!rs.wasNull()) {
            reg.setUserId(userId);
        }

        reg.setUserName(rs.getString("user_name"));
        reg.setUserEmail(rs.getString("user_email"));

        Timestamp ts = rs.getTimestamp("registration_date");
        if (ts != null) {
            reg.setRegistrationDate(ts.toLocalDateTime());
        }

        reg.setStatus(rs.getString("status"));
        return reg;
    };

    @Override
    public Optional<Registration> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, event_id, user_id, user_name, user_email, registration_date, status " +
                "FROM registrations WHERE id = ?";
        List<Registration> list = jdbcTemplate.query(sql, registrationRowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Registration> findAll() {
        String sql = "SELECT id, event_id, user_id, user_name, user_email, registration_date, status " +
                "FROM registrations ORDER BY registration_date DESC, id DESC";
        return jdbcTemplate.query(sql, registrationRowMapper);
    }

    @Override
    public List<Registration> findByEventId(Long eventId) {
        if (eventId == null) return new ArrayList<>();
        String sql = "SELECT id, event_id, user_id, user_name, user_email, registration_date, status " +
                "FROM registrations WHERE event_id = ? AND UPPER(status) != 'CANCELLED' " +
                "ORDER BY registration_date DESC, id DESC";
        return jdbcTemplate.query(sql, registrationRowMapper, eventId);
    }

    @Override
    public List<Registration> findByUserId(Long userId) {
        if (userId == null) return new ArrayList<>();
        String sql = "SELECT id, event_id, user_id, user_name, user_email, registration_date, status " +
                "FROM registrations WHERE user_id = ? AND UPPER(status) != 'CANCELLED' " +
                "ORDER BY registration_date DESC, id DESC";
        return jdbcTemplate.query(sql, registrationRowMapper, userId);
    }

    @Override
    public List<Registration> findByUserEmail(String userEmail) {
        if (userEmail == null) return new ArrayList<>();
        String sql = "SELECT id, event_id, user_id, user_name, user_email, registration_date, status " +
                "FROM registrations WHERE LOWER(user_email) = LOWER(?) AND UPPER(status) != 'CANCELLED' " +
                "ORDER BY registration_date DESC, id DESC";
        return jdbcTemplate.query(sql, registrationRowMapper, userEmail.trim());
    }

    @Override
    public boolean existsByEventIdAndUserId(Long eventId, Long userId) {
        if (eventId == null || userId == null) return false;
        String sql = "SELECT COUNT(*) FROM registrations WHERE event_id = ? AND user_id = ? AND UPPER(status) != 'CANCELLED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, eventId, userId);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEventIdAndUserEmail(Long eventId, String userEmail) {
        if (eventId == null || userEmail == null) return false;
        String sql = "SELECT COUNT(*) FROM registrations WHERE event_id = ? AND LOWER(user_email) = LOWER(?) AND UPPER(status) != 'CANCELLED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, eventId, userEmail.trim());
        return count != null && count > 0;
    }

    @Override
    public Optional<Registration> findByEventIdAndUserId(Long eventId, Long userId) {
        if (eventId == null || userId == null) return Optional.empty();
        String sql = "SELECT id, event_id, user_id, user_name, user_email, registration_date, status " +
                "FROM registrations WHERE event_id = ? AND user_id = ? AND UPPER(status) != 'CANCELLED'";
        List<Registration> list = jdbcTemplate.query(sql, registrationRowMapper, eventId, userId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<Registration> findByEventIdAndUserEmail(Long eventId, String userEmail) {
        if (eventId == null || userEmail == null) return Optional.empty();
        String sql = "SELECT id, event_id, user_id, user_name, user_email, registration_date, status " +
                "FROM registrations WHERE event_id = ? AND LOWER(user_email) = LOWER(?) AND UPPER(status) != 'CANCELLED'";
        List<Registration> list = jdbcTemplate.query(sql, registrationRowMapper, eventId, userEmail.trim());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public long countByEventId(Long eventId) {
        if (eventId == null) return 0L;
        String sql = "SELECT COUNT(*) FROM registrations WHERE event_id = ? AND UPPER(status) != 'CANCELLED'";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, eventId);
        return count != null ? count : 0L;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM registrations WHERE UPPER(status) != 'CANCELLED'";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public Registration save(Registration registration) {
        if (registration.getRegistrationDate() == null) {
            registration.setRegistrationDate(LocalDateTime.now());
        }
        if (registration.getStatus() == null || registration.getStatus().isBlank()) {
            registration.setStatus("CONFIRMED");
        }

        if (registration.getId() == null) {
            String sql = "INSERT INTO registrations (event_id, user_id, user_name, user_email, registration_date, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, registration.getEventId());
                if (registration.getUserId() != null) {
                    ps.setLong(2, registration.getUserId());
                } else {
                    ps.setNull(2, Types.BIGINT);
                }
                ps.setString(3, registration.getUserName());
                ps.setString(4, registration.getUserEmail());
                ps.setTimestamp(5, Timestamp.valueOf(registration.getRegistrationDate()));
                ps.setString(6, registration.getStatus());
                return ps;
            }, keyHolder);

            Number key = extractGeneratedId(keyHolder);
            if (key != null) {
                registration.setId(key.longValue());
            }
        } else {
            String checkSql = "SELECT COUNT(*) FROM registrations WHERE id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, registration.getId());
            if (count != null && count > 0) {
                String updateSql = "UPDATE registrations SET event_id = ?, user_id = ?, user_name = ?, " +
                        "user_email = ?, status = ? WHERE id = ?";
                jdbcTemplate.update(updateSql,
                        registration.getEventId(),
                        registration.getUserId(),
                        registration.getUserName(),
                        registration.getUserEmail(),
                        registration.getStatus(),
                        registration.getId()
                );
            } else {
                String insertSql = "INSERT INTO registrations (id, event_id, user_id, user_name, user_email, registration_date, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(insertSql,
                        registration.getId(),
                        registration.getEventId(),
                        registration.getUserId(),
                        registration.getUserName(),
                        registration.getUserEmail(),
                        Timestamp.valueOf(registration.getRegistrationDate()),
                        registration.getStatus()
                );
            }
        }
        return registration;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) return;
        String sql = "DELETE FROM registrations WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteByEventIdAndUserId(Long eventId, Long userId) {
        if (eventId == null || userId == null) return;
        String sql = "DELETE FROM registrations WHERE event_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, eventId, userId);
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
