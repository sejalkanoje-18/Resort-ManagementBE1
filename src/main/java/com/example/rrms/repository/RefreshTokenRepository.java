package com.example.rrms.repository;

import com.example.rrms.domain.RefreshToken;
import org.antlr.v4.runtime.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.config.Task;

import java.sql.Ref;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    @Modifying
    @Query("update RefreshToken r set r.revoked = true where r.userId = : userId and r.revoked = false")
    void revokeAllForUser(@Param("userId") Long userId);

}
