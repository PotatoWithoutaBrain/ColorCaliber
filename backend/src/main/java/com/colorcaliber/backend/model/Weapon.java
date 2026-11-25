package com.colorcaliber.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "weapons")
public class Weapon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(name = "base_damage", nullable = false)
    private double baseDamage;

    @Column(name = "fire_delay", nullable = false)
    private double fireDelay;

    @Column(name = "bullet_speed", nullable = false)
    private double bulletSpeed;

    @Column(length = 255)
    private String description;

    public Weapon() {
    }

    public Weapon(String code, String name, double baseDamage, double fireDelay, double bulletSpeed, String description) {
        this.code = code;
        this.name = name;
        this.baseDamage = baseDamage;
        this.fireDelay = fireDelay;
        this.bulletSpeed = bulletSpeed;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBaseDamage() { return baseDamage; }
    public void setBaseDamage(double baseDamage) { this.baseDamage = baseDamage; }

    public double getFireDelay() { return fireDelay; }
    public void setFireDelay(double fireDelay) { this.fireDelay = fireDelay; }

    public double getBulletSpeed() { return bulletSpeed; }
    public void setBulletSpeed(double bulletSpeed) { this.bulletSpeed = bulletSpeed; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}