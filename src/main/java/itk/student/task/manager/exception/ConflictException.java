package itk.student.task.manager.exception;

import itk.student.task.manager.enums.ErrorCode;

public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }
}
