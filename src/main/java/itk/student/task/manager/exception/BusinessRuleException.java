package itk.student.task.manager.exception;

import itk.student.task.manager.enums.ErrorCode;

public class BusinessRuleException extends ApiException {

    public BusinessRuleException(String message) {
        super(ErrorCode.UNPROCESSABLE_ENTITY, message);
    }
}
