package com.roome.domain.userGenrePreference.listner;

import com.roome.domain.mybook.event.BookCollectionEvent;
import com.roome.domain.mycd.event.CdCollectionEvent;
import com.roome.domain.userGenrePreference.service.GenrePreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenrePreferenceEventListener {

    private final GenrePreferenceService genrePreferenceService;

    // CD 추가 이벤트 리스너
    @Async("taskExecutor")
    @EventListener
    @Transactional
    public void handleCdAddedEvent(CdCollectionEvent.CdAddedEvent event) {
        log.info("Handling CD added event for user: {}", event.getUserId());
        genrePreferenceService.updateCdGenrePreferences(event.getUserId());
    }

    // CD 제거 이벤트 리스너
    @Async("taskExecutor")
    @EventListener
    @Transactional
    public void handleCdRemovedEvent(CdCollectionEvent.CdRemovedEvent event) {
        log.info("Handling CD removed event for user: {}", event.getUserId());
        genrePreferenceService.updateCdGenrePreferences(event.getUserId());
    }

    // 책 추가 이벤트 리스너
    @Async("taskExecutor")
    @EventListener
    @Transactional
    public void handleBookAddedEvent(BookCollectionEvent.BookAddedEvent event) {
        log.info("Handling Book added event for user: {}", event.getUserId());
        genrePreferenceService.updateBookGenrePreferences(event.getUserId());
    }

    // 책 제거 이벤트 리스너
    @Async("taskExecutor")
    @EventListener
    @Transactional
    public void handleBookRemovedEvent(BookCollectionEvent.BookRemovedEvent event) {
        log.info("Handling Book removed event for user: {}", event.getUserId());
        genrePreferenceService.updateBookGenrePreferences(event.getUserId());
    }
}
