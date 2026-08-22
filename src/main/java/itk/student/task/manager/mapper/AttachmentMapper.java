package itk.student.task.manager.mapper;

import itk.student.task.manager.dto.response.AttachmentResponse;
import itk.student.task.manager.entity.Attachment;
import org.springframework.stereotype.Component;

@Component
public class AttachmentMapper {

    public AttachmentResponse toResponse(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getTaskId(),
                attachment.getOriginalName(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getUploadedById(),
                attachment.getCreatedAt()
        );
    }
}
