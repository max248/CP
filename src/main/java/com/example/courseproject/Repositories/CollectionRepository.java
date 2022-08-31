package com.example.courseproject.Repositories;

import com.example.courseproject.model.Collections;
import com.example.courseproject.model.Topics;
import com.example.courseproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collections, Long> {
    @Query("SELECT c FROM Collections c where c.name = ?1")
    Collections findByName(String name);

    @Query("SELECT c FROM Collections c order by c.id")
    List<Collections> findAllOrderById();
    @Modifying
    @Query("update Collections c set c.name = ?2, c.updateDate = current_timestamp where c.id = ?1")
    @Transactional
    void updateNameById(Long id, String name);
    @Modifying
    @Query("update Collections c set c.status = ?2 where c.id = ?1")
    @Transactional
    void updateStatusById(Long id, boolean flag);

    @Query("SELECT c FROM Collections c where c.user.id = ?1")
    List<Collections> findAllByUser(Long userId);

    @Query(nativeQuery = true, value = "select CAST(json_agg(t.*) as TEXT) as json from (select \n" +
            "c.name collection_name, c.descriptions descriptions, u.first_name || ' ' || \n" +
            "u.last_name as author_name, t.name as topic_name, c.image_url\n" +
            "from collections c\n" +
            "left join users u on u.id = c.create_user_id\n" +
            "left join topics t on t.id = c.topic_id\n" +
            "where c.status is true and u.id = ?1)t")
    String getCollectionJsonDataByUserId(Long userId);
}
