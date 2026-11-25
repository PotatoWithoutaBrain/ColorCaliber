package com.colorcaliber.backend.repository;

import com.colorcaliber.backend.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {
    Optional<Player> findByUsernameIgnoreCase(String username);
    List<Player> findTop10ByOrderByHighScoreDesc();
    List<Player> findTop10ByBestCompletionTimeIsNotNullOrderByBestCompletionTimeAsc();
}