package com.example.courseproject.Repositories;

import com.example.courseproject.model.Topics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topics, Long> {
    @Query("SELECT t FROM Topics t order by t.id")
    List<Topics> findAllOrderById();

    @Query("SELECT t FROM Topics t where t.id = ?1")
    Topics getById(Long id);

    @Query("SELECT t FROM Topics t where t.name = ?1")
    Topics findByName(String name);

    @Modifying
    @Query("update Topics t set t.status = ?2, t.updateDate = current_timestamp where t.id = ?1")
    @Transactional
    void updateStatusById(Long id, boolean flag);

    @Modifying
    @Query("update Topics t set t.name = ?2, t.updateDate = current_timestamp where t.id = ?1")
    @Transactional
    void updateNameById(Long id, String name);

}
