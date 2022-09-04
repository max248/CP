package com.example.courseproject.Repositories;

import com.example.courseproject.model.Rates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RateRepository extends JpaRepository<Rates, Long> {

    @Query("SELECT r FROM Rates r where r.user.id = ?1 and r.item.id = ?2")
    Rates findByItemUser(Long userId, Long itemId);
}
