package itk.student.task.manager.aqa.hw.dto;

import itk.student.task.manager.enums.TaskPriority;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateTaskRequest {

  public CreateTaskRequest() {}

  public CreateTaskRequest(UUID projectId, String title, String description, TaskPriority priority) {
    this.projectId = projectId;
    this.title = title;
    this.description = description;
    this.priority = priority;
  }

  private UUID projectId;

  private String title;

  private String description;

  private TaskPriority priority;

  private LocalDateTime dueDate;

  private UUID assigneeId;

}
