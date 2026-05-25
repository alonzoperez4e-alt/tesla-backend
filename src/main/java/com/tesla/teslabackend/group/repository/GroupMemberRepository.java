package com.tesla.teslabackend.group.repository;

import com.tesla.teslabackend.group.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    Optional<GroupMember> findByStudentId(Long studentId);
    boolean existsByStudentId(Long studentId);
    List<GroupMember> findByGroupIdOrderByGroupExpDesc(Long groupId);

    // NUEVO MÉTODO: Cuenta cuántos integrantes tiene un grupo en específico
    long countByGroupId(Long groupId);
}