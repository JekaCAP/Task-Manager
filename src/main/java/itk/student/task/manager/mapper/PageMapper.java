package itk.student.task.manager.mapper;

import itk.student.task.manager.dto.common.PageMeta;
import itk.student.task.manager.dto.common.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class PageMapper {

    public <T, R> PagedResponse<R> toPagedResponse(Page<T> page, Function<T, R> mapper) {
        List<R> content = page.getContent().stream().map(mapper).toList();
        PageMeta meta = new PageMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
        return new PagedResponse<>(content, meta);
    }
}
