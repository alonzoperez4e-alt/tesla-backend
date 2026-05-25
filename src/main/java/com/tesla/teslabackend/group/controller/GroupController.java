package com.tesla.teslabackend.group.controller;

import com.tesla.teslabackend.group.dto.GroupRankingDTO;
import com.tesla.teslabackend.group.dto.JoinGroupRequestDTO;
import com.tesla.teslabackend.group.entity.Group;
import com.tesla.teslabackend.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Ajustar según la seguridad de tu proyecto
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@RequestParam String name, @RequestParam Long creatorId) {
        try {
            Group group = groupService.createGroup(name, creatorId);
            return ResponseEntity.ok(group);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinGroup(@RequestBody JoinGroupRequestDTO request) {
        try {
            String groupName = groupService.joinGroup(request.getCode(), request.getStudentId());
            return ResponseEntity.ok("Te has unido exitosamente al grupo: " + groupName);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{groupId}/ranking")
    public ResponseEntity<List<GroupRankingDTO>> getRanking(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupRanking(groupId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Group> getStudentGroup(@PathVariable Long studentId) {
        Group group = groupService.getGroupByStudentId(studentId);
        return group != null ? ResponseEntity.ok(group) : ResponseEntity.noContent().build();
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<?> leaveGroup(@PathVariable Long groupId, @RequestBody java.util.Map<String, Long> payload) {
        try {
            Long studentId = payload.get("studentId");
            if (studentId == null) {
                return ResponseEntity.badRequest().body("El studentId es obligatorio.");
            }

            groupService.leaveGroup(groupId, studentId);
            return ResponseEntity.ok().body("Has salido del grupo exitosamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}