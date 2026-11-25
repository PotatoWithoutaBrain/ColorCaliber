package com.colorcaliber.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "high_score", nullable = false)
    private int highScore = 0;

    @Column(name = "best_completion_time")
    private Double bestCompletionTime;

    @Column(name = "total_games", nullable = false)
    private int totalGames = 0;

    @Column(name = "total_victories", nullable = false)
    private int totalVictories = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Player() {
    }

    public Player(String username) {
        this.username = username;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getHighScore() { return highScore; }
    public void setHighScore(int highScore) { this.highScore = highScore; }

    public Double getBestCompletionTime() { return bestCompletionTime; }
    public void setBestCompletionTime(Double bestCompletionTime) { this.bestCompletionTime = bestCompletionTime; }

    public int getTotalGames() { return totalGames; }
    public void setTotalGames(int totalGames) { this.totalGames = totalGames; }

    public int getTotalVictories() { return totalVictories; }
    public void setTotalVictories(int totalVictories) { this.totalVictories = totalVictories; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}