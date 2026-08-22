package itk.student.task.manager.exception;

import itk.student.task.manager.enums.ErrorCode;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
}
