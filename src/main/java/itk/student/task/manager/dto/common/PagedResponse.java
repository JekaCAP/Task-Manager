package itk.student.task.manager.dto.common;

import java.util.List;

public record PagedResponse<T>(List<T> content, PageMeta meta) {
}
