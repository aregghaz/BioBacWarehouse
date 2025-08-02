package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
