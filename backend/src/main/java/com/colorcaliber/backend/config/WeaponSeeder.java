package com.colorcaliber.backend.config;

import com.colorcaliber.backend.model.Weapon;
import com.colorcaliber.backend.repository.WeaponRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class WeaponSeeder implements CommandLineRunner {

    private final WeaponRepository weaponRepository;

    public WeaponSeeder(WeaponRepository weaponRepository) {
        this.weaponRepository = weaponRepository;
    }

    @Override
    public void run(String... args) {
        seedIfMissing("SIMPLE", "Straight Blaster", 20.0, 0.0, 580.0,
                "Initial semi-automatic blaster with no fire delay.");
        seedIfMissing("SHOTGUN", "Shotgun", 20.0, 0.35, 520.0,
                "Semi-automatic spread weapon firing 9 pellets in a cone.");
        seedIfMissing("SMG", "Rapid SMG", 10.0, 0.07, 640.0,
                "Automatic rapid-fire SMG with reduced damage per bullet.");
    }

    private void seedIfMissing(String code, String name, double baseDamage,
                               double fireDelay, double bulletSpeed, String description) {
        if (weaponRepository.findByCodeIgnoreCase(code).isEmpty()) {
            weaponRepository.save(new Weapon(code, name, baseDamage, fireDelay, bulletSpeed, description));
        }
    }
}