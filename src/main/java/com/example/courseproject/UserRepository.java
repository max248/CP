package com.example.courseproject;


import com.example.courseproject.model.Role;
import com.example.courseproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u where u.email = :email")
    public User getUserByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u where u.email = ?1")
    User findByEmail(String email);

    @Modifying
    @Query("update User u set u.lastLoginDate = ?2 where u.id = ?1")
    @Transactional
    void updateLastLoginDateById(Long id, String date);

    @Modifying
    @Query("update User u set u.enabled = ?2 where u.id = ?1")
    @Transactional
    void updateStatusById(Long id, boolean flag);

    @Modifying
    @Query("update User u set u.role = ?2 where u.id = ?1")
    @Transactional
    void updateRoleById(Long id, Role role);
}
