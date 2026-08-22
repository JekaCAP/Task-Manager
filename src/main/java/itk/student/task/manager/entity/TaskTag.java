package itk.student.task.manager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "task_tags")
@IdClass(TaskTag.TaskTagId.class)
@Getter
@Setter
@NoArgsConstructor
public class TaskTag {

    @Id
    @Column(name = "task_id", length = 36)
    private UUID taskId;

    @Id
    @Column(name = "tag_id", length = 36)
    private UUID tagId;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TaskTagId implements Serializable {
        private UUID taskId;
        private UUID tagId;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TaskTagId taskTagId = (TaskTagId) o;
            return Objects.equals(taskId, taskTagId.taskId) && Objects.equals(tagId, taskTagId.tagId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(taskId, tagId);
        }
    }

    public TaskTag(UUID taskId, UUID tagId) {
        this.taskId = taskId;
        this.tagId = tagId;
    }

    @PrePersist
    void validate() {
        if (taskId == null || tagId == null) {
            throw new IllegalStateException("taskId and tagId are required");
        }
    }
}
