package com.example.courseproject.Repositories;

import com.example.courseproject.model.Items;
import com.example.courseproject.model.Topics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ItemRepository extends JpaRepository<Items,Long> {
    @Query("SELECT i FROM Items i where i.name = ?1 and i.user.id = ?2")
    Topics findByName(String name, Long userId);
}
