package com.inventory.wareflow.repository;

import com.inventory.wareflow.entity.AdminPermission;
import com.inventory.wareflow.entity.User;
import com.inventory.wareflow.enums.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminPermissionRepository extends JpaRepository<AdminPermission, Long> {
    List<AdminPermission> findByUser(User user);

    Optional<AdminPermission> findByUserAndActivity(User user, Activity activity);

    boolean existsByUserAndActivity(User user, Activity activity);

    void deleteByUserAndActivity(User user, Activity activity);
}