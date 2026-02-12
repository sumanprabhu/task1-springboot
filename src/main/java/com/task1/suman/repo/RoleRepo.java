package com.task1.suman.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.task1.suman.model.Role;

public interface RoleRepo extends JpaRepository<Role, Long> {
}
