package com.tesla.teslabackend.group.service;

import com.tesla.teslabackend.group.dto.GroupRankingDTO;
import com.tesla.teslabackend.group.entity.Group;
import com.tesla.teslabackend.group.entity.GroupMember;
import com.tesla.teslabackend.group.repository.GroupMemberRepository;
import com.tesla.teslabackend.group.repository.GroupRepository;
import com.tesla.teslabackend.group.repository.ChatMessageRepository;
import com.tesla.teslabackend.user.entity.Usuario;
import com.tesla.teslabackend.user.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    // INYECTAMOS TU REPOSITORIO DE USUARIOS
    private final UsuarioRepository usuarioRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public Group createGroup(String name, Long creatorId) {
        if (groupMemberRepository.existsByStudentId(creatorId)) {
            throw new IllegalArgumentException("Ya perteneces a un grupo. Abandona tu grupo actual antes de crear uno.");
        }

        if (groupRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("El nombre '" + name + "' ya está siendo usado por otro grupo. Por favor, elige otro nombre.");
        }

        String uniqueCode = "TSL-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Group group = new Group();
        group.setName(name);
        group.setCode(uniqueCode);
        group.setCreatorId(creatorId);
        Group savedGroup = groupRepository.save(group);

        // 4. Agregar al creador como primer miembro
        GroupMember leader = new GroupMember();
        leader.setGroup(savedGroup);
        leader.setStudentId(creatorId);
        leader.setGroupExp(0);
        groupMemberRepository.save(leader);

        return savedGroup;
    }

    @Transactional
    public String joinGroup(String code, Long studentId) {
        if (groupMemberRepository.existsByStudentId(studentId)) {
            throw new IllegalArgumentException("Ya estás en un grupo activo.");
        }

        Group group = groupRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Código de grupo no válido o no existe."));

        GroupMember newMember = new GroupMember();
        newMember.setGroup(group);
        newMember.setStudentId(studentId);
        groupMemberRepository.save(newMember);

        return group.getName();
    }

    @Transactional(readOnly = true)
    public List<GroupRankingDTO> getGroupRanking(Long groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroupIdOrderByGroupExpDesc(groupId);
        AtomicInteger position = new AtomicInteger(1);

        return members.stream().map(member -> {

            String studentName = usuarioRepository.findById(member.getStudentId().intValue())
                    .map(usuario -> usuario.getNombre() + " " + usuario.getApellido()) // Concatenamos Nombre y Apellido
                    .orElse("Estudiante " + member.getStudentId()); // Fallback por si el usuario fue borrado de la BD

            return new GroupRankingDTO(
                    position.getAndIncrement(),
                    member.getStudentId(),
                    studentName,
                    member.getGroupExp()
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Group getGroupByStudentId(Long studentId) {
        return groupMemberRepository.findByStudentId(studentId)
                .map(member -> groupRepository.findById(member.getGroup().getId()).orElse(null))
                .orElse(null);
    }

    @Transactional
    public void leaveGroup(Long groupId, Long studentId) {
        GroupMember member = groupMemberRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("No perteneces a ningún grupo activo."));

        if (!member.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("No perteneces a este grupo específico.");
        }

        long totalMembers = groupMemberRepository.countByGroupId(groupId);

        groupMemberRepository.delete(member);

        if (totalMembers <= 1) {
            chatMessageRepository.deleteByGroupId(groupId);
            groupRepository.deleteById(groupId);
        }
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        chatMessageRepository.deleteByGroupId(groupId);

        groupRepository.deleteById(groupId);
    }
}