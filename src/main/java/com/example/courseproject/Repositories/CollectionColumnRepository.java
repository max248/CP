package com.example.courseproject.Repositories;

import com.example.courseproject.model.CollectionColumns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CollectionColumnRepository extends JpaRepository<CollectionColumns, Long> {
    @Query("SELECT cc FROM CollectionColumns cc where cc.collection.id = ?1")
    List<CollectionColumns> findAllByCollection(Long collectionId);

    @Query("SELECT cc FROM CollectionColumns cc where cc.collection.id = ?1 and cc.name = ?2")
    List<CollectionColumns> findByNameCollection(Long collectionId, String name);
}
