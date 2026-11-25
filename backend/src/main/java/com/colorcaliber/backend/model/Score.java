package com.colorcaliber.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scores")
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "weapon_id", nullable = false)
    private Weapon weapon;

    @Column(name = "weapon_level", nullable = false)
    private int weaponLevel = 1;

    @Column(nullable = false)
    private int score;

    @Column(name = "completion_time", nullable = false)
    private double completionTime;

    @Column(name = "rounds_cleared", nullable = false)
    private int roundsCleared;

    @Column(name = "diamonds_collected", nullable = false)
    private int diamondsCollected;

    @Column(name = "blue_grid_points", nullable = false)
    private int blueGridPoints;

    @Column(nullable = false)
    private boolean victory;

    @Column(name = "played_at", nullable = false, updatable = false)
    private LocalDateTime playedAt;

    public Score() {
    }

    @PrePersist
    protected void onCreate() {
        this.playedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Weapon getWeapon() { return weapon; }
    public void setWeapon(Weapon weapon) { this.weapon = weapon; }

    public int getWeaponLevel() { return weaponLevel; }
    public void setWeaponLevel(int weaponLevel) { this.weaponLevel = weaponLevel; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public double getCompletionTime() { return completionTime; }
    public void setCompletionTime(double completionTime) { this.completionTime = completionTime; }

    public int getRoundsCleared() { return roundsCleared; }
    public void setRoundsCleared(int roundsCleared) { this.roundsCleared = roundsCleared; }

    public int getDiamondsCollected() { return diamondsCollected; }
    public void setDiamondsCollected(int diamondsCollected) { this.diamondsCollected = diamondsCollected; }

    public int getBlueGridPoints() { return blueGridPoints; }
    public void setBlueGridPoints(int blueGridPoints) { this.blueGridPoints = blueGridPoints; }

    public boolean isVictory() { return victory; }
    public void setVictory(boolean victory) { this.victory = victory; }

    public LocalDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }
}