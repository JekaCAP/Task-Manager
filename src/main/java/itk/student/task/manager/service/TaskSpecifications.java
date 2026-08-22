package itk.student.task.manager.service;

import itk.student.task.manager.entity.Task;
import itk.student.task.manager.enums.TaskPriority;
import itk.student.task.manager.enums.TaskStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<Task> filter(UUID projectId,
                                             TaskStatus status,
                                             TaskPriority priority,
                                             UUID assigneeId,
                                             UUID tagId,
                                             Instant dueBefore,
                                             Instant dueAfter,
                                             String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (projectId != null) {
                predicates.add(cb.equal(root.get("projectId"), projectId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("assigneeId"), assigneeId));
            }
            if (dueBefore != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), dueBefore));
            }
            if (dueAfter != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), dueAfter));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }
            if (tagId != null && query != null) {
                var taskTagRoot = query.from(itk.student.task.manager.entity.TaskTag.class);
                predicates.add(cb.equal(taskTagRoot.get("taskId"), root.get("id")));
                predicates.add(cb.equal(taskTagRoot.get("tagId"), tagId));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
