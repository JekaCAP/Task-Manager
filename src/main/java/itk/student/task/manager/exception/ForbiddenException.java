package itk.student.task.manager.exception;

import itk.student.task.manager.enums.ErrorCode;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }
}
