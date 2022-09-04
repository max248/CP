package com.example.courseproject.Repositories;

import com.example.courseproject.Projections.ItemProjection;
import com.example.courseproject.model.Collections;
import com.example.courseproject.model.Items;
import com.example.courseproject.model.Topics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManagerFactory;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Items,Long> {
    @Query("SELECT i FROM Items i where i.name = ?1 and i.user.id = ?2")
    Items findByName(String name, Long userId);

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

    @Query(nativeQuery = true, value = "select CAST(json_agg(tt.*) as text) as json from(\n" +
            "select t.id,json_agg(t.*) as json\n" +
            "from \n" +
            "(select i.id, i.name, i.image_url," +
            "(select name from collections where id = i.collection_id) as collection_name," +
            "(select count(*) from comments where item_id = i.id) as comment_count,\n" +
            "(select  COALESCE(round(sum(rate)/COALESCE(count(id),0)),0) as overall_rate from rates where item_id = i.id) as overall_rate,\n" +
            "json_build_object('columns',\n" +
            "(select json_agg(m.*) from (select cc.name,itd.data from item_data  itd\n" +
            "left join collection_columns cc on cc.id = itd.collection_column_id\n" +
            "where itd.item_id = i.id)m), 'tags',(select json_agg(m.*) from \n" +
            "(select t.name\n" +
            "from tags  t\n" +
            "left join item_tags it on it.tag_id = t.id\n" +
            "where it.item_id = i.id)m\n" +
            ")) as json \n" +
            "\n" +
            "from items i where i.status is true group by i.id order by i.id\n" +
            ")t group by t.id) tt")
    String getItemJsonDataByUserId(Long userId);
    @Query(nativeQuery = true, value = "select CAST(json_agg(tt.*) as text) as json from(\n" +
            "select t.id,json_agg(t.*) as json\n" +
            "from \n" +
            "(select i.id, i.name, i.image_url," +
            "(select name from collections where id = i.collection_id) as collection_name," +
            "(select count(*) from comments where item_id = i.id) as comment_count,\n" +
            "(select  COALESCE(round(sum(rate)/COALESCE(count(id),0)),0) as overall_rate from rates where item_id = i.id) as overall_rate,\n" +
            "json_build_object('columns',\n" +
            "(select json_agg(m.*) from (select cc.name,itd.data from item_data  itd\n" +
            "left join collection_columns cc on cc.id = itd.collection_column_id\n" +
            "where itd.item_id = i.id)m), 'tags',(select json_agg(m.*) from \n" +
            "(select t.name\n" +
            "from tags  t\n" +
            "left join item_tags it on it.tag_id = t.id\n" +
            "where it.item_id = i.id)m\n" +
            ")) as json \n" +
            "\n" +
            "from items i where i.status is true and i.id = ?1 group by i.id order by i.id\n" +
            ")t group by t.id) tt")
    String getItemJsonDataByItemId(Long itemId);

}
