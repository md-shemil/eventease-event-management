package com.eventease.repository;

import com.eventease.model.Category;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class JdbcCategoryRepository implements CategoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcCategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Category> categoryRowMapper = (rs, rowNum) -> {
        Category category = new Category();
        category.setId(rs.getLong("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setIcon(rs.getString("icon"));
        return category;
    };

    @Override
    public Optional<Category> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, name, description, icon FROM categories WHERE id = ?";
        List<Category> list = jdbcTemplate.query(sql, categoryRowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public Optional<Category> findByName(String name) {
        if (name == null) return Optional.empty();
        String sql = "SELECT id, name, description, icon FROM categories WHERE LOWER(name) = LOWER(?)";
        List<Category> list = jdbcTemplate.query(sql, categoryRowMapper, name.trim());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Category> findAll() {
        String sql = "SELECT id, name, description, icon FROM categories ORDER BY id ASC";
        return jdbcTemplate.query(sql, categoryRowMapper);
    }

    @Override
    public Category save(Category category) {
        if (category.getId() == null) {
            String sql = "INSERT INTO categories (name, description, icon) VALUES (?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, category.getName());
                ps.setString(2, category.getDescription());
                ps.setString(3, category.getIcon());
                return ps;
            }, keyHolder);

            Number key = extractGeneratedId(keyHolder);
            if (key != null) {
                category.setId(key.longValue());
            }
        } else {
            String checkSql = "SELECT COUNT(*) FROM categories WHERE id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, category.getId());
            if (count != null && count > 0) {
                String updateSql = "UPDATE categories SET name = ?, description = ?, icon = ? WHERE id = ?";
                jdbcTemplate.update(updateSql, category.getName(), category.getDescription(), category.getIcon(), category.getId());
            } else {
                String insertSql = "INSERT INTO categories (id, name, description, icon) VALUES (?, ?, ?, ?)";
                jdbcTemplate.update(insertSql, category.getId(), category.getName(), category.getDescription(), category.getIcon());
            }
        }
        return category;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) return;
        String sql = "DELETE FROM categories WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) return false;
        String sql = "SELECT COUNT(*) FROM categories WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
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
