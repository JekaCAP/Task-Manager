package itk.student.task.manager.service;

import itk.student.task.manager.config.UploadProperties;
import itk.student.task.manager.dto.response.AttachmentResponse;
import itk.student.task.manager.entity.Attachment;
import itk.student.task.manager.entity.Task;
import itk.student.task.manager.exception.BusinessRuleException;
import itk.student.task.manager.exception.ResourceNotFoundException;
import itk.student.task.manager.mapper.AttachmentMapper;
import itk.student.task.manager.repository.AttachmentRepository;
import itk.student.task.manager.repository.TaskRepository;
import itk.student.task.manager.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final AttachmentMapper attachmentMapper;
    private final UploadProperties uploadProperties;
    private final ProjectAccessService projectAccessService;

    @Transactional
    public AttachmentResponse upload(UUID taskId, MultipartFile file) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        projectAccessService.requireWriteAccess(task.getProjectId());

        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("File must not be empty");
        }
        if (file.getSize() > uploadProperties.getMaxFileSizeBytes()) {
            throw new BusinessRuleException("Uploaded file is too large");
        }
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        if (!uploadProperties.getAllowedContentTypes().contains(contentType)) {
            throw new BusinessRuleException("File type is not allowed: " + contentType);
        }

        try {
            Path uploadDir = Path.of("uploads", task.getProjectId().toString());
            Files.createDirectories(uploadDir);
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path target = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), target);

            Attachment attachment = new Attachment();
            attachment.setTaskId(taskId);
            attachment.setOriginalName(file.getOriginalFilename());
            attachment.setContentType(contentType);
            attachment.setSizeBytes(file.getSize());
            attachment.setStoragePath(target.toString());
            attachment.setUploadedById(SecurityUtils.currentUserId());
            attachmentRepository.save(attachment);

            return attachmentMapper.toResponse(attachment);
        } catch (IOException ex) {
            throw new BusinessRuleException("Failed to store uploaded file");
        }
    }
}
