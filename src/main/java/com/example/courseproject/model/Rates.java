package com.example.courseproject.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "rates")
public class Rates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne
    @JoinColumn(name = "item_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Items item;

    @Column
    private Integer rate;

    @Column(name = "create_date", nullable = false, length = 64)
    private Date createDate;

    @Column(name = "update_date", nullable = true, length = 64)
    private Date updateDate;

}
