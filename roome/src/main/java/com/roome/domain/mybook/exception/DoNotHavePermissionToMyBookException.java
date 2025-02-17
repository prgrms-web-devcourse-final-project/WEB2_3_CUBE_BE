package com.roome.domain.mybook.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class DoNotHavePermissionToMyBookException extends BusinessException {

    public DoNotHavePermissionToMyBookException() {
        super(ErrorCode.USER_DO_NOT_HAVE_PERMISSION_TO_MY_BOOK);
    }
}
