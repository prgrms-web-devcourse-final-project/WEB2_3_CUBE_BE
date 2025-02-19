package com.roome.domain.room.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class RoomAuthorizationException extends BusinessException {

    public RoomAuthorizationException() {
        super(ErrorCode.ROOM_ACCESS_DENIED);
    }
}
