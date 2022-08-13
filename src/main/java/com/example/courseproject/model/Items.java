package com.example.courseproject.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "items")
public class Items {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "collection_id",nullable = false)
    private Collections collection;

    @ManyToOne
    @JoinColumn(name = "create_user_id",nullable = false)
    private User user;

    @Column(name = "create_date", nullable = false, length = 64)
    private Date createDate;

    @Column(name = "update_date", nullable = true, length = 64)
    private Date updateDate;

    @Column
    private boolean status;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
