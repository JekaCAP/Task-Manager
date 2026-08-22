package itk.student.task.manager.mapper;

import itk.student.task.manager.dto.response.ActivityLogResponse;
import itk.student.task.manager.entity.ActivityLog;
import itk.student.task.manager.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ActivityLogMapper {

    public ActivityLogResponse toResponse(ActivityLog log, User actor) {
        String actorName = actor.getFirstName() + " " + actor.getLastName();
        return new ActivityLogResponse(
                log.getId(),
                log.getTaskId(),
                log.getActorId(),
                actorName,
                log.getAction(),
                log.getDetails(),
                log.getCreatedAt()
        );
    }
}
