package com.example.courseproject.Repositories;

import com.example.courseproject.model.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comments, Long> {
    @Query("SELECT c FROM Comments c where c.item.id = ?1 order by c.createDate desc")
    List<Comments> findAllbyItemId(Long itemId);
}
