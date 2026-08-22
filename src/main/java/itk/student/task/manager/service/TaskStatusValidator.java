package itk.student.task.manager.service;

import itk.student.task.manager.enums.ProjectRole;
import itk.student.task.manager.enums.TaskStatus;
import itk.student.task.manager.exception.BusinessRuleException;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class TaskStatusValidator {

    private static final Map<TaskStatus, Set<TaskStatus>> ALLOWED = Map.of(
            TaskStatus.TODO, EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED),
            TaskStatus.IN_PROGRESS, EnumSet.of(TaskStatus.REVIEW, TaskStatus.TODO, TaskStatus.CANCELLED),
            TaskStatus.REVIEW, EnumSet.of(TaskStatus.DONE, TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED),
            TaskStatus.DONE, EnumSet.of(TaskStatus.CANCELLED),
            TaskStatus.CANCELLED, EnumSet.noneOf(TaskStatus.class)
    );

    private TaskStatusValidator() {
    }

    public static void validateTransition(TaskStatus current, TaskStatus target) {
        if (current == target) {
            return;
        }
        Set<TaskStatus> allowed = ALLOWED.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new BusinessRuleException("Invalid status transition: " + current + " -> " + target);
        }
    }
}
