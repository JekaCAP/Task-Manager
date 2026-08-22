package itk.student.task.manager.exception;

import itk.student.task.manager.enums.ErrorCode;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }
}
