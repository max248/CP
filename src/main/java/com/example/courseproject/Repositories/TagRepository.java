package com.example.courseproject.Repositories;

import com.example.courseproject.model.Tags;
import com.example.courseproject.model.Topics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TagRepository extends JpaRepository<Tags, Long> {
    @Query("SELECT t FROM Tags t where t.name = ?1")
    Tags findByName(String name);

    @Modifying
    @Query("update Tags t set t.status = ?2, t.updateDate = current_timestamp where t.id = ?1")
    @Transactional
    void updateStatusById(Long id, boolean flag);

    @Modifying
    @Query("update Tags t set t.name = ?2, t.updateDate = current_timestamp where t.id = ?1")
    @Transactional
    void updateNameById(Long id, String name);

    @Query("SELECT t FROM Tags t order by t.id")
    List<Tags> findAllOrderById();

}
