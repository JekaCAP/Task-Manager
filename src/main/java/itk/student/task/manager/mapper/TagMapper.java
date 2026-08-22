package itk.student.task.manager.mapper;

import itk.student.task.manager.dto.response.TagResponse;
import itk.student.task.manager.entity.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public TagResponse toResponse(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getProjectId(),
                tag.getName(),
                tag.getColor(),
                tag.getCreatedAt()
        );
    }
}
