package com.example.courseproject.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "comments")
public class Comments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "item_id",nullable = false)
    private Items item;

    @Column
    private String content;

    @Column(name = "create_date", nullable = false, length = 64)
    private Date createDate;

    @Column(name = "update_date", nullable = true, length = 64)
    private Date updateDate;

}
