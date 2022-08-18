package com.example.courseproject.Repositories;

import com.example.courseproject.model.ItemData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemDataRepository extends JpaRepository<ItemData, Long> {
}
