package com.example.courseproject.model;

import javax.persistence.*;

@Entity
@Table(name = "item_data")
public class ItemData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "collection_column_id",nullable = false)
    private CollectionColumns collectionColumns;

    @ManyToOne
    @JoinColumn(name = "item_id",nullable = false)
    private Items item;

    @Column
    private String data;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CollectionColumns getCollectionColumns() {
        return collectionColumns;
    }

    public void setCollectionColumns(CollectionColumns collectionColumns) {
        this.collectionColumns = collectionColumns;
    }

    public Items getItem() {
        return item;
    }

    public void setItem(Items item) {
        this.item = item;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
