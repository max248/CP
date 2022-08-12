package com.example.courseproject;

import com.example.courseproject.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query("SELECT r FROM Role r where r.name = ?1")
    Role findByRoleName(String roleName);


    @Query("SELECT r FROM Role r where r.id = ?1")
    Role findRoleById(Integer id);
}
