package com.example.rrms.service;

import com.example.rrms.domain.Role;
import com.example.rrms.domain.Task;
import com.example.rrms.domain.TaskStatus;
import com.example.rrms.domain.User;
import com.example.rrms.domain.UserStatus;
import com.example.rrms.repository.TaskRepository;
import com.example.rrms.repository.UserRepository;
import com.example.rrms.security.CurrentUser;
import com.example.rrms.security.user.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository tasks;
    private final UserRepository users;
    private final AuditService audit;

    @Transactional
    public Task assign(Long taskId, Long staffId) {
        UserPrincipal me = CurrentUser.get();
        Task task = loadTask(taskId, me);

        User staff = users.findByIdAndTenantId(staffId, me.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

        if (staff.getRole() != Role.STAFF || staff.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Staff is not active");
        }
        if (staff.getStaffType() != task.getDepartment()) {
            throw new IllegalStateException("Staff type does not match task department");
        }

        task.setAssignedStaffId(staffId);
        task.setStatus(TaskStatus.ASSIGNED);
        audit.log(me, "TASK_ASSIGN", "TASK", taskId.toString(), "SUCCESS", "staff=" + staffId);
        return task;
    }

    @Transactional
    public Task verify(Long taskId) {
        UserPrincipal me = CurrentUser.get();
        Task task = loadTask(taskId, me);
        requireStatus(task, TaskStatus.COMPLETED_BY_STAFF, TaskStatus.UNDER_INSPECTION);
        task.setStatus(TaskStatus.CLOSED);
        audit.log(me, "TASK_VERIFY", "TASK", taskId.toString(), "SUCCESS", null);
        return task;
    }

    @Transactional
    public Task reject(Long taskId, String reason) {
        UserPrincipal me = CurrentUser.get();
        Task task = loadTask(taskId, me);
        requireStatus(task, TaskStatus.COMPLETED_BY_STAFF, TaskStatus.UNDER_INSPECTION);
        task.setStatus(TaskStatus.REWORK_REQUIRED);
        task.setRejectionReason(reason);
        audit.log(me, "TASK_REJECT", "TASK", taskId.toString(), "SUCCESS", reason);
        return task;
    }

    @Transactional(readOnly = true)
    public List<Task> myTasks() {
        UserPrincipal me = CurrentUser.get();
        return tasks.findByTenantIdAndAssignedStaffId(me.getTenantId(), me.getId());
    }

    @Transactional
    public Task start(Long taskId) {
        UserPrincipal me = CurrentUser.get();
        Task task = loadAssignedToMe(taskId, me);
        requireStatus(task, TaskStatus.ASSIGNED, TaskStatus.REWORK_REQUIRED);
        task.setStatus(TaskStatus.IN_PROGRESS);
        audit.log(me, "TASK_START", "TASK", taskId.toString(), "SUCCESS", null);
        return task;
    }

    @Transactional
    public Task complete(Long taskId) {
        UserPrincipal me = CurrentUser.get();
        Task task = loadAssignedToMe(taskId, me);
        requireStatus(task, TaskStatus.IN_PROGRESS);
        task.setStatus(TaskStatus.COMPLETED_BY_STAFF);
        audit.log(me, "TASK_COMPLETE", "TASK", taskId.toString(), "SUCCESS", null);
        return task;
    }

    private Task loadTask(Long id, UserPrincipal me) {
        return tasks.findByIdAndTenantId(id, me.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
    }

    private Task loadAssignedToMe(Long id, UserPrincipal me) {
        Task task = loadTask(id, me);
        if (!me.getId().equals(task.getAssignedStaffId())) {
            audit.log(me, "TASK_ACCESS", "TASK", id.toString(), "DENIED", "not assigned");
            throw new AccessDeniedException("Task is not assigned to you");
        }
        return task;
    }

    private void requireStatus(Task t, TaskStatus... allowed) {
        if (!Arrays.asList(allowed).contains(t.getStatus())) {
            throw new IllegalStateException("Invalid transition from " + t.getStatus());
        }
    }
}
