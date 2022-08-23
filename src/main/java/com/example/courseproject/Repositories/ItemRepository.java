package com.example.courseproject.Repositories;

import com.example.courseproject.model.Collections;
import com.example.courseproject.model.Items;
import com.example.courseproject.model.Topics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ItemRepository extends JpaRepository<Items,Long> {
    @Query("SELECT i FROM Items i where i.name = ?1 and i.user.id = ?2")
    Topics findByName(String name, Long userId);

    @Query("SELECT i FROM Items i order by i.id")
    List<Items> findAllOrderById();

    @Query("SELECT i FROM Items i where i.user.id = ?1")
    List<Items> findAllByUser(Long userId);

    @Modifying
    @Query("update Items i set i.status = ?2 where i.id = ?1")
    @Transactional
    void updateStatusById(Long id, boolean flag);

    @Modifying
    @Query("update Items i set i.name = ?2, i.updateDate = current_timestamp where i.id = ?1")
    @Transactional
    void updateNameById(Long id, String name);


}
