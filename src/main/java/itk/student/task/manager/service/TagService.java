package itk.student.task.manager.service;

import itk.student.task.manager.dto.response.TagResponse;
import itk.student.task.manager.entity.Tag;
import itk.student.task.manager.entity.Task;
import itk.student.task.manager.entity.TaskTag;
import itk.student.task.manager.exception.ConflictException;
import itk.student.task.manager.exception.ResourceNotFoundException;
import itk.student.task.manager.mapper.TagMapper;
import itk.student.task.manager.repository.TagRepository;
import itk.student.task.manager.repository.TaskRepository;
import itk.student.task.manager.repository.TaskTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TaskRepository taskRepository;
    private final TaskTagRepository taskTagRepository;
    private final TagMapper tagMapper;
    private final ProjectAccessService projectAccessService;

    @Transactional(readOnly = true)
    public List<TagResponse> listTags(UUID projectId) {
        if (projectId != null) {
            projectAccessService.requireReadAccess(projectId);
            return tagRepository.findByProjectId(projectId).stream()
                    .map(tagMapper::toResponse)
                    .toList();
        }
        return tagRepository.findAll().stream().map(tagMapper::toResponse).toList();
    }

    @Transactional
    public void attachTag(UUID taskId, UUID tagId) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        projectAccessService.requireWriteAccess(task.getProjectId());

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        if (!tag.getProjectId().equals(task.getProjectId())) {
            throw new ResourceNotFoundException("Tag does not belong to task project");
        }
        if (taskTagRepository.existsByTaskIdAndTagId(taskId, tagId)) {
            throw new ConflictException("Tag is already attached to task");
        }
        taskTagRepository.save(new TaskTag(taskId, tagId));
    }
}
