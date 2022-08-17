package com.example.courseproject.Repositories;

import com.example.courseproject.model.Collections;
import com.example.courseproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collections, Long> {
    @Query("SELECT c FROM Collections c where c.name = ?1")
    Collections findByName(String name);

    @Modifying
    @Query("update Collections c set c.status = ?2 where c.id = ?1")
    @Transactional
    void updateStatusById(Long id, boolean flag);

    @Query("SELECT c FROM Collections c where c.user.id = ?1")
    List<Collections> findAllByUser(Long userId);

}
