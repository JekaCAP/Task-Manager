package itk.student.task.manager.service;

import itk.student.task.manager.entity.ActivityLog;
import itk.student.task.manager.enums.ActivityAction;
import itk.student.task.manager.repository.ActivityLogRepository;
import itk.student.task.manager.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public void log(UUID taskId, ActivityAction action, String details) {
        ActivityLog log = new ActivityLog();
        log.setTaskId(taskId);
        log.setActorId(SecurityUtils.currentUserId());
        log.setAction(action);
        log.setDetails(details);
        activityLogRepository.save(log);
    }
}
