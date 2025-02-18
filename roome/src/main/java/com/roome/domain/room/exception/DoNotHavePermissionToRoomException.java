package com.roome.domain.room.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class DoNotHavePermissionToRoomException extends BusinessException {

    public DoNotHavePermissionToRoomException() {
        super(ErrorCode.USER_DO_NOT_HAVE_PERMISSION_TO_ROOM);
    }
}
