package com.roome.domain.userGenrePreference.listner;

import com.roome.domain.mybook.event.BookCollectionEvent;
import com.roome.domain.mycd.event.CdCollectionEvent;
import com.roome.domain.userGenrePreference.service.GenrePreferenceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GenrePreferenceEventListenerTest {

    @Mock
    private GenrePreferenceService genrePreferenceService;

    @InjectMocks
    private GenrePreferenceEventListener eventListener;

    @Test
    @DisplayName("CD 추가 이벤트 처리 성공")
    void handleCdAddedEvent_ShouldCallUpdateCdGenrePreferences() {
        // Given
        Long userId = 1L;
        CdCollectionEvent.CdAddedEvent event = new CdCollectionEvent.CdAddedEvent(this, userId);

        // When
        eventListener.handleCdAddedEvent(event);

        // Then
        verify(genrePreferenceService).updateCdGenrePreferences(userId);
    }

    @Test
    @DisplayName("CD 제거 이벤트 처리 성공")
    void handleCdRemovedEvent_ShouldCallUpdateCdGenrePreferences() {
        // Given
        Long userId = 1L;
        CdCollectionEvent.CdRemovedEvent event = new CdCollectionEvent.CdRemovedEvent(this, userId);

        // When
        eventListener.handleCdRemovedEvent(event);

        // Then
        verify(genrePreferenceService).updateCdGenrePreferences(userId);
    }

    @Test
    @DisplayName("책 추가 이벤트 처리 성공")
    void handleBookAddedEvent_ShouldCallUpdateBookGenrePreferences() {
        // Given
        Long userId = 1L;
        BookCollectionEvent.BookAddedEvent event = new BookCollectionEvent.BookAddedEvent(this, userId);

        // When
        eventListener.handleBookAddedEvent(event);

        // Then
        verify(genrePreferenceService).updateBookGenrePreferences(userId);
    }

    @Test
    @DisplayName("책 제거 이벤트 처리 성공")
    void handleBookRemovedEvent_ShouldCallUpdateBookGenrePreferences() {
        // Given
        Long userId = 1L;
        BookCollectionEvent.BookRemovedEvent event = new BookCollectionEvent.BookRemovedEvent(this, userId);

        // When
        eventListener.handleBookRemovedEvent(event);

        // Then
        verify(genrePreferenceService).updateBookGenrePreferences(userId);
    }
}