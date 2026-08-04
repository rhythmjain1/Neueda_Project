package com.neueda.tms.repository.auth;

import com.neueda.tms.repository.auth.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * JDBC-based repository for User entities.
 * All queries use parameterized statements to prevent SQL injection.
 * Passwords are stored as BCrypt hashes — never plaintext.
 */
@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ── RowMapper ──────────────────────────────────────────────────────────────

    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        String role = rs.getString("role");
        if (role != null) {
            user.setRole(User.UserRole.valueOf(role));
        }
        user.setIsActive(rs.getBoolean("is_active"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        user.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        return user;
    };

    // ── Write Operations ───────────────────────────────────────────────────────

    public User save(User user) {
        if (user.getId() == null) {
            return insert(user);
        } else {
            update(user);
            return user;
        }
    }

    private User insert(User user) {
        String sql = """
                INSERT INTO users (username, password_hash, role, is_active, created_at)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole() != null ? user.getRole().name() : User.UserRole.ANALYST.name());
            ps.setBoolean(4, user.getIsActive() != null && user.getIsActive());
            ps.setTimestamp(5, Timestamp.valueOf(
                    user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) user.setId(key.longValue());
        return user;
    }

    private void update(User user) {
        String sql = """
                UPDATE users SET username = ?, password_hash = ?,
                    role = ?, is_active = ? WHERE id = ?
                """;
        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPasswordHash(),
                user.getRole() != null ? user.getRole().name() : User.UserRole.ANALYST.name(),
                user.getIsActive() != null && user.getIsActive(),
                user.getId());
    }

    // ── Read Operations ────────────────────────────────────────────────────────

    public Optional<User> findByUsername(String username) {
        try {
            User user = jdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE username = ?", ROW_MAPPER, username);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean existsByUsername(String username) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?", Long.class, username);
        return count != null && count > 0;
    }
}
