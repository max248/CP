package com.example.courseproject.Repositories;

import com.example.courseproject.model.ItemData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemDataRepositories extends JpaRepository<ItemData, Long> {
}
