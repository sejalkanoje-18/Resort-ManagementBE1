package com.example.rrms.web;

import com.example.rrms.domain.Task;
import com.example.rrms.service.TaskService;
import com.example.rrms.dto.CreateTaskRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService svc;

    @PreAuthorize("hasAuthority('TASK_CREATE')")
    @PostMapping("/api/management/tasks")
    public Task create(@Valid @RequestBody CreateTaskRequest req) {
        return svc.createTask(req);
    }

    @PreAuthorize("hasAuthority('TASK_VIEW')")
    @GetMapping("/api/management/tasks")
    public List<Task> getTasks() {
        return svc.getTenantTasks();
    }

    @PreAuthorize("hasAuthority('TASK_ASSIGN')")
    @PutMapping("/api/management/tasks/{id}/assign/{staffId}")
    public Task assign(@PathVariable Long id, @PathVariable Long staffId) {
        return svc.assign(id, staffId);
    }

    @PreAuthorize("hasAuthority('TASK_VERIFY')")
    @PutMapping("/api/management/tasks/{id}/verify")
    public Task verify(@PathVariable Long id) {
        return svc.verify(id);
    }

    @PreAuthorize("hasAuthority('TASK_REJECT')")
    @PutMapping("/api/management/tasks/{id}/reject")
    public Task reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return svc.reject(id, body.get("reason"));
    }

    @PreAuthorize("hasAuthority('TASK_VIEW_ASSIGNED')")
    @GetMapping("/api/staff/tasks")
    public List<Task> myTasks() {
        return svc.myTasks();
    }

    @PreAuthorize("hasAuthority('TASK_START')")
    @PutMapping("/api/staff/tasks/{id}/start")
    public Task start(@PathVariable Long id) {
        return svc.start(id);
    }

    @PreAuthorize("hasAuthority('TASK_COMPLETE')")
    @PutMapping("/api/staff/tasks/{id}/complete")
    public Task complete(@PathVariable Long id) {
        return svc.complete(id);
    }
}
