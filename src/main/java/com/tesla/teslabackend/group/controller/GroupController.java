package com.tesla.teslabackend.group.controller;

import com.tesla.teslabackend.group.dto.GroupRankingDTO;
import com.tesla.teslabackend.group.dto.JoinGroupRequestDTO;
import com.tesla.teslabackend.group.entity.Group;
import com.tesla.teslabackend.group.service.GroupService;
import com.tesla.teslabackend.user.component.IdentityExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Ajustar según la seguridad de tu proyecto
public class GroupController {

    private final GroupService groupService;

    private final IdentityExtractor identityExtractor;

    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@RequestParam String name, @AuthenticationPrincipal Jwt jwt) {
        try {
            Long creatorId = Long.valueOf(identityExtractor.getUsuarioId(jwt));
            Group group = groupService.createGroup(name, creatorId);
            return ResponseEntity.ok(group);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinGroup(@RequestBody JoinGroupRequestDTO request, @AuthenticationPrincipal Jwt jwt) {
        try {
            Long studentId = Long.valueOf(identityExtractor.getUsuarioId(jwt));
            String groupName = groupService.joinGroup(request.getCode(), studentId);
            return ResponseEntity.ok("Te has unido exitosamente al grupo: " + groupName);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{groupId}/ranking")
    public ResponseEntity<List<GroupRankingDTO>> getRanking(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupRanking(groupId));
    }

    @GetMapping("/student/me")
    public ResponseEntity<Group> getStudentGroup(@AuthenticationPrincipal  Jwt jwt) {
        Long studentId = Long.valueOf(identityExtractor.getUsuarioId(jwt));
        Group group = groupService.getGroupByStudentId(studentId);
        return group != null ? ResponseEntity.ok(group) : ResponseEntity.noContent().build();
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<?> leaveGroup(@PathVariable Long groupId, @AuthenticationPrincipal Jwt jwt) {
        try {
            Long studentId = Long.valueOf(identityExtractor.getUsuarioId(jwt));

            groupService.leaveGroup(groupId, studentId);
            return ResponseEntity.ok().body("Has salido del grupo exitosamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}