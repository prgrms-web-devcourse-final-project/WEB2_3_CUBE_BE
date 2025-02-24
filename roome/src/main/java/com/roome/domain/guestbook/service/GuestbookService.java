package com.roome.domain.guestbook.service;

import com.roome.domain.guestbook.dto.*;
import com.roome.domain.guestbook.entity.Guestbook;
import com.roome.domain.guestbook.entity.RelationType;
import com.roome.domain.guestbook.repository.GuestbookRepository;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.repository.RoomRepository;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.global.exception.BusinessException;
import com.roome.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuestbookService {
    private final GuestbookRepository guestbookRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public GuestbookListResponseDto getGuestbook(Long roomId, int page, int size) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        Page<Guestbook> guestbookPage = guestbookRepository.findByRoom(room, PageRequest.of(page - 1, size));

        List<GuestbookResponseDto> guestbooks = guestbookPage.stream()
                .map(GuestbookResponseDto::from)
                .collect(Collectors.toList());

        return GuestbookListResponseDto.builder()
                .roomId(roomId)
                .guestbook(guestbooks)
                .pagination(PaginationDto.builder()
                        .page(page)
                        .size(size)
                        .totalPages(guestbookPage.getTotalPages())
                        .build())
                .build();
    }

    @Transactional
    public GuestbookResponseDto addGuestbook(Long roomId, Long userId, GuestbookRequestDto requestDto) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Guestbook guestbook = Guestbook.builder()
                .room(room)
                .user(user)
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .message(requestDto.getMessage())
                .relation(RelationType.지나가던_나그네)
                .build();

        guestbookRepository.save(guestbook);
        return GuestbookResponseDto.from(guestbook);
    }

    @Transactional
    public void deleteGuestbook(Long guestbookId, Long userId) {
        Guestbook guestbook = guestbookRepository.findById(guestbookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GUESTBOOK_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!guestbook.getUser().equals(user)) {
            throw new BusinessException(ErrorCode.GUESTBOOK_DELETE_FORBIDDEN);
        }

        guestbookRepository.delete(guestbook);
    }
}
