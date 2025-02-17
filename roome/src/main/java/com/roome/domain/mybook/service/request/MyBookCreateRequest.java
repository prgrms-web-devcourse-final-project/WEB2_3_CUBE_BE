package com.roome.domain.mybook.service.request;

import java.time.LocalDate;

public record MyBookCreateRequest(
        Long isbn,
        String title,
        String author,
        String publisher,
        LocalDate publishedDate,
        String imageUrl,
        String category,
        Long page
) {
}
