package com.example.courseproject.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "item_tags")
public class ItemTags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Items items;

    @ManyToOne
    @JoinColumn(name = "tag_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tags tags;

    @Column(name = "create_date", nullable = false, length = 64)
    private Date createDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Items getItems() {
        return items;
    }

    public void setItems(Items items) {
        this.items = items;
    }

    public Tags getTags() {
        return tags;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
