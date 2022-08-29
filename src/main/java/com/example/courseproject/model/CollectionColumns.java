package com.example.courseproject.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Table(name = "collection_columns")
public class CollectionColumns {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "collection_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Collections collection;

    @ManyToOne
    @JoinColumn(name = "column_type_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ColumnType columnType;

    @Column(nullable = false, length = 256)
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Collections getCollection() {
        return collection;
    }

    public void setCollection(Collections collection) {
        this.collection = collection;
    }

    public ColumnType getColumnType() {
        return columnType;
    }

    public void setColumnType(ColumnType columnType) {
        this.columnType = columnType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
