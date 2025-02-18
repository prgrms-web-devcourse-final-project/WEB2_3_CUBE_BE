package com.roome.domain.room.exception;

import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;

public class RoomNoFoundException extends BusinessException {

    public RoomNoFoundException() {
        super(ErrorCode.ROOM_NOT_FOUND);
    }
}
