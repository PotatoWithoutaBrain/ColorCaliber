package com.colorcaliber.backend.repository;

import com.colorcaliber.backend.model.Weapon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeaponRepository extends JpaRepository<Weapon, Long> {
    Optional<Weapon> findByCodeIgnoreCase(String code);
}