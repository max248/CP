package com.example.courseproject.Controllers;

import com.example.courseproject.model.ItemTags;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemTagsRepository extends JpaRepository<ItemTags,Long> {
}
