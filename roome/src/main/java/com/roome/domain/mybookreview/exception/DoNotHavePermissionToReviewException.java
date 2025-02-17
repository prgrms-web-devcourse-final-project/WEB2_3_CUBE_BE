package com.roome.domain.mybookreview.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class DoNotHavePermissionToReviewException extends BusinessException {

    public DoNotHavePermissionToReviewException() {
        super(ErrorCode.USER_DO_NOT_HAVE_PERMISSION_TO_REVIEW);
    }
}
