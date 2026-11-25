package com.colorcaliber.backend.repository;

import com.colorcaliber.backend.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScoreRepository extends JpaRepository<Score, UUID> {
    List<Score> findByPlayerIdOrderByPlayedAtDesc(UUID playerId);
    List<Score> findTop10ByOrderByScoreDesc();
    List<Score> findTop10ByVictoryTrueOrderByCompletionTimeAsc();
    List<Score> findByWeaponCodeIgnoreCaseOrderByScoreDesc(String code);
}