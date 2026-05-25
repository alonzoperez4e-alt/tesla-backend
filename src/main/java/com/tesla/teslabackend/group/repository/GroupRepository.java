package com.tesla.teslabackend.group.repository;

import com.tesla.teslabackend.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByCode(String code);

    boolean existsByNameIgnoreCase(String name);
}